package ld28;

import ld28.App;
import ldtk.StateSelector;
import ldtk.Kernel;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class LD28 {
	public static void main(String[] args) {
		LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
		cfg.title = "LD28";
		cfg.useGL20 = true;
		cfg.width = 1280;
		cfg.height = 720;

		// To start a desktop game just tell the kernel about your app so that it can switch between your game's
		// states, then start it all through LwglApplication just as you would any other LibGDX desktop game.
		StateSelector gameStateSelector = new App();
		Kernel kernel = new Kernel(gameStateSelector);
		new LwjglApplication(kernel, cfg);
	}
}
