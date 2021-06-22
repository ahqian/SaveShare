package com.alexhqi.saveshare.event.handler;

import com.alexhqi.saveshare.event.type.TaskEvent;

public class TaskHandler extends BaseTypedEventHandler<TaskEvent> {

    public TaskHandler() {
        super(TaskEvent.class);
    }

    @Override
    protected void doHandle(TaskEvent event) {
        try {
            event.getTask().call();
        } catch (Exception e) {
            // not really a good way to deal with this right now. Should expand with proper handling.
        }
    }

    @Override
    public String getHandlerType() {
        return TaskEvent.TYPE;
    }
}
