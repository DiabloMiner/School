package com.diablominer.opengl.examples.learning;

import java.util.*;

public class EventManager {

    public List<Event> currentEvents;
    public Map<Class<?>, Set<EventObserver>> eventObservers;

    public EventManager() {
        currentEvents = new ArrayList<>();
        eventObservers = new HashMap<>();
    }

    public void addEventObserver(EventTypes eventType, EventObserver observer) {
        eventObservers.putIfAbsent(eventType.classValue, new HashSet<>());
        eventObservers.get(eventType.classValue).add(observer);
    }

    public void addEvent(Event event) {
        currentEvents.add(event);
    }

    public void executeEvents() {
        for (Event event : currentEvents) {
            executeEvent(event);
        }
        currentEvents.clear();
    }

    public void executeEvent(Event event) {
        Set<EventObserver> observers = eventObservers.getOrDefault(event.getClass(), new HashSet<>());
        for (EventObserver observer : observers) {
            observer.update(event);
        }
    }

}
