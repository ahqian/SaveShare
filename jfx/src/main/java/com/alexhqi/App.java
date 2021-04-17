package com.alexhqi;

import com.alexhqi.dependency.ScuffedServiceContext;
import com.alexhqi.event.EventBus;
import com.alexhqi.event.EventHandlerFactory;
import com.alexhqi.event.EventProcessor;
import com.alexhqi.event.QueuedEventBus;
import com.alexhqi.event.handler.AppWorkingHandler;
import com.alexhqi.event.handler.StartGameHandler;
import com.alexhqi.saveapp.core.GameManager;
import com.alexhqi.saveapp.service.SaveServiceFactory;
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
        scene = new Scene(loadFXML("primary"), 640, 480);
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
            launch();
        } catch (IOException e) {
            // should log
        }

    }

}