package com.alexhqi.saveshare.event.handler;

import com.alexhqi.saveshare.event.SuccessResult;
import com.alexhqi.saveshare.event.type.DeleteSourceEvent;
import com.alexhqi.saveshare.service.SaveServiceFactory;
import com.alexhqi.saveshare.service.git.GitRepo;
import com.alexhqi.saveshare.service.git.GitSaveService;

public class DeleteSourceHandler extends CallbackHandler<DeleteSourceEvent, SuccessResult> {

    public DeleteSourceHandler() {
        super(DeleteSourceEvent.class);
    }

    @Override
    protected SuccessResult handleEventForResult(DeleteSourceEvent event) {
        try {
            GitRepo source = event.getSource();
            GitSaveService gitSaveService = (GitSaveService) SaveServiceFactory.getService(GitSaveService.SERVICE_ID);
            gitSaveService.removeRepo(source.getName());
        } catch (Exception e) {
            return new SuccessResult(false, e);
        }
        return new SuccessResult(true);
    }

    @Override
    public String getHandlerType() {
        return DeleteSourceEvent.TYPE;
    }
}
