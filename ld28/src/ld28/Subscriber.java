package ld28;

public interface Subscriber {

	/**
	 * Called when an event should be handled.
	 * 
	 * @param event the event.
	 */
	public abstract void onEvent(Event event);
}
