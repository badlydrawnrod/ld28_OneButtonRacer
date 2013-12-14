package ld28;

import ldtk.Camera;
import ldtk.Kernel;
import ldtk.State;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;

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

	public Playing(App app) {
		this.app = app;
	}
	
	@Override
	public void enter() {
		Gdx.input.setCatchBackKey(true);
		virtualWidth = 800;
		virtualHeight = 480;
		guiCam = Kernel.cameras.create("guiCam", virtualWidth, virtualHeight);
		gameCam = Kernel.cameras.create("gameCam", virtualWidth, virtualHeight);
		world = new World();
		worldRenderer = new WorldRenderer(world, gameCam);
		worldRenderer.setup();
	}

	@Override
	public void exit() {
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
	}
}
