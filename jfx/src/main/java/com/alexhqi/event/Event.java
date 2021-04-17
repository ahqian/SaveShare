package com.alexhqi.event;

import java.util.UUID;

/**
 * Holds the necessary info to uniquely identify the event,
 * its type, and any necessary transport data it must carry.
 * Event implementations must be thread safe.
 */
public interface Event {

    // Type must be unique and case INSENSITIVE.
    String getType();
    UUID getID();

    void finish();
    boolean isFinished();

}
