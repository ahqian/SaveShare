package com.alexhqi.event;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class EventHandlerFactory {

    private EventHandlerFactory() {}

    private static Map<String, EventHandler> handlerMap = new ConcurrentHashMap<>();

    public static void registerHandler(EventHandler handler) {
        handlerMap.put(handler.getHandlerType(), handler);
    }

    public static EventHandler getHandlerFor(String type) {
        return handlerMap.get(type);
    }

}
