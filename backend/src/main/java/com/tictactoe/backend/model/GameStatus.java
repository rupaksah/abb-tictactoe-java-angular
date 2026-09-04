package com.tictactoe.backend.model;

/**
 * Lifecycle status of a game session, matching the statuses named in the
 * problem statement's "Game State Response" section (InProgress / Won / Draw).
 * Jackson serialization names are applied in the DTO layer, not here, so this
 * enum stays free of any framework dependency and can be unit tested in
 * isolation.
 */
public enum GameStatus {
    IN_PROGRESS,
    WON,
    DRAW
}
