package com.alexhqi.saveshare.event.type;

import java.util.concurrent.Callable;

public class TaskEvent extends BaseEvent {

    public static final String TYPE = "TASK_EVENT";

    private final Callable<Void> task;

    public TaskEvent(Callable<Void> task) {
        super(TYPE);
        this.task = task;
    }

    public Callable<Void> getTask() {
        return task;
    }
}
