package com.alexhqi.saveshare.service.git;

import java.util.ArrayList;
import java.util.List;

public class RepoInfo {
    private List<GitSave> saves = new ArrayList<>();

    public List<GitSave> getSaves() {
        return saves;
    }

    public void setSaves(List<GitSave> saves) {
        this.saves = saves;
    }
}
