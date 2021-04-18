package com.alexhqi.saveshare;

import com.alexhqi.saveshare.dependency.ScuffedServiceContext;
import com.alexhqi.saveshare.event.EventBus;
import com.alexhqi.saveshare.event.EventProcessor;
import com.alexhqi.saveshare.event.type.AppWorkingEvent;
import com.alexhqi.saveshare.event.type.StartGameEvent;
import com.alexhqi.saveshare.core.Game;
import com.alexhqi.saveshare.core.GameManager;
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
        return "Name: " + game.getName() + "\n\n" +
                "Executable: " + game.getExecutable().getAbsolutePath() + "\n" +
                "Save Directory: " + game.getSaveConfiguration().getGameSaveDirectory().getAbsolutePath() + "\n\n" +
                "Save Service ID: " + game.getSaveConfiguration().getSaveServiceId() + "\n" +
                "Save Service Reference: " + game.getSaveConfiguration().getRemoteSaveUuid() + "\n";
    }
}
