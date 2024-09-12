package com.trials.deck_tiering.model;

import java.util.ArrayList;
import java.util.List;

public enum GameEnum {
    POKEMON("Pokemon"),
    YUGIOH("Yu-Gi-Oh"),
    COMMANDER("Commander"),
    MAGIC("Magic the Gathering");

    private String name;

    GameEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static List<String> getGameList() {
        List<String> gameList = new ArrayList<>();

        gameList.add(POKEMON.getName());
        gameList.add(YUGIOH.getName());
        gameList.add(COMMANDER.getName());
        gameList.add(MAGIC.getName());

        return gameList;
    }
}
