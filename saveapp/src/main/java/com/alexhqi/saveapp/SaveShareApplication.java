package com.alexhqi.saveapp;

import com.alexhqi.saveapp.core.GameManager;
import com.alexhqi.saveapp.service.SaveServiceFactory;
import com.alexhqi.saveapp.service.git.GitSaveService;
import com.alexhqi.saveapp.ui.ConsoleUI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SaveShareApplication {

	/*
	TASKS:
	- Game configuration edit capabilities
	- Repo integrity checks on start
	- Self "cleaning" functionality, attempt to fix bad config or remove them
	- Fix logging configuration
	- Various cleanups and logic refactoring
	- Proper automation tests where applicable
	 */

	private static final Logger LOGGER = LogManager.getLogger(SaveShareApplication.class);

	public static void main(String[] args) {
		try {
			GameManager gameManager = new GameManager();
			ConsoleUI ui = new ConsoleUI(gameManager, (GitSaveService)SaveServiceFactory.getService(GitSaveService.SERVICE_ID));
			ui.start();
		} catch (Exception e) {
			LOGGER.error("Exception encountered. Exiting.", e);
		}

	}

}
