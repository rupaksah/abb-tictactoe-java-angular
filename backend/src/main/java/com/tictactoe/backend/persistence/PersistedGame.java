package com.tictactoe.backend.persistence;

/**
 * Raw row read back from the {@code games} SQLite table, before it is turned
 * into a live {@link com.tictactoe.backend.core.GameSession} by
 * {@link GameStateCodec#toSession}. Kept as plain strings/booleans here so
 * this package has no dependency on how the domain model chooses to
 * represent things internally.
 */
public record PersistedGame(
        String id,
        String mode,
        String board,
        String currentPlayer,
        String status,
        String winner,
        String winningCells,
        String moveHistory,
        boolean scoreCounted
) {
}
