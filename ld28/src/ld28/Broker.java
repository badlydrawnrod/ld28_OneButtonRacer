package ld28;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Broker {

	private Map<Class<? extends Event>, List<Subscriber>> subscriberMap;

	public Broker() {
		subscriberMap = new HashMap<Class<? extends Event>, List<Subscriber>>();
	}
	

	public Broker subscribe(Class<? extends Event> klass, Subscriber listener) {
		List<Subscriber> listeners = subscriberMap.get(klass);
		if (listeners == null) {
			listeners = new ArrayList<Subscriber>();
		}
		listeners.add(listener);
		subscriberMap.put(klass, listeners);
		return this;
	}
	
	public Broker unsubscribe(Class<? extends Event> klass, Subscriber listener) {
		List<Subscriber> listeners = subscriberMap.get(klass);
		if (listeners != null) {
			while (listeners.remove(listener)) {
			}
			if (listeners.size() == 0) {
				subscriberMap.remove(klass);
			}
		}
		return this;
	}
	
	public Broker unsubscribeAll(Class<? extends Event> klass) {
		subscriberMap.remove(klass);
		return this;
	}
	
	public void publish(Event event) {
		Class<? extends Event> klass = event.getClass();
		List<Subscriber> subscribers = subscriberMap.get(klass);
		if (subscribers != null) {
			// Iterate in reverse order so that subscribers can remove themselves when called.
			for (int i = subscribers.size() - 1; i >= 0; i--) {
				Subscriber subscriber = subscribers.get(i);
				subscriber.onEvent(event);
			}
		}
	}
}
