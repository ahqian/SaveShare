package com.alexhqi.saveshare.event.handler;

import com.alexhqi.saveshare.event.type.ValidateGitRepoEvent;
import com.alexhqi.saveshare.service.git.GitRepo;
import com.alexhqi.saveshare.event.SuccessResult;

public class ValidateGitRepoHandler extends CallbackHandler<ValidateGitRepoEvent, SuccessResult> {

    public ValidateGitRepoHandler() {
        super(ValidateGitRepoEvent.class);
    }

    @Override
    protected SuccessResult handleEventForResult(ValidateGitRepoEvent event) {
        GitRepo repo = event.getRepo();
        // todo.. validate repo exists at given location and token is valid
        return new SuccessResult(true);
    }

    @Override
    public String getHandlerType() {
        return ValidateGitRepoEvent.TYPE;
    }
}
