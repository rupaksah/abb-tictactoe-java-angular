package com.tictactoe.backend.core;

import com.tictactoe.backend.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Stateless win/draw detection for a 3x3 board: all 3 rows, 3 columns and
 * 2 diagonals are checked.
 */
public final class WinChecker {

    private WinChecker() {
    }

    /** All 8 possible winning lines, each as an array of {row,col} pairs. */
    private static final int[][][] LINES = buildLines();

    private static int[][][] buildLines() {
        List<int[][]> lines = new ArrayList<>();
        // Rows
        for (int r = 0; r < Board.SIZE; r++) {
            lines.add(new int[][]{{r, 0}, {r, 1}, {r, 2}});
        }
        // Columns
        for (int c = 0; c < Board.SIZE; c++) {
            lines.add(new int[][]{{0, c}, {1, c}, {2, c}});
        }
        // Diagonals
        lines.add(new int[][]{{0, 0}, {1, 1}, {2, 2}});
        lines.add(new int[][]{{0, 2}, {1, 1}, {2, 0}});
        return lines.toArray(new int[0][][]);
    }

    public static WinResult checkWin(Board board) {
        for (int[][] line : LINES) {
            Player a = board.get(line[0][0], line[0][1]);
            if (a == null) {
                continue;
            }
            Player b = board.get(line[1][0], line[1][1]);
            Player c = board.get(line[2][0], line[2][1]);
            if (a == b && b == c) {
                return WinResult.of(a, List.of(line[0], line[1], line[2]));
            }
        }
        return WinResult.none();
    }

    public static boolean isDraw(Board board, WinResult winResult) {
        return !winResult.isWon() && board.isFull();
    }
}
