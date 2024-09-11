package com.trials.deck_tiering.model;

import java.util.Comparator;

public class DeckComprator implements Comparator<Deck> {

    @Override
    public int compare(Deck a, Deck b) {
        return b.getRating() - a.getRating();
    }
}
