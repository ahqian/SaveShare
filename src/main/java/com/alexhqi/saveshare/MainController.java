package com.alexhqi.saveshare;

import com.alexhqi.saveshare.core.Game;
import com.alexhqi.saveshare.core.GameManager;
import com.alexhqi.saveshare.core.SaveConfiguration;
import com.alexhqi.saveshare.dependency.ScuffedServiceContext;
import com.alexhqi.saveshare.event.EventBus;
import com.alexhqi.saveshare.event.EventProcessor;
import com.alexhqi.saveshare.event.type.AppWorkingEvent;
import com.alexhqi.saveshare.event.type.StartGameEvent;
import com.alexhqi.saveshare.event.type.ValidateGameEvent;
import com.alexhqi.saveshare.service.SaveServiceFactory;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class MainController implements Initializable {

    @FXML
    public TextField executableText;

    @FXML
    public TextField saveDirText;

    @FXML
    public ComboBox<String> saveServiceCombo;

    @FXML
    public ComboBox<UUID> referenceCombo;

    @FXML
    public TextFlow errorTextArea;

    @FXML
    private VBox mainBox;

    @FXML
    private ListView<Game> gameListView;

    @FXML
    private TextArea gameInfoPane;

    @FXML
    private TextField nameText;

    private GameManager gameManager;
    private EventBus eventBus;

    @FXML
    private void onServiceSelected(ActionEvent event) {
        // todo this whole service selection could be removed with better save reference management
    }

    @FXML
    private void onAddGame() {
        Game game = getGameFromInput();
        eventBus.registerEvent(new AppWorkingEvent("Validate Game", AppWorkingEvent.Status.WORKING, mainBox));
        eventBus.registerEvent(new ValidateGameEvent(validationResult -> {
            if (validationResult.isSuccess()) {
                try {
                    gameManager.addGame(game);
                    // have to request ui thread to run later because events are handled in non-ui worker threads
                    Platform.runLater(() -> {
                        gameListView.getItems().add(game);
                    });
                } catch (IOException e) {
                    setErrorText("Failed to persist new Game configuration. Exception: " + e.getMessage());
                }
            } else {
                setErrorText(validationResult.getReason());
            }
            return null;
        }, game));
        eventBus.registerEvent(new AppWorkingEvent("Validate Game", AppWorkingEvent.Status.COMPLETE, mainBox));
    }

    private Game getGameFromInput() {
        Game game = new Game();
        game.setName(nameText.getText());
        game.setExecutable(new File(executableText.getText()));
        SaveConfiguration saveConfiguration = new SaveConfiguration();
        saveConfiguration.setSaveServiceId(saveServiceCombo.getValue());
        saveConfiguration.setGameSaveDirectory(new File(saveDirText.getText()));
        saveConfiguration.setRemoteSaveUuid(referenceCombo.getValue());
        game.setSaveConfiguration(saveConfiguration);
        return game;
    }

    private void setErrorText(String s) {
        // this is usually called from a separate worker thread
        Platform.runLater(() -> {
            Text text = new Text(s);
            text.setFill(Color.RED);
            errorTextArea.getChildren().clear();
            errorTextArea.getChildren().add(text);
        });
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

        SaveServiceFactory.getServices().forEach(service -> {
            saveServiceCombo.getItems().add(service.getId());
            referenceCombo.getItems().addAll(service.getAllSaves());
        });

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
        gameListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // todo convert gameInfoPane into logging output window
            gameInfoPane.setText(getGameInfoText(newValue));
            nameText.setText(newValue.getName());
            executableText.setText(newValue.getExecutable().getAbsolutePath());
            saveDirText.setText(newValue.getSaveConfiguration().getGameSaveDirectory().getAbsolutePath());
            saveServiceCombo.getSelectionModel().clearAndSelect(
                    saveServiceCombo.getItems().indexOf(newValue.getSaveConfiguration().getSaveServiceId())
            );
            referenceCombo.getSelectionModel().clearAndSelect(
                    referenceCombo.getItems().indexOf(newValue.getSaveConfiguration().getRemoteSaveUuid())
            );
        });
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
