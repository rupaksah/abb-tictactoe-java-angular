package com.tictactoe.backend.core;

import com.tictactoe.backend.model.Player;

/**
 * Mutable 3x3 board. Zero framework dependencies by design, so this class
 * (and the rest of this package) can be compiled and exercised with a plain
 * `javac`/`java` test harness independent of Spring/Maven.
 */
public final class Board {
    public static final int SIZE = 3;

    private final Player[][] cells = new Player[SIZE][SIZE];

    public Board() {
    }

    /** Deep copy constructor, used to snapshot state for undo. */
    public Board(Board other) {
        for (int r = 0; r < SIZE; r++) {
            System.arraycopy(other.cells[r], 0, this.cells[r], 0, SIZE);
        }
    }

    public boolean isInBounds(int row, int col) {
        return row >= 0 && row < SIZE && col >= 0 && col < SIZE;
    }

    public boolean isEmpty(int row, int col) {
        return isInBounds(row, col) && cells[row][col] == null;
    }

    public Player get(int row, int col) {
        return cells[row][col];
    }

    public void place(int row, int col, Player player) {
        cells[row][col] = player;
    }

    public void clear(int row, int col) {
        cells[row][col] = null;
    }

    public boolean isFull() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (cells[r][c] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Returns a plain String[3][3] view ("X" / "O" / null) for API responses. */
    public String[][] toStringGrid() {
        String[][] grid = new String[SIZE][SIZE];
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                grid[r][c] = cells[r][c] == null ? null : cells[r][c].name();
            }
        }
        return grid;
    }
}
