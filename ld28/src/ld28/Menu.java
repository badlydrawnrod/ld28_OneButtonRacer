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
	private boolean isOnePressed;
	private boolean isEscapePressed;
	private String startInstructions = "Press [1] or [2] to select the number of players";
	private String firstLine = "Player 1 (red car) - press 'A' to change lanes";
	private String secondLine = "Player 2 (blue car) - press 'L' to change lanes";
	private boolean isTwoPressed;

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
		boolean wasOnePressed = isOnePressed;
		isOnePressed = Gdx.input.isKeyPressed(Keys.NUM_1);
		if ((wasOnePressed && !isOnePressed)) {
			app.requestPlaying(false);
		}
		boolean wasTwoPressed = isTwoPressed;
		isTwoPressed = Gdx.input.isKeyPressed(Keys.NUM_2);
		if ((wasTwoPressed && !isTwoPressed)) {
			app.requestPlaying(true);
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
		Rectangle rect = font.bounds(startInstructions);
		float x = -rect.width / 2;
		float y = -rect.height / 2;
		font.draw(startInstructions, x, y, Color.WHITE);
		y -= rect.height * 2;
		font.draw(firstLine, x, y, Color.WHITE);
		y -= rect.height * 2;
		font.draw(secondLine, x, y, Color.WHITE);
	}
}