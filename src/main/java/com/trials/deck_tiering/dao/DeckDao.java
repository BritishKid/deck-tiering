package com.trials.deck_tiering.dao;

import com.trials.deck_tiering.model.Deck;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class DeckDao {

    private static final String CSV_SEPARATOR = ",";
    File file = new File("csv/decks.csv").getAbsoluteFile();


    public Map<String, Deck> getUniqueDeckList() {
        Map<String, Deck> decks = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(CSV_SEPARATOR);
                String deckId = values[0];
                if(Integer.parseInt(values[2]) != 1300 && Integer.parseInt(values[2]) != 1500 && Integer.parseInt(values[2]) != 1750) {
                    Deck deck = new Deck(deckId, values[1], Integer.parseInt(values[2]), values[3], values[4], Integer.parseInt(values[5]), values[6]);
                    decks.put(deckId, deck);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return decks;
    }
    //read and write files


    public List<Deck> getDeckInformationFromId(String deckId) {
        List<Deck> decks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if(values[0].equals(deckId)) {
                    Deck deck = new Deck(values[0], values[1], Integer.parseInt(values[2]), values[3], values[4], Integer.parseInt(values[5]), values[6]);
                    decks.add(deck);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return decks;
    }


    public void writeDecks(List<Deck> decks) {

        try {
            FileWriter fw = new FileWriter(file, true); //todo put csv creator in the model
            BufferedWriter bw = new BufferedWriter(fw);
            for(Deck deck: decks) {
                StringBuffer oneline = new StringBuffer();
                buildCsvWriter(deck, oneline);
                bw.write(oneline.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Deck> getFullDeckList() {
       List<Deck> decks = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(CSV_SEPARATOR);
                Deck deck = new Deck(values[0], values[1], Integer.parseInt(values[2]), values[3], values[4], Integer.parseInt(values[5]), values[6]);
                decks.add(deck);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return decks;
    }

    public void writeDeck(Deck deck) {
        try {
            FileWriter fw = new FileWriter(file, true); //todo put csv creator in the model
            BufferedWriter bw = new BufferedWriter(fw);
            StringBuffer oneline = new StringBuffer();
            buildCsvWriter(deck, oneline);
            bw.write(oneline.toString());
            bw.newLine();
            bw.flush();
            bw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void buildCsvWriter(Deck deck, StringBuffer oneline) {
        oneline.append(deck.getId());
        oneline.append(CSV_SEPARATOR);
        oneline.append(deck.getName());
        oneline.append(CSV_SEPARATOR);
        oneline.append(deck.getRating());
        oneline.append(CSV_SEPARATOR);
        oneline.append(deck.getOwner());
        oneline.append(CSV_SEPARATOR);
        oneline.append(deck.getCardList());
        oneline.append(CSV_SEPARATOR);
        oneline.append(deck.getTier());
        oneline.append(CSV_SEPARATOR);
        oneline.append(deck.getGame());
    }
}
