package com.alexhqi.event.type;

import com.alexhqi.event.Event;

import java.util.UUID;

public abstract class BaseEvent implements Event {

    private boolean finished = false;
    private final String type;
    private final UUID uuid;

    protected BaseEvent(String type) {
        this.type = type;
        this.uuid = UUID.randomUUID();
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public UUID getID() {
        return uuid;
    }

    @Override
    public void finish() {
        finished = true;
    }

    @Override
    public boolean isFinished() {
        return finished;
    }
}
