package com.trials.deck_tiering.dao;

import com.trials.deck_tiering.model.Card;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class DecklistDao {

    private static final String CSV_SEPARATOR = ",";
    String filePath = "csv/decklists/%s.csv"; //todo use this to add in the name genned by the decklist id

    public List<Card> getDecklist(String cardList) throws IOException {
        List<Card> listOfCards = new ArrayList<>();
        String fileLocation = String.format(filePath, cardList);
        File file = new File(fileLocation).getAbsoluteFile();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(CSV_SEPARATOR);
                listOfCards.add(new Card(values[0], values[1], values[2], values[3]));
            }
        }

        return listOfCards;
    }
}
