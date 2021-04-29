package com.alexhqi.saveshare.event.type;

import com.alexhqi.saveshare.event.SuccessResult;
import com.alexhqi.saveshare.service.git.GitRepo;

import java.util.function.Function;

public class DeleteSourceEvent extends CallbackEvent<SuccessResult> {

    public static final String TYPE = "DELETE_SOURCE_EVENT";

    // change to generic source interface
    private final GitRepo source;

    public DeleteSourceEvent(Function<SuccessResult, Void> callback, GitRepo source) {
        super(TYPE, callback);
        this.source = source;
    }

    public GitRepo getSource() {
        return source;
    }
}
