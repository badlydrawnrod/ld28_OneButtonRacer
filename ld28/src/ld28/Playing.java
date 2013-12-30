package ld28;

import ldtk.Camera;
import ldtk.Font;
import ldtk.Image;
import ldtk.Kernel;
import ldtk.State;
import ldtk.Tune;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.math.Rectangle;

public class Playing extends State {

	private final App app;
	private Camera guiCam;
	private Camera gameCam;
	private float virtualWidth;
	private float virtualHeight;
	private WorldRenderer worldRenderer;
	private World world;
	private boolean isEscapePressed;
	private boolean isBackPressed;
	private Tune soundtrack;
	private Font scoreFont;
	private boolean isTwoPlayer;
	private boolean isSpacePressed;
	private Image greenBarImage;

	public Playing(App app) {
		this.app = app;
		this.isTwoPlayer = false;
	}
	
	public void setTwoPlayer(boolean isTwoPlayer) {
		this.isTwoPlayer = isTwoPlayer;
	}
	
	@Override
	public void enter() {
		Gdx.input.setCatchBackKey(true);
		virtualWidth = 1280;
		virtualHeight = 720;
		guiCam = Kernel.cameras.create("guiCam", virtualWidth, virtualHeight);
		gameCam = Kernel.cameras.create("gameCam", virtualWidth, virtualHeight);
		guiCam.setScissored(false);
		world = new World(isTwoPlayer);
		worldRenderer = new WorldRenderer(world, gameCam);
		soundtrack = Kernel.tunes.get("music/soundtrack");
		soundtrack.setLooping(true);
		soundtrack.play();
		scoreFont = Kernel.fonts.get("fonts/consolas32");
		greenBarImage = Kernel.images.get("atlases/ld28/greenbar");
	}

	@Override
	public void exit() {
		soundtrack.stop();
		Gdx.input.setCatchBackKey(false);
		gameCam.dispose();
		guiCam.dispose();
	}

	@Override
	public void update() {
		boolean wasEscapePressed = isEscapePressed;
		isEscapePressed = Gdx.input.isKeyPressed(Keys.ESCAPE);
		boolean wasBackPressed = isBackPressed;
		isBackPressed = Gdx.input.isKeyPressed(Keys.BACK);
		if ((wasEscapePressed && !isEscapePressed) || (wasBackPressed && !isBackPressed)) {
			app.requestMenu();
			return;
		}
		boolean wasSpacePressed = isSpacePressed;
		isSpacePressed = Gdx.input.isKeyPressed(Keys.SPACE);
		boolean justTouched = Gdx.input.justTouched();
		if (((wasSpacePressed && !isSpacePressed) || justTouched) && world.canQuit()) {
			app.requestMenu();
			return;
		}
		int oldLevel = world.level();
		world.update();
		if (world.level() != oldLevel) {
			worldRenderer.onLevelStart();
		}
	}

	@Override
	public void draw() {
		Gdx.gl.glDisable(GL10.GL_SCISSOR_TEST);
		Gdx.gl.glClearColor(0.0f, 0.25f, 0.0f, 1.0f);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		worldRenderer.draw();
		guiCam.activate();
		
		// Draw player one's info.
		String scoreString = String.format("Player One: %010d", world.player1Score());
		scoreFont.draw(scoreString,
				-guiCam.windowWidth() / 2,
				guiCam.windowHeight() / 2 - scoreFont.height(),
				Color.YELLOW);
		int lap = world.player1Lap();
		int laps = world.laps();
		String lapString;
		if (lap > laps) {
			lapString = "*** FINISHED ***";
		}
		else if (world.player1Health() < 0) {
			lapString = "*** CRASHED ***";
		}
		else {
			lapString = String.format("Lap %d/%d",  lap, laps);
		}
		if (lapString.charAt(0) != '*' || Kernel.time.time % 0.5f < 0.25f) {
			scoreFont.draw(lapString,
					-guiCam.windowWidth() / 2,
					guiCam.windowHeight() / 2 - 2 * scoreFont.height(),
					Color.YELLOW);
		}
		int numBars = (int) ((world.player1Health() * 10) + 0.5f);
		for (int i = 0; i < numBars; i++) {
			greenBarImage.draw(-guiCam.windowWidth() / 2 + 4 + 16 * i, guiCam.windowHeight() / 2 - 80);
		}

		// Draw player two's info if required.
		if (isTwoPlayer) {
			scoreString = String.format("Player Two: %010d", world.player2Score());
			Rectangle bounds = scoreFont.bounds(scoreString);
			scoreFont.draw(scoreString,
					guiCam.windowWidth() / 2 - bounds.width,
					guiCam.windowHeight() / 2 - scoreFont.height(),
					Color.YELLOW);
			lap = world.player2Lap();
			if (lap > laps) {
				lapString = "*** FINISHED ***";
			}
			else if (world.player2Health() < 0) {
				lapString = "*** CRASHED ***";
			}
			else {
				lapString = String.format("Lap %d/%d",  lap, laps);
			}
			bounds = scoreFont.bounds(lapString);
			if (lapString.charAt(0) != '*' || Kernel.time.time % 0.5f < 0.25f) {
				scoreFont.draw(lapString,
						guiCam.windowWidth() / 2 - bounds.width,
						guiCam.windowHeight() / 2 - 2 * scoreFont.height(),
						Color.YELLOW);
			}
			numBars = (int) ((world.player2Health() * 10) + 0.5f);
			for (int i = 0; i < numBars; i++) {
				greenBarImage.draw(guiCam.windowWidth() / 2 - 4 - 16 * i, guiCam.windowHeight() / 2 - 80);
			}
		}
		
		// Draw the race over text if required.
		if (world.isGameOver()) {
			String gameOverString = "*** RACE OVER ***";
			Rectangle bounds = scoreFont.bounds(gameOverString);
			scoreFont.draw(gameOverString,
					-bounds.width / 2,
					-bounds.height / 2,
					Color.YELLOW);
			if (world.canQuit()) {
				boolean isOnAndroid = Gdx.app.getType() == ApplicationType.Android;
				if (isOnAndroid) {
					gameOverString = "tap to continue";
				}
				else {
					gameOverString = "press [SPACE] to continue";
				}
				bounds = scoreFont.bounds(gameOverString);
				scoreFont.draw(gameOverString,
						-bounds.width / 2,
						-bounds.height / 2 - 2 * bounds.height,
						Color.YELLOW);
			}
		}
		
		// Draw the game won text if required.
		if (world.isGameWon()) {
			String gameWonString = "*** RACE COMPLETED ***";
			Rectangle bounds = scoreFont.bounds(gameWonString);
			scoreFont.draw(gameWonString,
					-bounds.width / 2,
					-bounds.height / 2,
					Color.YELLOW);
			if (world.canQuit()) {
				boolean isOnAndroid = Gdx.app.getType() == ApplicationType.Android;
				if (isOnAndroid) {
					gameWonString = "tap to continue";
				}
				else {
					gameWonString = "press [SPACE] to continue";
				}
				bounds = scoreFont.bounds(gameWonString);
				scoreFont.draw(gameWonString,
						-bounds.width / 2,
						-bounds.height / 2 - 2 * bounds.height,
						Color.YELLOW);
			}
		}
		
		// Draw the track name.
		String nameString = world.levelName();
		Rectangle bounds = scoreFont.bounds(nameString);
		scoreFont.draw(nameString,
				-bounds.width / 2,
				guiCam.windowHeight() / 2 - bounds.height,
				Color.YELLOW);
	}
}
