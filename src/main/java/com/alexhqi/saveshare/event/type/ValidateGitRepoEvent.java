package com.alexhqi.saveshare.event.type;

import com.alexhqi.saveshare.service.git.GitRepo;
import com.alexhqi.saveshare.validation.ValidationResult;

import java.util.function.Function;

public class ValidateGitRepoEvent extends CallbackEvent<ValidationResult> {

    public static final String TYPE = "VALIDATE_GIT_REPO_EVENT";

    private final GitRepo repo;

    public ValidateGitRepoEvent(Function<ValidationResult, Void> callback, GitRepo repo) {
        super(TYPE, callback);
        this.repo = repo;
    }

    public GitRepo getRepo() {
        return repo;
    }
}
