package com.alexhqi.saveshare.event.handler;

import com.alexhqi.saveshare.event.type.ValidateGitRepoEvent;
import com.alexhqi.saveshare.service.git.GitRepo;
import com.alexhqi.saveshare.validation.ValidationResult;

public class ValidateGitRepoHandler extends CallbackHandler<ValidateGitRepoEvent, ValidationResult> {

    public ValidateGitRepoHandler() {
        super(ValidateGitRepoEvent.class);
    }

    @Override
    protected ValidationResult handleEventForResult(ValidateGitRepoEvent event) {
        GitRepo repo = event.getRepo();
        // todo.. validate repo exists at given location and token is valid
        return new ValidationResult(true);
    }

    @Override
    public String getHandlerType() {
        return ValidateGitRepoEvent.TYPE;
    }
}
