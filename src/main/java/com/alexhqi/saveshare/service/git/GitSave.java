package com.alexhqi.saveshare.service.git;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Objects;
import java.util.UUID;

public class GitSave {

    private UUID uuid;
    private String name;
    private String repoPath;

    @JsonIgnore
    private String repoName;

    public GitSave() {
        uuid = UUID.randomUUID();
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRepoPath() {
        return repoPath;
    }

    public void setRepoPath(String repoPath) {
        this.repoPath = repoPath;
    }

    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(String repoName) {
        this.repoName = repoName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitSave gitSave = (GitSave) o;
        return Objects.equals(uuid, gitSave.uuid) &&
                Objects.equals(name, gitSave.name) &&
                Objects.equals(repoPath, gitSave.repoPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, repoPath);
    }
}
