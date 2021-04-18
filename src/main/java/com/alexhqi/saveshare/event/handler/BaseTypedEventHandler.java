package com.alexhqi.saveshare.event.handler;

import com.alexhqi.saveshare.event.Event;
import com.alexhqi.saveshare.event.EventHandler;

public abstract class BaseTypedEventHandler<T extends Event> implements EventHandler {

    private final Class<T> eventClass;

    protected BaseTypedEventHandler(Class<T> eventClass) {
        this.eventClass = eventClass;
    }

    @Override
    public void handle(Event event) {
        if (event.isFinished()) {
            return;
        }
        if (!event.getType().equals(getHandlerType())) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " for event type " + getHandlerType() +
                    " could not handle event id " + event.getID() + " with type " + event.getType());
        }
        // I'm both interesting in, and disgusted by this casting approach to generic types.
        if (!eventClass.isAssignableFrom(event.getClass())) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " for event type " + getHandlerType() +
                    " could not handle event id " + event.getID() + " with class " + event.getClass());
        }
        doHandle(eventClass.cast(event));
        event.finish();
    }

    protected abstract void doHandle(T event);

}
