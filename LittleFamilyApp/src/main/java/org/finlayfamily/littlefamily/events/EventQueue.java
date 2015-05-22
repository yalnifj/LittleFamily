package org.finlayfamily.littlefamily.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jfinlay on 5/22/2015.
 */
public class EventQueue {
    private Map<String, List<EventListener>> subscribers;

    private static EventQueue ourInstance = new EventQueue();

    public static EventQueue getInstance() {
        return ourInstance;
    }

    private EventQueue() {
        subscribers = new HashMap<>();
    }

    public void subscribe(String topic, EventListener listener) {
        List<EventListener> listeners = subscribers.get(topic);
        if (listeners==null) {
            listeners = new ArrayList<>();
            subscribers.put(topic, listeners);
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void unSubscribe(String topic, EventListener listener) {
        List<EventListener> listeners = subscribers.get(topic);
        if (listeners!=null) {
            listeners.remove(listener);
        }
    }

    public void publish(String topic, Object o) {
        List<EventListener> listeners = subscribers.get(topic);
        if (listeners!=null) {
            for (EventListener l : listeners) {
                l.onEvent(topic, o);
            }
        }
    }
}
