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
    private double ratingDeviation;
    private double volatility;
    private String owner;
    private String cardList; // deckKey
    private int tier;
    private String game;
    private int gamesPlayed;
    private String historyOutcome;

    public static Deck createNewDeck(
            String id,
            String name,
            String owner,
            String deckKey,
            String game
    ) {
        Deck deck = new Deck();
        deck.setId(id);
        deck.setName(name);
        deck.setRating(1500);
        deck.setRatingDeviation(350.0);
        deck.setVolatility(0.06);
        deck.setOwner(owner);
        deck.setCardList(deckKey);
        deck.setTier(3);
        deck.setGame(game);
        deck.setGamesPlayed(0);
        return deck;
    }
}
