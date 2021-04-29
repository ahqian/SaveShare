package com.alexhqi.saveshare;

import com.alexhqi.saveshare.dependency.ScuffedServiceContext;
import com.alexhqi.saveshare.event.EventBus;
import com.alexhqi.saveshare.event.EventHandlerFactory;
import com.alexhqi.saveshare.event.EventProcessor;
import com.alexhqi.saveshare.event.QueuedEventBus;
import com.alexhqi.saveshare.event.handler.*;
import com.alexhqi.saveshare.core.GameManager;
import com.alexhqi.saveshare.service.SaveServiceFactory;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("primary"));
        stage.setScene(scene);
        stage.setOnCloseRequest((event) -> {
            EventProcessor.getInstance().finishProcessing();
            Platform.exit();
            System.exit(0);
        });
        stage.show();
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        try {
            // this seems a bit all over the place.
            SaveServiceFactory.initializeServices();
            ScuffedServiceContext.registerInstance(GameManager.class, new GameManager());

            EventBus eventBus = new QueuedEventBus();
            EventProcessor.getInstance().registerEventBus(eventBus);
            EventProcessor.getInstance().startProcessing();

            EventHandlerFactory.registerHandler(new AppWorkingHandler());
            EventHandlerFactory.registerHandler(new StartGameHandler(ScuffedServiceContext.getInstance(GameManager.class)));
            EventHandlerFactory.registerHandler(new ValidateGameHandler());
            EventHandlerFactory.registerHandler(new ValidateGitRepoHandler());
            EventHandlerFactory.registerHandler(new DeleteSourceHandler());
            launch();
        } catch (IOException e) {
            // should log
        }

    }

}