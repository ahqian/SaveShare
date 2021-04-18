package com.alexhqi.saveshare.core;

import java.util.List;

public class Configuration {
    private List<Game> games;

    public Configuration() {
    }

    public Configuration(List<Game> games) {
        this.games = games;
    }

    public List<Game> getGames() {
        return games;
    }

    public void setGames(List<Game> games) {
        this.games = games;
    }

    public void addGame(Game game) {
        this.games.add(game);
    }

    public void removeGame(Game game) {

    }
}
