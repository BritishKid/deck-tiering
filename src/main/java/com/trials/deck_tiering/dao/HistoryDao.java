package com.trials.deck_tiering.dao;

import com.trials.deck_tiering.model.History;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class HistoryDao {

    private static final String CSV_SEPARATOR = ",";
    File file = new File("csv/history.csv").getAbsoluteFile();


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
                oneline.append(history.getGame());
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

    public List<History> getHistoryFromDeckId(String deckId) {
        List<History> historyList = new ArrayList<>();
        try (
            BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if(values[0].equals(deckId)) {
                    History history = new History(values[0], values[1], values[2], Integer.parseInt(values[3]), values[4], values[5], values[6]);
                    historyList.add(history);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return historyList;
    }
}


