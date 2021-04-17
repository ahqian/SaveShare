package com.alexhqi.saveapp.core;

import com.alexhqi.saveapp.service.RemoteSaveService;
import com.alexhqi.saveapp.service.SaveServiceFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class GameManager {

    private static final String DATA_DIRECTORY = ".saveshare";
    private static final String CONFIG_FILE = "configuration.txt";
    private static final Logger LOGGER = LogManager.getLogger(GameManager.class);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Configuration configuration;
    private File configFile;

    public GameManager() throws IOException {
        initialize();
    }

    public void initialize() throws IOException {
        LOGGER.info("Initializing GameManager.");
        configFile = getConfigFile();
        StringBuilder sb = new StringBuilder();
        Files.readAllLines(configFile.toPath()).forEach(sb::append);

        if (sb.length() > 0) {
            ObjectReader reader = objectMapper.readerFor(Configuration.class);
            configuration = reader.readValue(sb.toString());
        } else {
            configuration = new Configuration(new ArrayList<>());
        }
        LOGGER.info("Configuration loaded. Initialization complete.");
    }

    private File getConfigFile() throws IOException {
        LOGGER.info("Seeking configuration file.");
        String homeDirectory = System.getenv("APPDATA");
        Path pathToConfigFile = Paths.get(homeDirectory);
        pathToConfigFile = pathToConfigFile.resolve(DATA_DIRECTORY);
        if (!pathToConfigFile.toFile().exists() && !pathToConfigFile.toFile().mkdirs()) {
            throw new IllegalStateException("Failed to create directories necessary for config file placement in " + pathToConfigFile.toString());
        }
        pathToConfigFile = pathToConfigFile.resolve(CONFIG_FILE);

        File configFile = new File(pathToConfigFile.toUri());
        if (!configFile.exists()) {
            LOGGER.info("No existing configuration found. Generating in {}", pathToConfigFile.toString());
            if (!configFile.createNewFile()) {
                throw new IllegalStateException("Failed to create new configuration file.");
            }
        }

        return configFile;
    }

    public void addGame(Game game) throws IOException {
        configuration.addGame(game);
        Files.write(configFile.toPath(), objectMapper.writerFor(Configuration.class).writeValueAsString(configuration).getBytes());
    }

    public List<Game> getGames() {
        return new ArrayList<>(configuration.getGames());
    }

    public void playGame(Game game) {
        SaveConfiguration saveConfiguration = game.getSaveConfiguration();
        RemoteSaveService saveService = SaveServiceFactory.getService(saveConfiguration.getSaveServiceId());

        File repoSave = saveService.getSaveWithId(saveConfiguration.getRemoteSaveUuid());
        if (repoSave == null) {
            throw new IllegalStateException("The specified save " + saveConfiguration.getRemoteSaveUuid() + " could not be found by the RemoteSaveService.");
        }
        // this is kind of wonky
        Path localsave = game.getSaveConfiguration().getGameSaveDirectory().toPath().resolve(repoSave.getName());
        File localSaveBackup = localsave.getParent().resolve("saveShareBackup").toFile();

        // if existing save, back it up
        boolean existingSave = localsave.toFile().exists();
        if (existingSave && !localsave.toFile().renameTo(localSaveBackup)) {
            LOGGER.error("Failed to backup existing save.");
            return;
        }

        try {
            // symlink local save to cloud save
            Files.createSymbolicLink(localsave, repoSave.toPath());

            try {
                // launch game and wait for exit
                Process process = Runtime.getRuntime().exec(game.getExecutable().getAbsolutePath(), null, game.getExecutable().getParentFile());
                process.waitFor();
            } catch (InterruptedException e) {
                LOGGER.error("GameManager was interrupted while waiting for game to exit. Cleaning up.");
            }

            // remove symlink
            Files.delete(localsave);

            // restore existing save if it was there
            if (existingSave && !localSaveBackup.renameTo(localsave.toFile())) {
                LOGGER.error("Failed to restore save backup stored at {}. Please investigate.", localSaveBackup.toString());
            }

            // update remote save with local changes
            saveService.updateSaveWithId(saveConfiguration.getRemoteSaveUuid());

        } catch (Exception e) {
            LOGGER.error("Exception encountered managing saves. Likely requires manual fixing.", e);
        }
    }

}
