package com.alexhqi.jfx.event.handler;

import com.alexhqi.jfx.event.type.StartGameEvent;
import com.alexhqi.saveapp.core.GameManager;

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
