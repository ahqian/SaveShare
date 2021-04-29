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
import com.alexhqi.saveshare.event.type.ValidateGitRepoEvent;
import com.alexhqi.saveshare.service.RemoteSaveService;
import com.alexhqi.saveshare.service.SaveServiceFactory;
import com.alexhqi.saveshare.service.git.GitRepo;
import com.alexhqi.saveshare.service.git.GitSaveService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
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

    public VBox gitSourceBox;
    public TextField gitSourceNameTextField;
    public TextField gitSourceUriTextField;
    public TextField gitSourceTokenTextField;
    public ComboBox<String> sourceServiceCombo;
    // todo this should be a generic source interface
    public ListView<GitRepo> sourceListView;

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
        // todo this whole service selection could be removed with better save reference management for cleaner ui
        try {
            RemoteSaveService service = SaveServiceFactory.getService(saveServiceCombo.getValue());
            referenceCombo.getItems().clear();
            referenceCombo.getItems().addAll(service.getAllSaves());
        } catch (IllegalArgumentException e) {
            setErrorText("Could not find internal Remote Save Service with id: "+ saveServiceCombo.getValue());
        }
    }

    @FXML
    private void onAddGame() {
        Game game = getGameFromInput();
        eventBus.registerEvent(new AppWorkingEvent("Validate Game", AppWorkingEvent.Status.WORKING, mainBox));
        eventBus.registerEvent(new ValidateGameEvent(validationResult -> {
            if (validationResult.isSuccess()) {
                try {
                    // this is running on task thread - not ui thread
                    gameManager.addGame(game);
                    // have to request ui thread to run later because events are handled in non-ui worker threads
                    Platform.runLater(() -> {
                        gameListView.getItems().add(game);
                        gameListView.getSelectionModel().select(game);
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

        // SAVE SERVICE INITIALIZATION
        SaveServiceFactory.getServices().forEach(service -> {
            saveServiceCombo.getItems().add(service.getId());
            sourceServiceCombo.getItems().add(service.getId());
        });

        // GAMES LIST INITIALIZATION
        gameListView.setCellFactory((param -> new ListCell<>() {
            @Override
            protected void updateItem(Game item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getName() == null) {
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

        // GIT REPOS
        sourceListView.setCellFactory((param -> new ListCell<>() {
            @Override
            protected void updateItem(GitRepo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getName() == null) {
                    setText(null);
                } else {
                    setText(item.getName());
                }
            }
        }));
        sourceListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        sourceListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            showGitSourceInfo(newValue);
        });
        GitSaveService gitSaveService = (GitSaveService)SaveServiceFactory.getService(GitSaveService.SERVICE_ID);
        sourceListView.getItems().addAll(gitSaveService.getRepos());

        // EVENT HANDLING INITIALIZATION
        eventBus = EventProcessor.getInstance().getBus();
    }

    private void showGitSourceInfo(GitRepo repo) {
        // will trigger onSourceServiceSelected
        sourceServiceCombo.getSelectionModel().select(GitSaveService.SERVICE_ID);

        gitSourceNameTextField.setText(repo.getName());
        gitSourceUriTextField.setText(repo.getRepoUri().toString());
        gitSourceTokenTextField.setText(repo.getToken());
    }

    private String getGameInfoText(Game game) {
        return "Name: " + game.getName() + "\n\n" +
                "Executable: " + game.getExecutable().getAbsolutePath() + "\n" +
                "Save Directory: " + game.getSaveConfiguration().getGameSaveDirectory().getAbsolutePath() + "\n\n" +
                "Save Service ID: " + game.getSaveConfiguration().getSaveServiceId() + "\n" +
                "Save Service Reference: " + game.getSaveConfiguration().getRemoteSaveUuid() + "\n";
    }

    /**
     * SOURCE TAB - i have no idea yet how to best split this logic into a separate class
     */
    @FXML
    private void onSourceServiceSelected() {
        // will eventually have to also hide other source forms
        gitSourceBox.setVisible(GitSaveService.SERVICE_ID.equals(sourceServiceCombo.getValue()));
        gitSourceBox.setDisable(!GitSaveService.SERVICE_ID.equals(sourceServiceCombo.getValue()));
    }

    @FXML
    private void onAddSource() {
        if (GitSaveService.SERVICE_ID.equals(sourceServiceCombo.getValue())) {
            // handle add git repo
            GitRepo repo = getRepoFromInput();
            if (repo != null) {
                eventBus.registerEvent(new AppWorkingEvent("Add Source", AppWorkingEvent.Status.WORKING, mainBox));
                eventBus.registerEvent(new ValidateGitRepoEvent(validationResult -> {
                    if (validationResult.isSuccess()) {
                        try {
                            GitSaveService gitSaveService = (GitSaveService) SaveServiceFactory.getService(GitSaveService.SERVICE_ID);
                            gitSaveService.addRepo(repo);
                            Platform.runLater(() -> {
                                sourceListView.getItems().add(repo);
                                sourceListView.getSelectionModel().select(repo);
                            });
                        } catch (IOException | GitAPIException e) {
                            setErrorText("Exception encountered trying to add GitRepo: " + e.getMessage());
                        }
                    } else {
                        setErrorText(validationResult.getReason());
                    }
                    return null;
                }, repo));
                eventBus.registerEvent(new AppWorkingEvent("Add Source", AppWorkingEvent.Status.COMPLETE, mainBox));
            }
        }
    }

    private GitRepo getRepoFromInput() {
        GitRepo repo = new GitRepo();
        try {
            repo.setName(gitSourceNameTextField.getText());
            repo.setRepoUri(URI.create(gitSourceUriTextField.getText()));
            repo.setToken(gitSourceTokenTextField.getText());
            return repo;
        } catch (Exception e) {
            setErrorText(e.getMessage());
        }
        return null;
    }


    /**
     * END SOURCE TAB
     */
}
