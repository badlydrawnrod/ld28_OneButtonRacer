package ld28;

import ldtk.Camera;
import ldtk.Font;
import ldtk.Kernel;
import ldtk.State;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;

public class Menu extends State {

	private final App app;
	private Camera guiCam;
	private Font font;
	private boolean isSpacePressed;
	private boolean isEscapePressed;
	private String pressSpace = "Press Space or Tap to play";

	public Menu(App app) {
		this.app = app;
	}
	
	@Override
	public void enter() {
		guiCam = Kernel.cameras.create("guiCam");
		font = Kernel.fonts.get("fonts/consolas32");
	}

	@Override
	public void exit() {
		guiCam.dispose();
	}

	@Override
	public void update() {
		boolean wasSpacePressed = isSpacePressed;
		isSpacePressed = Gdx.input.isKeyPressed(Keys.SPACE);
		if ((wasSpacePressed && !isSpacePressed) || Gdx.input.justTouched()) {
			app.requestPlaying();
		}
		boolean wasEscapePressed = isEscapePressed;
		isEscapePressed = Gdx.input.isKeyPressed(Keys.ESCAPE);
		if (wasEscapePressed && !isEscapePressed) {
			Gdx.app.exit();
		}
	}

	@Override
	public void draw() {
		guiCam.activate();
		Rectangle rect = font.bounds(pressSpace);
		float x = -rect.width / 2;
		float y = -rect.height / 2;
		font.draw(pressSpace, x, y, Color.WHITE);
	}
}