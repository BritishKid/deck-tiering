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
    private String outcome;
    private int oldRating;
    private String opponent;
}
