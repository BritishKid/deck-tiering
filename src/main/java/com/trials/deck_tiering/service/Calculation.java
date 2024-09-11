package com.trials.deck_tiering.service;

import com.trials.deck_tiering.model.Deck;

import java.util.ArrayList;
import java.util.List;

public class Calculation {

    public List<Deck> deckRatingCalculation(List<Deck> winningDecks, List<Deck> losingDecks) {
        List<Deck> updatedList = new ArrayList<>();

        //protected for multi matches later
        float winnerAverage = getAverageScore(winningDecks);
        float loserAverage = getAverageScore(losingDecks);

        float probabilityWinner = Probability(winnerAverage, loserAverage);
        float probabilityLoser = Probability(loserAverage, winnerAverage);

        float winnerChange = WinnerEloRatingChange(winnerAverage, probabilityWinner);
        for (Deck deck : winningDecks) {
            deck.setRating((int) (deck.getRating() + winnerChange));
            updatedList.add(deck);
        }

        float loserChange = LoserEloRatingChange(loserAverage, probabilityLoser);
        for (Deck deck: losingDecks) {
            deck.setRating((int) (deck.getRating() - loserChange));
            updatedList.add(deck);
        }

        return updatedList;

    }

    private float LoserEloRatingChange(float loserAverage, float probabilityLoser) {
        float newLoserAverage = loserAverage + 30 * (0 - probabilityLoser);
        return Math.abs(newLoserAverage - loserAverage);
    }

    private float WinnerEloRatingChange(float winnerAverage, float probabilityWinner) {

        float newWinnerAverage = winnerAverage + 30 * (1 - probabilityWinner);
        return Math.abs(newWinnerAverage - winnerAverage);
    }


    private float Probability(float rating1,float rating2) {
        return 1.0f * 1.0f / (1 + 1.0f * (float)(Math.pow(10, 1.0f * (rating1 - rating2) / 400)));
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
