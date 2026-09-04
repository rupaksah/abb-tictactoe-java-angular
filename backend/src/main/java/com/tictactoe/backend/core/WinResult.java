package com.tictactoe.backend.core;

import com.tictactoe.backend.model.Player;

import java.util.List;

/**
 * Result of a win check: either no winner, or a winner plus the exact
 * winning line (used by the frontend to highlight the winning cells).
 */
public final class WinResult {
    private final boolean won;
    private final Player winner;
    private final List<int[]> winningCells;

    private WinResult(boolean won, Player winner, List<int[]> winningCells) {
        this.won = won;
        this.winner = winner;
        this.winningCells = winningCells;
    }

    public static WinResult none() {
        return new WinResult(false, null, List.of());
    }

    public static WinResult of(Player winner, List<int[]> winningCells) {
        return new WinResult(true, winner, winningCells);
    }

    public boolean isWon() {
        return won;
    }

    public Player getWinner() {
        return winner;
    }

    public List<int[]> getWinningCells() {
        return winningCells;
    }
}
