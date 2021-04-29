package com.alexhqi.saveshare.event.handler;

import com.alexhqi.saveshare.core.Game;
import com.alexhqi.saveshare.event.type.ValidateGameEvent;
import com.alexhqi.saveshare.event.SuccessResult;
import com.alexhqi.saveshare.validation.Validator;
import com.alexhqi.saveshare.validation.impl.GameValidator;

public class ValidateGameHandler extends CallbackHandler<ValidateGameEvent, SuccessResult> {

    public ValidateGameHandler() {
        super(ValidateGameEvent.class);
    }

    @Override
    protected SuccessResult handleEventForResult(ValidateGameEvent event) {
        Validator<Game> validator = new GameValidator();
        return validator.validate(event.getGame());
    }

    @Override
    public String getHandlerType() {
        return ValidateGameEvent.TYPE;
    }
}
