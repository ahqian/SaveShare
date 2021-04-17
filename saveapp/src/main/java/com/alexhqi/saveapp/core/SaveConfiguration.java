package com.alexhqi.saveapp.core;

import java.io.File;
import java.util.UUID;

public class SaveConfiguration {

    // todo this can be converted to a list alongside the saveServiceId
    private UUID remoteSaveUuid;
    private String saveServiceId;
    // private String saveServiceVersion;

    private File gameSaveDirectory;

    public SaveConfiguration() {
    }

    public SaveConfiguration(File gameSaveDirectory) {
        this.gameSaveDirectory = gameSaveDirectory;
    }

    public File getGameSaveDirectory() {
        return gameSaveDirectory;
    }

    public void setGameSaveDirectory(File gameSaveDirectory) {
        this.gameSaveDirectory = gameSaveDirectory;
    }

    public UUID getRemoteSaveUuid() {
        return remoteSaveUuid;
    }

    public void setRemoteSaveUuid(UUID remoteSaveUuid) {
        this.remoteSaveUuid = remoteSaveUuid;
    }

    public String getSaveServiceId() {
        return saveServiceId;
    }

    public void setSaveServiceId(String saveServiceId) {
        this.saveServiceId = saveServiceId;
    }
}
