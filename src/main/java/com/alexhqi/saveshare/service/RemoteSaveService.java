package com.alexhqi.saveshare.service;

import com.alexhqi.saveshare.core.Save;

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * This is intended to cover the common methods all RemoteSaveService implementations must fulfill
 * in order to facilitate the core functions of the GameManager (performing the symlinks and save file management when
 * playing a game).
 * Configuration of the service, saves, and games, are all context/impl + UI implementation dependent
 * and as such are left to direct relationships between UI and Service classes.
 */
public interface RemoteSaveService {

    String getId();
    boolean initialize() throws Exception;
    File getSaveWithId(UUID remoteSaveUuid);
    void updateSaveWithId(UUID remoteSaveUuid);
    List<Save> getAllSaves();

}
