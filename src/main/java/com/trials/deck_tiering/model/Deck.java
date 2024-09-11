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
}
