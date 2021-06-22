package com.alexhqi.saveshare.service.git;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceConfiguration {

    private List<GitRepo> repos = new ArrayList<>();

    @JsonIgnore
    private final Map<String, GitRepo> repoMap = new HashMap<>();

    @JsonIgnore
    private List<GitSave> saves = new ArrayList<>();

    public List<GitRepo> getRepos() {
        return repos;
    }

    public GitRepo getRepoWithName(String name) {
        return repoMap.get(name);
    }

    public void setRepos(List<GitRepo> repos) {
        this.repos = repos;
        repoMap.clear();
        repos.forEach(repo -> repoMap.put(repo.getName(), repo));
    }

    public List<GitSave> getSaves() {
        return saves;
    }

    public void setSaves(List<GitSave> saves) {
        this.saves = saves;
    }
}
