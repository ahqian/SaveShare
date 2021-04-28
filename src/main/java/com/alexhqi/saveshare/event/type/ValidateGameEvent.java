package com.alexhqi.saveshare.event.type;

import com.alexhqi.saveshare.core.Game;
import com.alexhqi.saveshare.validation.ValidationResult;

import java.util.function.Function;

public class ValidateGameEvent extends CallbackEvent<ValidationResult> {

    public static final String TYPE = "VALIDATE_GAME_EVENT";

    private final Game game;

    public ValidateGameEvent(Function<ValidationResult, Void> callback, Game game) {
        super(TYPE, callback);
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}
