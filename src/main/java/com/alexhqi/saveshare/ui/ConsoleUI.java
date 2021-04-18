package com.alexhqi.saveshare.ui;

import com.alexhqi.saveshare.core.Game;
import com.alexhqi.saveshare.core.GameManager;
import com.alexhqi.saveshare.core.SaveConfiguration;
import com.alexhqi.saveshare.service.git.GitRepo;
import com.alexhqi.saveshare.service.git.GitSave;
import com.alexhqi.saveshare.service.git.GitSaveService;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class ConsoleUI  {

    private final GameManager gameManager;
    private final GitSaveService gitSaveService;
    private Scanner scanner = new Scanner(System.in);

    private class CancelledInputException extends Exception {}

    public ConsoleUI(GameManager gameManager, GitSaveService gitSaveService) {
        this.gameManager = gameManager;
        this.gitSaveService = gitSaveService;
    }

    public void start() throws IOException, GitAPIException {
        while(true) {
            System.out.println("What would you like to do?");
            System.out.println("1. Show configured games.");
            System.out.println("2. Configure a new game.");
            System.out.println("3. Start Game.");
            System.out.println("4. Add remote source.");
            System.out.println("5. Remove remote source.");
            System.out.println("6. Upload Save File.");
            System.out.println("*. Exit.");
            while (true) {
                System.out.print("Input: ");
                try {
                    switch (Integer.parseInt(scanner.nextLine())) {
                        case 1:
                            clearScreen();
                            printGames(gameManager.getGames());
                            break;
                        case 2:
                            clearScreen();
                            Game newGame = requestGameInfo();
                            gameManager.addGame(newGame);
                            System.out.println("Successfully saved new Game configuration.");
                            break;
                        case 3:
                            clearScreen();
                            Game game = getGameToStart(gameManager.getGames());
                            if (game != null) {
                                gameManager.playGame(game);
                            }
                            break;
                        case 4:
                            clearScreen();
                            GitRepo repo = getRepoInfo();
                            if (repo != null) {
                                gitSaveService.addRepo(repo);
                            }
                            break;
                        case 5:
                            clearScreen();
                            String name = getRepoToRemove();
                            gitSaveService.removeRepo(name);
                            break;
                        case 6:
                            clearScreen();
                            File file = getFileToUpload();
                            GitRepo selection = getRepoSelection();
                            if (file != null && selection != null) {
                                gitSaveService.uploadSaveFile(file, selection);
                                System.out.println("Successfully uploaded file.");
                            }
                            break;
                        default:
                            return;
                    }
                    break;
                } catch (NumberFormatException e) {
                    return;
                } catch (CancelledInputException e) {
                    break;
                }
            }
        }
    }

    private GitRepo getRepoSelection() throws CancelledInputException {
        String input;
        GitRepo repo = null;
        List<GitRepo> repos = gitSaveService.getRepos();
        System.out.println("List of known repos:");
        for (int i = 0; i < repos.size(); i++) {
            System.out.println(i + ") " + repos.get(i).getName());
        }
        while (repo == null) {
            System.out.print("# Of Repo to Upload to (N to cancel): ");
            input = getInput(scanner);
            try {
                repo = repos.get(Integer.parseInt(input));
            } catch (NumberFormatException e) {
                System.out.println("That was not a number.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("That did not correspond to any of the listed repos.");
            }
        }
        return repo;
    }

    private File getFileToUpload() throws CancelledInputException {
        String input;
        File file = null;
        while (file == null || !file.exists()) {
            System.out.print("Path of file to upload (N to cancel): ");
            input = getInput(scanner);
            file = Paths.get(input).toFile();
        }
        return file;
    }

    public void printGames(List<Game> games) {
        for (int i = 0; i < games.size(); i++) {
            Game game = games.get(i);
            System.out.println("******************************");
            printGameConfiguration(game);
            System.out.println("******************************");
            if (i == games.size() - 1) {
                break;
            }
            System.out.println();
        }
    }

    public void printGameConfiguration(Game game) {
        System.out.println("Name: " + game.getName());
        System.out.println("Executable path: " + game.getExecutable().getAbsolutePath());
        System.out.println("Local Save Path: " + game.getSaveConfiguration().getGameSaveDirectory());
        System.out.println("Save Service Id: " + game.getSaveConfiguration().getSaveServiceId());
        System.out.println("Service Reference: " + game.getSaveConfiguration().getRemoteSaveUuid());
    }

    public Game requestGameInfo() throws CancelledInputException {
        Game game = new Game();
        String input;
        System.out.println("Please fill out the necessary config info. Type N to cancel at any time.");
        System.out.print("Input Game Name: ");
        input = getInput(scanner);
        game.setName(input);

        File executable = null;
        while (executable == null || !executable.exists()) {
            System.out.print("Executable path: ");
            input = getInput(scanner);
            executable = Paths.get(input).toFile();
            if (!executable.exists() || !executable.canExecute()) {
                System.out.println("No executable could be found with permission at the given path.");
            }
        }
        game.setExecutable(executable);

        SaveConfiguration saveConfiguration = new SaveConfiguration();
        System.out.print("Path to local save directory (the cloud save will be placed in here): ");
        input = getInput(scanner);
        saveConfiguration.setGameSaveDirectory(Paths.get(input).toFile());

        List<GitSave> gitSaves = gitSaveService.getSaves();
        System.out.println("List of known remote saves:");
        for (int i = 0; i < gitSaves.size(); i++) {
            System.out.println(i + ") " + gitSaves.get(i).getName());
        }
        GitSave gitSave = null;
        while (gitSave == null) {
            System.out.print("Save you wish to use (input just the number, N to cancel): ");
            input = getInput(scanner);
            try {
                gitSave = gitSaves.get(Integer.parseInt(input));
            } catch (NumberFormatException e) {
                System.out.println("That was not a number.");
            } catch (IndexOutOfBoundsException e) {
                System.out.println("That did not correspond to any of the listed saves.");
            }
        }
        saveConfiguration.setRemoteSaveUuid(gitSave.getUuid());
        saveConfiguration.setSaveServiceId(GitSaveService.SERVICE_ID);
        game.setSaveConfiguration(saveConfiguration);
        return game;
    }

    private String getInput(Scanner scanner) throws CancelledInputException {
        String input;
        input = scanner.nextLine();
        if (input.equalsIgnoreCase("n")) {
            throw new CancelledInputException();
        }
        return input;
    }

    public GitRepo getRepoInfo() throws CancelledInputException {
        GitRepo repo = new GitRepo();
        String input;
        System.out.println("Please fill out the necessary config info. Type N to cancel at any time.");
        while (true) {
            System.out.print("Repo uri: ");
            input = getInput(scanner);
            try {
                repo.setRepoUri(URI.create(input));
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("That was not a valid URI.");
            }
        }
        System.out.print("Access token: ");
        input = getInput(scanner);
        repo.setToken(input);
        System.out.print("Name this source (whatever you want): ");
        input = getInput(scanner);
        repo.setName(input);
        return repo;
    }

    public String getRepoToRemove() {
        System.out.print("Name of repo to remove: ");
        return scanner.nextLine();
    }

    public Game getGameToStart(List<Game> games) throws CancelledInputException {
        Game game;
        System.out.println("Games: " + games.stream().map(Game::getName).collect(Collectors.joining(", ")));
        while (true) {
            System.out.print("Game to play (N to cancel): ");
            String input = getInput(scanner);
            List<Game> selection = games.stream()
                    .filter((g) -> g.getName().equalsIgnoreCase(input))
                    .collect(Collectors.toList());
            if (selection.size() == 1) {
                game = selection.get(0);
                break;
            }
            System.out.println("That did not match any known game.");
        }
        return game;
    }

    private void clearScreen() {
//        System.out.print("\033[H\033[2J");
//        System.out.flush();
    }
}
