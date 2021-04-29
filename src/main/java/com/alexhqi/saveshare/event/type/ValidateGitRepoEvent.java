package com.alexhqi.saveshare.event.type;

import com.alexhqi.saveshare.service.git.GitRepo;
import com.alexhqi.saveshare.event.SuccessResult;

import java.util.function.Function;

public class ValidateGitRepoEvent extends CallbackEvent<SuccessResult> {

    public static final String TYPE = "VALIDATE_GIT_REPO_EVENT";

    private final GitRepo repo;

    public ValidateGitRepoEvent(Function<SuccessResult, Void> callback, GitRepo repo) {
        super(TYPE, callback);
        this.repo = repo;
    }

    public GitRepo getRepo() {
        return repo;
    }
}
