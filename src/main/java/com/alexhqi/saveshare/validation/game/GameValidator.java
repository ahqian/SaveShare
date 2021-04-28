package com.alexhqi.saveshare.validation.game;

import com.alexhqi.saveshare.core.Game;
import com.alexhqi.saveshare.service.RemoteSaveService;
import com.alexhqi.saveshare.service.SaveServiceFactory;
import com.alexhqi.saveshare.validation.ValidationResult;
import com.alexhqi.saveshare.validation.Validator;

import java.io.File;
import java.util.StringJoiner;

public class GameValidator implements Validator<Game> {

    @Override
    public ValidationResult validate(Game game) {
        StringJoiner problemBuilder = new StringJoiner("\n");

        // validate executable exists
        if (!game.getExecutable().exists() || !game.getExecutable().isFile()) {
            problemBuilder.add("The given executable doesn't point to a file on disk.");
        }

        // validate save directory exists
        if (!game.getSaveConfiguration().getGameSaveDirectory().exists() || !game.getSaveConfiguration().getGameSaveDirectory().isDirectory()) {
            problemBuilder.add("The given save directory path does not point to a directory on disk.");
        }

        try {
            // validate save service id is correct
            RemoteSaveService remoteSaveService = SaveServiceFactory.getService(game.getSaveConfiguration().getSaveServiceId());

            // validate a matching save reference exists with the given save service
            File file = remoteSaveService.getSaveWithId(game.getSaveConfiguration().getRemoteSaveUuid());
            if (file == null) {
                problemBuilder.add("No remote save could be found with reference " + game.getSaveConfiguration().getRemoteSaveUuid());
            }
        } catch (IllegalArgumentException e) {
            problemBuilder.add("No service could be found with id " + game.getSaveConfiguration().getSaveServiceId());
        }

        return new ValidationResult(problemBuilder.length() == 0, problemBuilder.toString());
    }
}
