package com.alexhqi.saveshare.core;

import java.io.File;
import java.util.Objects;

public class Game {

    private String name = "";

    private File executable;
    private SaveConfiguration saveConfiguration;

    public Game() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getExecutable() {
        return executable;
    }

    public void setExecutable(File executable) {
        this.executable = executable;
    }

    public SaveConfiguration getSaveConfiguration() {
        return saveConfiguration;
    }

    public void setSaveConfiguration(SaveConfiguration saveConfiguration) {
        this.saveConfiguration = saveConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Game game = (Game) o;
        return name.equals(game.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
