package com.alexhqi.saveshare.service.git;

import java.net.URI;
import java.util.Objects;

public class GitRepo {
    private String name;
    private URI repoUri;
    private String token;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public URI getRepoUri() {
        return repoUri;
    }

    public void setRepoUri(URI repoUri) {
        this.repoUri = repoUri;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GitRepo gitRepo = (GitRepo) o;
        return Objects.equals(name, gitRepo.name) &&
                Objects.equals(repoUri, gitRepo.repoUri) &&
                Objects.equals(token, gitRepo.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, repoUri, token);
    }

    @Override
    public String toString() {
        return name;
    }
}
