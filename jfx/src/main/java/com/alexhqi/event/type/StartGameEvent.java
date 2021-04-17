package com.alexhqi.event.type;

import com.alexhqi.saveapp.core.Game;

public class StartGameEvent extends BaseEvent {

    public static final String TYPE = "START_GAME_EVENT";

    private final Game game;

    public StartGameEvent(Game game) {
        super(TYPE);
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}
