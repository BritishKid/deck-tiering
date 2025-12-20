package com.trials.deck_tiering.service;

import com.trials.deck_tiering.dao.DeckDao;
import com.trials.deck_tiering.dao.DecklistDao;
import com.trials.deck_tiering.dao.HistoryDao;
import com.trials.deck_tiering.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.trials.deck_tiering.model.GameEnum.*;

@Service
public class DeckService {

    @Autowired private DeckDao deckDao;
    @Autowired private HistoryDao historyDao;
    @Autowired private DecklistDao decklistDao;
    @Autowired private Glicko2Calculation glicko2Calculation;

    public void updateDeckRatings1v1(String winnerId, String loserId, int winnerWins, int loserWins) {

        Deck winnerDeck = getLatestDeck(winnerId);
        Deck loserDeck = getLatestDeck(loserId);

        // Build history entries (optional — if you keep history.csv)
        History winnerHistory = new History();
        winnerHistory.setRecordedDeckId(winnerDeck.getId());
        winnerHistory.setRecordedDeckName(winnerDeck.getName());
        winnerHistory.setRecordedDeckOwner(winnerDeck.getOwner());
        winnerHistory.setOutcome("WON");
        winnerHistory.setOldRating(winnerDeck.getRating());
        winnerHistory.setGame(winnerDeck.getGame());
        winnerHistory.setOpponent(loserDeck.getName() + " owned by " + loserDeck.getOwner());
        winnerHistory.setWinsFor(winnerWins);
        winnerHistory.setWinsAgainst(loserWins);

        History loserHistory = new History();
        loserHistory.setRecordedDeckId(loserDeck.getId());
        loserHistory.setRecordedDeckName(loserDeck.getName());
        loserHistory.setRecordedDeckOwner(loserDeck.getOwner());
        loserHistory.setOutcome("LOST");
        loserHistory.setOldRating(loserDeck.getRating());
        loserHistory.setGame(loserDeck.getGame());
        loserHistory.setOpponent(winnerDeck.getName() + " owned by " + winnerDeck.getOwner());
        loserHistory.setWinsFor(loserWins);
        loserHistory.setWinsAgainst(winnerWins);

        // Apply Glicko-2 update (winner is deckA)
        List<Deck> updated = glicko2Calculation.update1v1Match(
                winnerDeck,
                loserDeck,
                winnerWins,
                loserWins
        );

        for (Deck d : updated) {
            d.setGamesPlayed(d.getGamesPlayed() + 1);
        }

        deckDao.writeDecks(updated);

        // Write history if you want to keep it
        historyDao.writeHistory(List.of(winnerHistory, loserHistory));
    }

    private Deck getLatestDeck(String deckId) {
        List<Deck> deckHistory = deckDao.getDeckInformationFromId(deckId);
        if (deckHistory.isEmpty()) {
            throw new IllegalArgumentException("No deck found with id: " + deckId);
        }
        return deckHistory.get(deckHistory.size() - 1);
    }

    public List<Deck> getAllUniqueDecks() {
        return new ArrayList<>(deckDao.getUniqueDeckList().values());
    }

    public List<Deck> getAllUniqueDecksUnfiltered() {
        return new ArrayList<>(deckDao.getUniqueDeckListUnfiltered().values());
    }

    public List<Deck> orderDeckByRating(List<Deck> decks) {
        decks.sort(new DeckComprator());
        return decks;
    }

    public List<Deck> filterByGame(List<Deck> decks, String game) {
        return decks.stream()
                .filter(d -> d.getGame().equals(game))
                .toList();
    }

    public void addNewDeck(Deck deck) {
        String fileName = deck.getName() + deck.getOwner();
        fileName = fileName.replaceAll("\\s+",""); //remove whitespace
        deck.setRating(1500);
        deck.setTier(3);
        deck.setRatingDeviation(350.0);
        deck.setVolatility(0.06);
        deck.setGamesPlayed(0);

        deck.setCardList(fileName);

        //do for each value in the games options
        if(deck.getGame().equals(POKEMON.getName())) {
            getPokemonGameList().forEach(gameName -> {
                deck.setId(deck.getName() + deck.getOwner() + gameName);
                deck.setGame(gameName);
                deckDao.writeDeck(deck);
            });
        }
        else if(deck.getGame().equals(YUGIOH.getName())) {
            getYugiohGameList().forEach(gameName -> {
                deck.setId(deck.getName() + deck.getOwner() + gameName);
                deck.setGame(gameName);
                deckDao.writeDeck(deck);
            });
        }
        else {
                    deck.setId(deck.getName() + deck.getOwner() + deck.getGame());
            deckDao.writeDeck(deck);
        }
    }

    public List<Deck> filterPlayersDecks(List<Deck> allUniqueDecks, String owner) {
        return allUniqueDecks.stream().filter(deck -> deck.getOwner().equals(owner)).collect(Collectors.toList());
    }

    public List<Deck> getFullDeckList() {
        return deckDao.getFullDeckList();
    }

    public List<Deck> getHistoryForDeck(List<Deck> allUniqueDecks, String deckId) {
        return IntStream.range(0, allUniqueDecks.size()).filter(i -> allUniqueDecks.get(i).getId().equals(deckId)).mapToObj(allUniqueDecks::get).toList();
    }

    public List<String> getHistoryOutput(String deckId) {
        List<History> history = historyDao.getHistoryFromDeckId(deckId);;
        List<String> output = new ArrayList<>();
        for(History h: history) {
            StringBuffer sb = new StringBuffer();
            sb.append(h.getOutcome());
            sb.append(" against ");
            sb.append(h.getOpponent());
            output.add(sb.toString());
        }
        return output;
    }

    public List<Deck> addHistoryOutput(List<Deck> deckHistoryList, List<String> deckHistoryOutput) {
        int i=0;
        for(Deck deck: deckHistoryList) {
            if(i > 0) {
                deck.setHistoryOutcome(deckHistoryOutput.get(i-1));
            } else {
                deck.setHistoryOutcome("Deck created");
            }
            i++;
        }
        return deckHistoryList;
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

}

