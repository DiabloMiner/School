package com.diablominer.opengl.examples.learning;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class EventManager {

    private final List<Event> currentEvents;
    private final Map<Class<?>, Set<EventObserver>> eventObservers;

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
            try {
                observer.getClass().getMethod(EventObserver.class.getMethods()[0].getName(), event.getClass()).invoke(observer, event);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                System.err.println("Event could not be executed. Exception: ");
                e.printStackTrace();
            }
        }
    }

}
