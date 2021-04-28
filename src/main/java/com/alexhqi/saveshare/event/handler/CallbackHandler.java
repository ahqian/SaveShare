package com.alexhqi.saveshare.event.handler;

import com.alexhqi.saveshare.event.type.CallbackEvent;

public abstract class CallbackHandler<T extends CallbackEvent<V>, V> extends BaseTypedEventHandler<T> {

    protected CallbackHandler(Class<T> eventClass) {
        super(eventClass);
    }

    @Override
    protected void doHandle(T event) {
        V result = handleEventForResult(event);
        event.getCallback().apply(result);
    }

    /**
     * Handle the event however necessary. Do not execute the callback method here.
     * @param event The callback event to handle.
     * @return Value which will be passed into the callback as input.
     */
    protected abstract V handleEventForResult(T event);
}
