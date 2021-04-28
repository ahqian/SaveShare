package com.alexhqi.saveshare.event.handler;

import com.alexhqi.saveshare.core.Game;
import com.alexhqi.saveshare.event.type.ValidateGameEvent;
import com.alexhqi.saveshare.validation.ValidationResult;
import com.alexhqi.saveshare.validation.Validator;
import com.alexhqi.saveshare.validation.game.GameValidator;

public class ValidateGameHandler extends CallbackHandler<ValidateGameEvent, ValidationResult> {

    public ValidateGameHandler() {
        super(ValidateGameEvent.class);
    }

    @Override
    protected ValidationResult handleEventForResult(ValidateGameEvent event) {
        Validator<Game> validator = new GameValidator();
        return validator.validate(event.getGame());
    }

    @Override
    public String getHandlerType() {
        return ValidateGameEvent.TYPE;
    }
}
