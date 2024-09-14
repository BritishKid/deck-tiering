package com.trials.deck_tiering.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class History {
    private String recordedDeckId;
    private String recordedDeckName;
    private String recordedDeckOwner;
    private int oldRating;
    private String outcome;
    private String opponent;
    private String game;
}
