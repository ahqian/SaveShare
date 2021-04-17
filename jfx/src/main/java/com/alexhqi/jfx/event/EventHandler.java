package com.alexhqi.jfx.event;

public interface EventHandler {

    String getHandlerType();
    void handle(Event event);

}
