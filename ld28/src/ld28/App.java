package ld28;

import ldtk.State;
import ldtk.StateSelector;

public class App implements StateSelector {

	/**
	 * An event broker, available to all classes in the application.
	 */
	public static Broker broker;
	
	private Playing playing;
	private Menu menu;
	private State state;

	public App() {
		broker = new Broker();
		playing = new Playing(this);
		menu = new Menu(this);
		state = menu;
	}

	@Override
	public State select() {
		return state;
	}
	
	public void requestPlaying(boolean isTwoPlayer) {
		if (state == menu) {
			playing.setTwoPlayer(isTwoPlayer);
			state = playing;
		}
	}
	
	public void requestMenu() {
		if (state == playing) {
			state = menu;
		}
	}
}
