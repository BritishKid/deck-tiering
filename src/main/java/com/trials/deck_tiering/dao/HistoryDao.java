package com.trials.deck_tiering.dao;

import com.trials.deck_tiering.model.Deck;
import com.trials.deck_tiering.model.History;
import org.springframework.stereotype.Repository;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Repository
public class HistoryDao {

    private static final String CSV_SEPARATOR = ",";
    File file = new File("history.csv").getAbsoluteFile();


    public void writeHistory(List<History> deckHistory) {
        try {
            FileWriter fw = new FileWriter(file, true); //todo put csv creator in the model
            BufferedWriter bw = new BufferedWriter(fw);
            for(History history: deckHistory) {
                StringBuffer oneline = new StringBuffer();
                oneline.append(history.getRecordedDeckId());
                oneline.append(CSV_SEPARATOR);
                oneline.append(history.getRecordedDeckName());
                oneline.append(CSV_SEPARATOR);
                oneline.append(history.getRecordedDeckOwner());
                oneline.append(CSV_SEPARATOR);
                oneline.append(history.getOldRating());
                oneline.append(CSV_SEPARATOR);
                oneline.append(history.getOutcome());
                oneline.append(CSV_SEPARATOR);
                oneline.append(history.getOpponent());
                oneline.append(CSV_SEPARATOR);
                bw.write(oneline.toString());
                bw.newLine();
            }
            bw.flush();
            bw.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

