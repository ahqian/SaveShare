package com.alexhqi.event;

/**
 * Provides an event bus interface for the registration of new events and the retrieval of registered events.
 * Internal event priority and return order is left to implementation discretion.
 * Implementations must be thread safe.
 */
public interface EventBus {

    void registerEvent(Event event);
    Event getNextEvent();
    boolean hasEvents();


}
