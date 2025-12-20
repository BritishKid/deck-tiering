package com.trials.deck_tiering.dao;

import com.trials.deck_tiering.model.History;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class HistoryDao {

    private static final String CSV_SEPARATOR = ",";
    private static final int EXPECTED_COLS = 9;

    private final File file = new File("csv/history.csv").getAbsoluteFile();

    public void writeHistory(List<History> deckHistory) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {

            for (History history : deckHistory) {

                String oneline = history.getRecordedDeckId() + CSV_SEPARATOR +
                        history.getRecordedDeckName() + CSV_SEPARATOR +
                        history.getRecordedDeckOwner() + CSV_SEPARATOR +
                        history.getOldRating() + CSV_SEPARATOR +
                        history.getOutcome() + CSV_SEPARATOR +
                        history.getOpponent() + CSV_SEPARATOR +
                        history.getGame() + CSV_SEPARATOR +
                        history.getWinsFor() + CSV_SEPARATOR +
                        history.getWinsAgainst();

                bw.write(oneline);
                bw.newLine();
            }

            bw.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<History> getHistoryFromDeckId(String deckId) {
        List<History> historyList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;

                String[] values = line.split(CSV_SEPARATOR, -1);
                if (values.length != EXPECTED_COLS) {
                    throw new IllegalArgumentException("Invalid history.csv row (expected "
                            + EXPECTED_COLS + " columns): " + line);
                }

                if (values[0].equals(deckId)) {
                    History history = new History(
                            values[0],                      // deckId
                            values[1],                      // name
                            values[2],                      // owner
                            Integer.parseInt(values[3]),    // oldRating
                            values[4],                      // outcome
                            values[5],                      // opponent
                            values[6],                      // game
                            Integer.parseInt(values[7]),    // winsFor
                            Integer.parseInt(values[8])     // winsAgainst
                    );

                    // NEW: score columns
                    history.setWinsFor(Integer.parseInt(values[7]));
                    history.setWinsAgainst(Integer.parseInt(values[8]));

                    historyList.add(history);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return historyList;
    }
}
