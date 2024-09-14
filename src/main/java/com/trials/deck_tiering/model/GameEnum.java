package com.trials.deck_tiering.model;

import java.util.ArrayList;
import java.util.List;

public enum GameEnum {
    POKEMON("Pokemon"),
    POKEMONFFA("Pokemon FFA"),
    POKEMONTEAM("Pokemon Team"),
    YUGIOH("Yu-Gi-Oh"),
    COMMANDER("Commander"),
    MAGIC("Magic the Gathering"),
    YUGIOHTEAM("Yu-Gi-Oh Team"),
    YUGIOHBR("Yu-Gi-Oh FFA"),
    YUGIOHTAG("Yu-Gi-Oh Tag Team");

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
        gameList.add(POKEMONFFA.getName());
        gameList.add(POKEMONTEAM.getName());
        gameList.add(YUGIOH.getName());
        gameList.add(COMMANDER.getName());
        gameList.add(MAGIC.getName());
        gameList.add(YUGIOHBR.getName());
        gameList.add(YUGIOHTEAM.getName());
        gameList.add(YUGIOHTAG.getName());

        return gameList;
    }

    public static List<String> getMainGameList() {
        List<String> gameList = new ArrayList<>();

        gameList.add(POKEMON.getName());
        gameList.add(YUGIOH.getName());
        gameList.add(MAGIC.getName());
        gameList.add(COMMANDER.getName());

        return gameList;
    }

    public static List<String> getPokemonGameList() {
        List<String> gameList = new ArrayList<>();

        gameList.add(POKEMON.getName());
        gameList.add(POKEMONFFA.getName());
        gameList.add(POKEMONTEAM.getName());

        return gameList;
    }

    public static List<String> getYugiohGameList() {
        List<String> gameList = new ArrayList<>();

        gameList.add(YUGIOH.getName());
        gameList.add(YUGIOHBR.getName());
        gameList.add(YUGIOHTEAM.getName());
        gameList.add(YUGIOHTAG.getName());

        return gameList;
    }


    public static List<String> getMtgGameList() {
        List<String> gameList = new ArrayList<>();

        gameList.add(MAGIC.getName());
        gameList.add(COMMANDER.getName());

        return gameList;
    }
}
