package com.tictactoe.backend.dto;

public class MoveHistoryItem {
    private int moveNumber;
    private String player;
    private int row;
    private int col;

    public MoveHistoryItem() {
    }

    public MoveHistoryItem(int moveNumber, String player, int row, int col) {
        this.moveNumber = moveNumber;
        this.player = player;
        this.row = row;
        this.col = col;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public void setMoveNumber(int moveNumber) {
        this.moveNumber = moveNumber;
    }

    public String getPlayer() {
        return player;
    }

    public void setPlayer(String player) {
        this.player = player;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
