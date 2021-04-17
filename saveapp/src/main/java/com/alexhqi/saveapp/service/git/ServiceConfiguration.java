package com.alexhqi.saveapp.service.git;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.List;

public class ServiceConfiguration {
    private List<GitRepo> repos = new ArrayList<>();

    @JsonIgnore
    private List<GitSave> saves = new ArrayList<>();

    public List<GitRepo> getRepos() {
        return repos;
    }

    public void setRepos(List<GitRepo> repos) {
        this.repos = repos;
    }

    public List<GitSave> getSaves() {
        return saves;
    }

    public void setSaves(List<GitSave> saves) {
        this.saves = saves;
    }
}
