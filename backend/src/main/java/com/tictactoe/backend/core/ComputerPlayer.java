package com.tictactoe.backend.core;

import com.tictactoe.backend.model.Player;

/**
 * Priority-based (non-minimax) computer opponent, implementing exactly the
 * priority order given in the problem statement:
 *   1. If the computer can win, play the winning move.
 *   2. Else if the human can win next, block that cell.
 *   3. Else take the center if available.
 *   4. Else take a corner if available.
 *   5. Else take any available cell.
 */
public final class ComputerPlayer {

    private static final int[] CENTER = {1, 1};
    private static final int[][] CORNERS = {{0, 0}, {0, 2}, {2, 0}, {2, 2}};

    private ComputerPlayer() {
    }

    /**
     * Chooses the computer's next move. Returns {row, col}, or null if the
     * board is full (caller should not invoke this in that case).
     */
    public static int[] chooseMove(Board board, Player computer, Player human) {
        int[] winningMove = findWinningMove(board, computer);
        if (winningMove != null) {
            return winningMove;
        }

        int[] blockingMove = findWinningMove(board, human);
        if (blockingMove != null) {
            return blockingMove;
        }

        if (board.isEmpty(CENTER[0], CENTER[1])) {
            return new int[]{CENTER[0], CENTER[1]};
        }

        for (int[] corner : CORNERS) {
            if (board.isEmpty(corner[0], corner[1])) {
                return new int[]{corner[0], corner[1]};
            }
        }

        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (board.isEmpty(r, c)) {
                    return new int[]{r, c};
                }
            }
        }

        return null;
    }

    /**
     * If `player` has a move available right now that would immediately win
     * the game, returns that move; otherwise null. Used both to find the
     * computer's own winning move and to detect the human's winning move
     * (for blocking).
     */
    private static int[] findWinningMove(Board board, Player player) {
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                if (!board.isEmpty(r, c)) {
                    continue;
                }
                board.place(r, c, player);
                boolean wins = WinChecker.checkWin(board).isWon();
                board.clear(r, c);
                if (wins) {
                    return new int[]{r, c};
                }
            }
        }
        return null;
    }
}
