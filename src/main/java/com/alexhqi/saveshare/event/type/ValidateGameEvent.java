package com.alexhqi.saveshare.event.type;

import com.alexhqi.saveshare.core.Game;
import com.alexhqi.saveshare.event.SuccessResult;

import java.util.function.Function;

public class ValidateGameEvent extends CallbackEvent<SuccessResult> {

    public static final String TYPE = "VALIDATE_GAME_EVENT";

    private final Game game;

    public ValidateGameEvent(Function<SuccessResult, Void> callback, Game game) {
        super(TYPE, callback);
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}
