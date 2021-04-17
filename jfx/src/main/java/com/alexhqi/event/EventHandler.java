package com.alexhqi.event;

public interface EventHandler {

    String getHandlerType();
    void handle(Event event);

}
