package com.alexhqi.jfx.dependency;

import java.util.HashMap;
import java.util.Map;

// as the name implies, this is scuffed beyond belief.
// Need to set up some proper dependency injection with JavaFX.. once I figure out how.
public class ScuffedServiceContext {

    private ScuffedServiceContext() {}

    private static final Map<Class<?>, Object> instances = new HashMap<>();

    public static <T> void registerInstance(Class<T> tClass, T object) {
        instances.put(tClass, object);
    }

    public static <T> T getInstance(Class<T> tClass) {
        Object object = instances.get(tClass);
        if (object == null) {
            throw new IllegalStateException("Requested unregistered instance of " + tClass.getCanonicalName());
        } else if (tClass.isInstance(object)) {
            return tClass.cast(object);
        }
        throw new IllegalStateException("Registered instance for class " + tClass.getCanonicalName() + " is of type " +
                object.getClass().getCanonicalName());
    }
}
