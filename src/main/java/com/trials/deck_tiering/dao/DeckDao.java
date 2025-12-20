package com.trials.deck_tiering.dao;

import com.trials.deck_tiering.model.Deck;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.*;

@Repository
public class DeckDao {

    private static final String CSV_SEPARATOR = ",";
    private static final int EXPECTED_COLS = 10;

    private final File file = new File("csv/decks.csv").getAbsoluteFile();

    public Map<String, Deck> getUniqueDeckList() {
        Map<String, Deck> decks = getUniqueDeckListUnfiltered();
        decks.values().removeIf(d -> d.getGamesPlayed() <= 0);
        return decks;
    }

    public Map<String, Deck> getUniqueDeckListUnfiltered() {
        Map<String, Deck> decks = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                Deck deck = parseDeck(line);
                decks.put(deck.getId(), deck); // keep latest row for this id
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return decks;
    }

    public List<Deck> getDeckInformationFromId(String deckId) {
        List<Deck> decks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] values = line.split(CSV_SEPARATOR, -1);
                if (values.length != EXPECTED_COLS) {
                    throw new IllegalArgumentException("Invalid decks.csv row (expected " + EXPECTED_COLS + " columns): " + line);
                }

                if (values[0].equals(deckId)) {
                    decks.add(parseDeck(values));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return decks;
    }

    public List<Deck> getFullDeckList() {
        List<Deck> decks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                decks.add(parseDeck(line));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return decks;
    }

    public void writeDecks(List<Deck> decks) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            for (Deck deck : decks) {
                bw.write(toCsvLine(deck));
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeDeck(Deck deck) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(toCsvLine(deck));
            bw.newLine();
            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Deck parseDeck(String line) {
        String[] values = line.split(CSV_SEPARATOR, -1);
        return parseDeck(values);
    }

    private static Deck parseDeck(String[] values) {
        if (values.length != EXPECTED_COLS) {
            throw new IllegalArgumentException("Invalid decks.csv row (expected " + EXPECTED_COLS + " columns)");
        }

        // trim for safety if csv ever gets hand-edited
        for (int i = 0; i < values.length; i++) {
            values[i] = values[i].trim();
        }

        Deck deck = new Deck();
        deck.setId(values[0]);
        deck.setName(values[1]);
        deck.setRating(Integer.parseInt(values[2]));
        deck.setRatingDeviation(Double.parseDouble(values[3]));
        deck.setVolatility(Double.parseDouble(values[4]));
        deck.setOwner(values[5]);
        deck.setCardList(values[6]);
        deck.setTier(Integer.parseInt(values[7]));
        deck.setGame(values[8]);
        deck.setGamesPlayed(Integer.parseInt(values[9]));
        return deck;
    }

    private static String toCsvLine(Deck deck) {
        return String.join(CSV_SEPARATOR,
                deck.getId(),
                deck.getName(),
                String.valueOf(deck.getRating()),
                String.valueOf(deck.getRatingDeviation()),
                String.valueOf(deck.getVolatility()),
                deck.getOwner(),
                deck.getCardList(),
                String.valueOf(deck.getTier()),
                deck.getGame(),
                String.valueOf(deck.getGamesPlayed())
        );
    }
}
