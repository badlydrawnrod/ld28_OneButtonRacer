package ld28;

import ldtk.Camera;
import ldtk.Font;
import ldtk.Kernel;
import ldtk.State;
import ldtk.Tune;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
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
		world = new World(isTwoPlayer);
		worldRenderer = new WorldRenderer(world, gameCam);
		worldRenderer.setup();
		soundtrack = Kernel.tunes.get("music/soundtrack");
		soundtrack.setLooping(true);
		soundtrack.play();
		scoreFont = Kernel.fonts.get("fonts/consolas32");
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
		world.update();
	}

	@Override
	public void draw() {
		worldRenderer.draw();
		guiCam.activate();
		String scoreString = String.format("Player One: %010d", world.player1Score());
		scoreFont.draw(scoreString,
				-guiCam.windowWidth() / 2,
				-guiCam.windowHeight() / 2 + scoreFont.height(),
				Color.YELLOW);
		if (isTwoPlayer) {
			scoreString = String.format("Player Two: %010d", world.player2Score());
			Rectangle bounds = scoreFont.bounds(scoreString);
			scoreFont.draw(scoreString,
					guiCam.windowWidth() / 2 - bounds.width,
					-guiCam.windowHeight() / 2 + scoreFont.height(),
					Color.YELLOW);
		}
	}
}
