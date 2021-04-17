package com.alexhqi;

import com.alexhqi.dependency.ScuffedServiceContext;
import com.alexhqi.event.EventBus;
import com.alexhqi.event.EventProcessor;
import com.alexhqi.event.type.AppWorkingEvent;
import com.alexhqi.event.type.StartGameEvent;
import com.alexhqi.saveapp.core.Game;
import com.alexhqi.saveapp.core.GameManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML
    private VBox mainBox;

    @FXML
    private ListView<Game> gameListView;

    @FXML
    private TextArea gameInfoPane;

    private GameManager gameManager;
    private EventBus eventBus;

    @FXML
    private void switchToSecondary() throws IOException {
        eventBus.registerEvent(new AppWorkingEvent("task", AppWorkingEvent.Status.WORKING, mainBox));
//        mainBox.setDisable(true);
//        mainBox.getChildren();
//        App.setRoot("secondary");
    }

    @FXML
    private void launchSelectedGame() {
        Game game = gameListView.getSelectionModel().getSelectedItem();
        if (game != null) {
            // these events are processed sequentially, using the same bus
            eventBus.registerEvent(new AppWorkingEvent("Play Game", AppWorkingEvent.Status.WORKING, mainBox));
            eventBus.registerEvent(new StartGameEvent(game));
            eventBus.registerEvent(new AppWorkingEvent("Play Game", AppWorkingEvent.Status.COMPLETE, mainBox));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gameManager = ScuffedServiceContext.getInstance(GameManager.class);
        gameManager.getGames().forEach(game -> gameListView.getItems().add(game));
        gameListView.setCellFactory((param -> new ListCell<>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);
                if (empty | item == null || item.getName() == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        }));
        gameListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        gameListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> gameInfoPane.setText(getGameInfoText(newValue)));
        eventBus = EventProcessor.getInstance().getBus();
    }

    private String getGameInfoText(Game game) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(game.getName()).append("\n\n");
        sb.append("Executable: ").append(game.getExecutable().getAbsolutePath()).append("\n");
        sb.append("Save Directory: ").append(game.getSaveConfiguration().getGameSaveDirectory().getAbsolutePath()).append("\n\n");
        sb.append("Save Service ID: ").append(game.getSaveConfiguration().getSaveServiceId()).append("\n");
        sb.append("Save Service Reference: ").append(game.getSaveConfiguration().getRemoteSaveUuid()).append("\n");
        return sb.toString();
    }
}
