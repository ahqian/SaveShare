package com.alexhqi.event.handler;

import com.alexhqi.event.type.AppWorkingEvent;
import javafx.application.Platform;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;

import java.util.HashSet;
import java.util.Set;

/**
 * Disables a given UI container while there is at least one ongoing task (identified by the event's task code).
 */
public class AppWorkingHandler extends BaseTypedEventHandler<AppWorkingEvent> {

    public AppWorkingHandler() {
        super(AppWorkingEvent.class);
    }

    private final Set<String> ongoingTasks = new HashSet<>();
    private final ProgressIndicator progressIndicator = new ProgressIndicator();

    @Override
    public String getHandlerType() {
        return AppWorkingEvent.TYPE;
    }

    @Override
    protected void doHandle(AppWorkingEvent event) {
        Pane pane = event.getPane();
        if (event.getStatus() == AppWorkingEvent.Status.WORKING) {
            ongoingTasks.add(event.getTaskCode());
            Platform.runLater(() -> {
                pane.setDisable(true);
                pane.getChildren().add(progressIndicator);
                progressIndicator.setLayoutX(pane.getWidth() / 2);
                progressIndicator.setLayoutY(pane.getHeight() / 2);
                progressIndicator.setVisible(true);
            });
        } else if (event.getStatus() == AppWorkingEvent.Status.COMPLETE) {
            ongoingTasks.remove(event.getTaskCode());
            if (ongoingTasks.isEmpty()) {
                Platform.runLater(() -> {
                    pane.getChildren().remove(progressIndicator);
                    progressIndicator.setVisible(false);
                    pane.setDisable(false);
                });
            }
        }
    }
}
