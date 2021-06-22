package com.alexhqi.saveshare;

import com.alexhqi.saveshare.core.Game;
import com.alexhqi.saveshare.core.GameManager;
import com.alexhqi.saveshare.core.Save;
import com.alexhqi.saveshare.core.SaveConfiguration;
import com.alexhqi.saveshare.dependency.ScuffedServiceContext;
import com.alexhqi.saveshare.event.Event;
import com.alexhqi.saveshare.event.EventBus;
import com.alexhqi.saveshare.event.EventProcessor;
import com.alexhqi.saveshare.event.type.*;
import com.alexhqi.saveshare.service.RemoteSaveService;
import com.alexhqi.saveshare.service.SaveServiceFactory;
import com.alexhqi.saveshare.service.git.GitRepo;
import com.alexhqi.saveshare.service.git.GitSave;
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
import java.util.stream.Collectors;

public class MainController implements Initializable {

    public TextFlow errorTextArea;
    public VBox mainBox;

    // todo ideally all these tabs are broken out of this mega class
    public ListView<Game> gameListView;
    public TextArea gameInfoPane;
    public TextField gameNameTestField;
    public TextField executableText;
    public TextField saveDirText;
    public ComboBox<String> gameListSaveServiceCombo;
    public ComboBox<UUID> referenceCombo;

    public ListView<GitRepo> sourceListView;
    public ComboBox<String> sourceServiceCombo;

    public VBox sourceGitContainer;
    public TextField gitSourceNameTextField;
    public TextField gitSourceUriTextField;
    public TextField gitSourceTokenTextField;

    public ComboBox<String> savesListSaveServiceCombo;
    public ListView<Save> saveListView;
    public TextField savesReferenceText;

    public VBox savesGitContainer;
    public ComboBox<GitRepo> gitSaveRepoCombo;
    public TextField gitSaveNameTextField;
    public TextField gitSaveUploadTextField;

    private GameManager gameManager;
    private EventBus eventBus;

    @FXML
    private void onServiceSelected(ActionEvent event) {
        // todo this whole service selection could be removed with better save reference management for cleaner ui
        try {
            RemoteSaveService service = SaveServiceFactory.getService(gameListSaveServiceCombo.getValue());
            referenceCombo.getItems().clear();
            referenceCombo.getItems().addAll(service.getAllSaves().stream()
                    .map(Save::getReference)
                    .collect(Collectors.toList())
            );
        } catch (IllegalArgumentException e) {
            setErrorText("Could not find internal Remote Save Service with id: "+ gameListSaveServiceCombo.getValue());
        }
    }

    @FXML
    private void onAddGame() {
        Game game = getGameFromInput();
        fireWorkEvent("Validate Game", new ValidateGameEvent(validationResult -> {
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
    }

    private Game getGameFromInput() {
        Game game = new Game();
        game.setName(gameNameTestField.getText());
        game.setExecutable(new File(executableText.getText()));
        SaveConfiguration saveConfiguration = new SaveConfiguration();
        saveConfiguration.setSaveServiceId(gameListSaveServiceCombo.getValue());
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
            fireWorkEvent("Play Game", new StartGameEvent(game));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gameManager = ScuffedServiceContext.getInstance(GameManager.class);
        gameManager.getGames().forEach(game -> gameListView.getItems().add(game));

        // SAVE SERVICE INITIALIZATION
        SaveServiceFactory.getServices().forEach(service -> {
            gameListSaveServiceCombo.getItems().add(service.getId());
            sourceServiceCombo.getItems().add(service.getId());
            savesListSaveServiceCombo.getItems().add(service.getId());
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
            gameNameTestField.setText(newValue.getName());
            executableText.setText(newValue.getExecutable().getAbsolutePath());
            saveDirText.setText(newValue.getSaveConfiguration().getGameSaveDirectory().getAbsolutePath());
            gameListSaveServiceCombo.getSelectionModel().clearAndSelect(
                    gameListSaveServiceCombo.getItems().indexOf(newValue.getSaveConfiguration().getSaveServiceId())
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
        gitSaveRepoCombo.getItems().addAll(gitSaveService.getRepos());

        // SAVES LIST
        saveListView.setCellFactory((param -> new ListCell<>() {
            @Override
            protected void updateItem(Save item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.getReference() == null) {
                    setText(null);
                } else {
                    setText(item.getReference().toString());
                }
            }
        }));
        saveListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        saveListView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
            showSaveInfo(newValue);
        });

        // EVENT HANDLING INITIALIZATION
        eventBus = EventProcessor.getInstance().getBus();
    }

    private void showSaveInfo(Save save) {
        if (save != null) {
            savesReferenceText.setText(save.getReference().toString());
            if (GitSaveService.SERVICE_ID.equals(save.getServiceId())) {
                GitSaveService gitSaveService = (GitSaveService) SaveServiceFactory.getService(GitSaveService.SERVICE_ID);
                GitRepo repo = gitSaveService.getRepoForSaveReference(save.getReference());
                GitSave gitSave = gitSaveService.getGitSaveWithId(save.getReference());
                if (repo == null) {
                    setErrorText("Could not find GitRepo for Save with reference " + save.getReference());
                    return;
                }
                if (gitSave == null) {
                    setErrorText("Could not find GitSave with reference " + save.getReference());
                    return;
                }
                gitSaveRepoCombo.getSelectionModel().select(repo);
                gitSaveNameTextField.setText(gitSave.getName());
            }
        } else {
            savesReferenceText.clear();
            gitSaveRepoCombo.getSelectionModel().select(null);
            gitSaveNameTextField.clear();
        }
        gitSaveUploadTextField.clear();
    }

    private void showGitSourceInfo(GitRepo repo) {
        if (repo != null) {
            // will trigger onSourceServiceSelected
            sourceServiceCombo.getSelectionModel().select(GitSaveService.SERVICE_ID);

            gitSourceNameTextField.setText(repo.getName());
            gitSourceUriTextField.setText(repo.getRepoUri().toString());
            gitSourceTokenTextField.setText(repo.getToken());
        } else {
            gitSourceNameTextField.clear();
            gitSourceUriTextField.clear();
            gitSourceTokenTextField.clear();
        }
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
        sourceGitContainer.setVisible(GitSaveService.SERVICE_ID.equals(sourceServiceCombo.getValue()));
        sourceGitContainer.setDisable(!GitSaveService.SERVICE_ID.equals(sourceServiceCombo.getValue()));
    }

    @FXML
    private void onAddSource() {
        if (GitSaveService.SERVICE_ID.equals(sourceServiceCombo.getValue())) {
            // handle add git repo
            GitRepo repo = getRepoFromInput();
            if (repo != null) {
                fireWorkEvent("Add Source", new ValidateGitRepoEvent(validationResult -> {
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
            }
        }
    }

    @FXML
    private void onDeleteSource() {
        GitRepo repo = sourceListView.getSelectionModel().getSelectedItem();
        if (repo != null) {
            fireWorkEvent("Delete Source", new DeleteSourceEvent(successResult -> {
                if (successResult.isSuccess()) {
                    Platform.runLater(() -> {
                        sourceListView.getItems().remove(repo);
                        sourceListView.getSelectionModel().select(-1);
                    });
                } else {
                    setErrorText(successResult.getReason());
                }
                return null;
            }, repo));
        }
    }

    private void fireWorkEvent(String taskCode, Event event) {
        eventBus.registerEvent(new AppWorkingEvent(taskCode, AppWorkingEvent.Status.WORKING, mainBox));
        eventBus.registerEvent(event);
        eventBus.registerEvent(new AppWorkingEvent(taskCode, AppWorkingEvent.Status.COMPLETE, mainBox));
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


    /**
     * SAVES TAB
     */
    // this naming scheme hurts me
    @FXML
    private void onSaveTabServiceSelected() {
        try {
            RemoteSaveService service = SaveServiceFactory.getService(savesListSaveServiceCombo.getValue());
            saveListView.getItems().clear();
            saveListView.getItems().addAll(service.getAllSaves());
        } catch (IllegalArgumentException e) {
            setErrorText("Could not find internal Remote Save Service with id: " + savesListSaveServiceCombo.getValue());
        }
    }

    public void onUploadSave(ActionEvent actionEvent) {

    }
    /**
     * END SAVES TAB
     */
}
