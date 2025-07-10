package com.trials.deck_tiering.service;

import com.trials.deck_tiering.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class Calculation {

    private final int TIER_1_RATING = 2000;
    private final int TIER_2_RATING = 1750;
    private final int TIER_3_RATING = 1400;
    private final int TIER_4_RATING = 1000;
    private final int TIER_5_RATING = 750;

    public List<Deck> deckRatingCalculation(List<Deck> winningDecks, List<Deck> losingDecks, Boolean bestOf3) {
        List<Deck> updatedList = new ArrayList<>();
        int kFactor = 60;

        //protected for multi matches later
        float winnerAverage = getAverageScore(winningDecks);
        float loserAverage = getAverageScore(losingDecks);

        float probabilityWinner = Probability(winnerAverage, loserAverage);
        float probabilityLoser = Probability(loserAverage, winnerAverage);

        if(bestOf3) {
            kFactor = 100;
        }

        float winnerChange = WinnerEloRatingChange(winnerAverage, probabilityWinner, kFactor);
        for (Deck deck : winningDecks) {
            deck.setRating((int) (deck.getRating() + winnerChange));
            deck.setTier(deckTierCalculation(deck.getRating()));
            updatedList.add(deck);
        }

        float loserChange = LoserEloRatingChange(loserAverage, probabilityLoser, kFactor);
        for (Deck deck: losingDecks) {
            deck.setRating((int) (deck.getRating() - loserChange));
            deck.setTier(deckTierCalculation(deck.getRating()));
            updatedList.add(deck);
        }

        return updatedList;

    }

    private int deckTierCalculation(int rating) {
        if(rating >= TIER_1_RATING) return 1;
        if(rating >= TIER_2_RATING) return 2;
        if(rating >= TIER_3_RATING) return 3;
        if(rating >= TIER_4_RATING) return 4;
        if(rating >= TIER_5_RATING) return 5;
        return 6;
    }

    private float LoserEloRatingChange(float loserAverage, float probabilityLoser, int kFactor) {
        float newLoserAverage = loserAverage + kFactor * (0 - probabilityLoser);
        return Math.abs(newLoserAverage - loserAverage);
    }

    private float WinnerEloRatingChange(float winnerAverage, float probabilityWinner, int kFactor) {

        float newWinnerAverage = winnerAverage + kFactor * (1 - probabilityWinner);
        return Math.abs(newWinnerAverage - winnerAverage);
    }


    private float Probability(float rating1,float rating2) {
        return 1.0f * 1.0f / (1 + 1.0f * (float)(Math.pow(10, 1.0f * (rating2 - rating1) / 400)));
    }

    private float getAverageScore(List<Deck> decks) {
        float totalScore = 0;
        int count = 0;
        for (Deck deck: decks) {
            totalScore = totalScore + deck.getRating();
            count++;
        }
        return totalScore/count;
    }

}
