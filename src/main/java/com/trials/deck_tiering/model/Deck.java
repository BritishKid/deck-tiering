package com.trials.deck_tiering.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Deck {
    private String id;
    private String name;
    private int rating;
    private String owner;
    private String cardList;
    private int tier;
    private String game;
    private String historyOutcome;

    public Deck(String id, String name, int rating, String owner, String cardList, int tier, String game) {
        this.id = id;
        this.name = name;
        this.rating = rating;
        this.owner = owner;
        this.cardList = cardList;
        this.tier = tier;
        this.game = game;
    }
}
