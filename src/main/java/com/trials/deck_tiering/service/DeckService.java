package com.trials.deck_tiering.service;

import com.trials.deck_tiering.dao.DeckDao;
import com.trials.deck_tiering.dao.DecklistDao;
import com.trials.deck_tiering.dao.HistoryDao;
import com.trials.deck_tiering.model.Card;
import com.trials.deck_tiering.model.Deck;
import com.trials.deck_tiering.model.DeckComprator;
import com.trials.deck_tiering.model.History;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class DeckService {

    @Autowired
    private DeckDao deckDao;

    @Autowired
    private HistoryDao historyDao;

    @Autowired
    private DecklistDao decklistDao;

    //manip data from reads

    public List<Deck> getAllUniqueDecks() {
        Map<String, Deck> mapDeckList = deckDao.getUniqueDeckList();
        return new ArrayList<Deck>(mapDeckList.values());
    }

    public void updateDeckRatings(String[] winners, String[] losers) {

        List<Deck> winnerDecks = new ArrayList<>();
        List<Deck> loserDecks = new ArrayList<>();

        //todo make history work for multi matches
        List<History> historyForWinners = new ArrayList<>();
        List<History> historyForLosers = new ArrayList<>();

        //get winner deck information
        for (String winner : winners) {
            List<Deck> deckHistory = getDeckInformation(winner); //get history of the deck
            int latestDeckData = deckHistory.size() - 1;
            Deck latestDeckInformation = deckHistory.get(latestDeckData);
            winnerDecks.add(latestDeckInformation);

            History winnerHistory = new History();
            winnerHistory.setRecordedDeckId(latestDeckInformation.getId());
            winnerHistory.setRecordedDeckName(latestDeckInformation.getName());
            winnerHistory.setRecordedDeckOwner(latestDeckInformation.getOwner());
            winnerHistory.setOutcome("WON");
            winnerHistory.setOldRating(latestDeckInformation.getRating());
            winnerHistory.setGame(latestDeckInformation.getGame());
            historyForWinners.add(winnerHistory);
        }

        //get loser deck information
        for (String loser : losers) {
            List<Deck> deckHistory = getDeckInformation(loser); //get history of the deck
            int latestDeckData = deckHistory.size() - 1;
            Deck latestDeckInformation = deckHistory.get(latestDeckData);
            loserDecks.add(latestDeckInformation);

            History loserHistory = new History();
            loserHistory.setRecordedDeckId(latestDeckInformation.getId());
            loserHistory.setRecordedDeckName(latestDeckInformation.getName());
            loserHistory.setRecordedDeckOwner(latestDeckInformation.getOwner());
            loserHistory.setOutcome("LOST");
            loserHistory.setOldRating(latestDeckInformation.getRating());
            loserHistory.setGame(latestDeckInformation.getGame());
            historyForLosers.add(loserHistory);
        }

        //calculate new ratings
        Calculation calculation = new Calculation();

        //update csv
        List<Deck> updatedList = calculation.deckRatingCalculation(winnerDecks, loserDecks);

        writeDecks(updatedList);
        writeHistory(historyForWinners, historyForLosers);
    }

    private void writeHistory(List<History> historyForWinners, List<History> historyForLosers) {
        //collect
        List<History> deckHistory = new ArrayList<>();

        //add to opponents the other ones then put into new list
        for(History winner: historyForWinners) {
            StringBuffer opponents = new StringBuffer();
            for (int i = 0; i < historyForLosers.size(); i++) {
                if(i>0) {
                    opponents.append(" and ");
                }
                opponents.append(historyForLosers.get(i).getRecordedDeckName() +" owned by " + historyForLosers.get(i).getRecordedDeckOwner());
            }
            winner.setOpponent(opponents.toString());
            deckHistory.add(winner);
        }

        for(History loser: historyForLosers) {
            StringBuffer opponents = new StringBuffer();
            for (int i = 0; i < historyForWinners.size(); i++) {
                if(i>0) {
                    opponents.append(" and ");
                }
                opponents.append(historyForWinners.get(i).getRecordedDeckName() +" owned by " + historyForWinners.get(i).getRecordedDeckOwner());
            }
            loser.setOpponent(opponents.toString());
            deckHistory.add(loser);
        }

        historyDao.writeHistory(deckHistory);
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

    public List<Deck> getHistoryForDeck(List<Deck> allUniqueDecks, String deckId) {
        return IntStream.range(0, allUniqueDecks.size()).filter(i -> allUniqueDecks.get(i).getId().equals(deckId)).mapToObj(allUniqueDecks::get).toList();
    }

    public List<Deck> filterByGame(List<Deck> allUniqueDecks, String game) {
        return IntStream.range(0, allUniqueDecks.size()).filter(i -> allUniqueDecks.get(i).getGame().equals(game)).mapToObj(allUniqueDecks::get).toList();
    }

    public List<Deck> getFullDeckList() {
        return deckDao.getFullDeckList();
        }

    public List<List<Object>> getChartData(List<Deck> deckHistoryList) {
        List<List<Object>> graphData = new ArrayList<>();
        for (int i = 0; i < deckHistoryList.size(); i++) {
            graphData.add(List.of(i, deckHistoryList.get(i).getRating()));
        }
        return graphData;
    }

    public Object filterByTier(List<Deck> allUniqueDecks, int tier) {
        return allUniqueDecks.stream().filter(allUniqueDeck -> allUniqueDeck.getTier() == tier).collect(Collectors.toList());
    }

    public void addNewDeck(Deck deck) {
        deck.setId(deck.getName() + deck.getOwner() + deck.getGame());
        deck.setRating(1000);
        deck.setTier(4);
        deck.setCardList("TBA");

        deckDao.writeDeck(deck);
    }

    public List<Card> getCardList(String cardList) throws IOException {
        return decklistDao.getDecklist(cardList);
        }
}
