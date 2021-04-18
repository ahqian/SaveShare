package com.alexhqi.saveshare.event.handler;

import com.alexhqi.saveshare.event.type.StartGameEvent;
import com.alexhqi.saveshare.core.GameManager;

public class StartGameHandler extends BaseTypedEventHandler<StartGameEvent> {

    private final GameManager gameManager;

    public StartGameHandler(GameManager gameManager) {
        super(StartGameEvent.class);
        this.gameManager = gameManager;
    }

    @Override
    public String getHandlerType() {
        return StartGameEvent.TYPE;
    }

    @Override
    protected void doHandle(StartGameEvent event) {
        gameManager.playGame(event.getGame());
    }

}
