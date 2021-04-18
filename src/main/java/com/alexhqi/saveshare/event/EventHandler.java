package com.alexhqi.saveshare.event;

public interface EventHandler {

    String getHandlerType();
    void handle(Event event);

}
