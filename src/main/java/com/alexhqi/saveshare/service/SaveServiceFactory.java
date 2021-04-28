package com.alexhqi.saveshare.service;

import com.alexhqi.saveshare.service.git.GitSaveService;

import java.util.Collections;
import java.util.List;

public abstract class SaveServiceFactory {

    private static final GitSaveService gitSaveService = new GitSaveService();

    static {
        initializeServices();
    }

    public static void initializeServices() {
        try {
            gitSaveService.initialize();
        } catch (Exception e) {
            throw new IllegalStateException("Exception encountered initializing services.", e);
        }
    }

    public static RemoteSaveService getService(String id) {
        switch (id) {
            case GitSaveService.SERVICE_ID:
                return gitSaveService;
            default:
                throw new IllegalArgumentException("Unknown service id: " + id);
        }
    }

    public static List<RemoteSaveService> getServices() {
        return Collections.singletonList(gitSaveService);
    }
}
