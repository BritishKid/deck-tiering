package com.trials.deck_tiering.service;

import com.trials.deck_tiering.dao.DeckDao;
import com.trials.deck_tiering.model.Deck;
import com.trials.deck_tiering.model.DeckComprator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DeckService {

    @Autowired
    private DeckDao deckDao;

    //manip data from reads

    public List<Deck> getAllUniqueDecks() {
        Map<String, Deck> mapDeckList = deckDao.getDeckList();
        return new ArrayList<Deck>(mapDeckList.values());
    }

    public void updateDeckRatings(String[] winners, String[] losers) {

        List<Deck> winnerDecks = new ArrayList<>();
        List<Deck> loserDecks = new ArrayList<>();

        //get winner deck information
        for (String winner : winners) {
            List<Deck> deckHistory = getDeckInformation(winner); //get history of the deck
            int latestDeckData = deckHistory.size() - 1;
            winnerDecks.add(deckHistory.get(latestDeckData));
        }

        //get loser deck information
        for (String loser : losers) {
            List<Deck> deckHistory = getDeckInformation(loser); //get history of the deck
            int latestDeckData = deckHistory.size() - 1;
            loserDecks.add(deckHistory.get(latestDeckData));
        }

        //calculate new ratings
        Calculation calculation = new Calculation();

        //update csv
        List<Deck> updatedList = calculation.deckRatingCalculation(winnerDecks, loserDecks);
        writeDecks(updatedList);

    }

    public void writeDecks (List<Deck> decks) {
        deckDao.writeDecks(decks);
    }

    private List<Deck> getDeckInformation(String deckId) {

        return deckDao.getDeckInformationFromId(deckId);
    }

    public List<Deck> orderDeckByRating(List<Deck> allUniqueDecks) {
        allUniqueDecks.sort(new DeckComprator());

        return allUniqueDecks;
    }

    public List<Deck> filterPlayersDecks(List<Deck> allUniqueDecks, String owner) {
        return allUniqueDecks.stream().filter(deck -> deck.getOwner().equals(owner)).collect(Collectors.toList());
    }
}
