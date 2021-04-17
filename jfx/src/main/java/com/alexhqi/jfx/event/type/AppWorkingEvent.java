package com.alexhqi.jfx.event.type;

import javafx.scene.layout.Pane;

public class AppWorkingEvent extends BaseEvent {

    public enum Status {
        WORKING,
        COMPLETE;
    }

    public static final String TYPE = "APP_WORKING_EVENT";

    private final String taskCode;
    private final Status status;
    private final Pane pane;

    public AppWorkingEvent(String taskCode, Status status, Pane pane) {
        super(TYPE);
        this.taskCode = taskCode;
        this.status = status;
        this.pane = pane;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public Status getStatus() {
        return status;
    }

    public Pane getPane() {
        return pane;
    }
}
