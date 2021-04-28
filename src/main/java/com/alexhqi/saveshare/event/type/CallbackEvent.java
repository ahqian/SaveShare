package com.alexhqi.saveshare.event.type;

import java.util.function.Function;

public abstract class CallbackEvent<T> extends BaseEvent {

    private final Function<T,Void> callback;

    public CallbackEvent(String type, Function<T, Void> callback) {
        super(type);
        this.callback = callback;
    }

    public Function<T, Void> getCallback() {
        return callback;
    }
}
