package com.tictactoe.backend.dto;

import java.util.List;

/**
 * Full game state as returned by every game endpoint (create/get/move/
 * undo/reset), so the frontend can always re-render from a single response
 * per the "Backend State Ownership" clarification.
 */
public class GameStateResponse {
    private String gameId;
    private String[][] board;
    private String currentPlayer;
    private String gameMode;
    private String status;
    private String winner;
    private List<CellPosition> winningCells;
    private List<MoveHistoryItem> moveHistory;
    private boolean canUndo;
    private ScoreboardResponse scoreboard;

    public GameStateResponse() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String[][] getBoard() {
        return board;
    }

    public void setBoard(String[][] board) {
        this.board = board;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getGameMode() {
        return gameMode;
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public List<CellPosition> getWinningCells() {
        return winningCells;
    }

    public void setWinningCells(List<CellPosition> winningCells) {
        this.winningCells = winningCells;
    }

    public List<MoveHistoryItem> getMoveHistory() {
        return moveHistory;
    }

    public void setMoveHistory(List<MoveHistoryItem> moveHistory) {
        this.moveHistory = moveHistory;
    }

    public boolean isCanUndo() {
        return canUndo;
    }

    public void setCanUndo(boolean canUndo) {
        this.canUndo = canUndo;
    }

    public ScoreboardResponse getScoreboard() {
        return scoreboard;
    }

    public void setScoreboard(ScoreboardResponse scoreboard) {
        this.scoreboard = scoreboard;
    }
}
