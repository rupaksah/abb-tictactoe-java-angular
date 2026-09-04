package com.tictactoe.backend.model;

/**
 * A single recorded move in a game's move history.
 */
public final class Move {
    private final int moveNumber;
    private final Player player;
    private final int row;
    private final int col;

    public Move(int moveNumber, Player player, int row, int col) {
        this.moveNumber = moveNumber;
        this.player = player;
        this.row = row;
        this.col = col;
    }

    public int getMoveNumber() {
        return moveNumber;
    }

    public Player getPlayer() {
        return player;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public String toString() {
        return "Move#" + moveNumber + "[" + player + " -> (" + row + "," + col + ")]";
    }
}
