package com.tictactoe.backend.core;

import com.tictactoe.backend.model.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the computer opponent's move priority: win > block > center >
 * corner > any available cell, exactly as specified in the problem
 * statement's "Basic Computer Mode" section.
 */
class ComputerPlayerTest {

    @Test
    void takesWinningMoveWhenAvailable() {
        Board b = new Board();
        b.place(0, 0, Player.O);
        b.place(0, 1, Player.O);
        b.place(1, 0, Player.X);
        b.place(1, 1, Player.X);

        int[] move = ComputerPlayer.chooseMove(b, Player.O, Player.X);

        assertEquals(0, move[0]);
        assertEquals(2, move[1]);
    }

    @Test
    void blocksOpponentWinningMoveWhenNoOwnWinAvailable() {
        Board b = new Board();
        b.place(0, 0, Player.X);
        b.place(1, 1, Player.X);
        b.place(0, 1, Player.O);
        b.place(1, 0, Player.O);

        int[] move = ComputerPlayer.chooseMove(b, Player.O, Player.X);

        assertEquals(2, move[0]);
        assertEquals(2, move[1]);
    }

    @Test
    void winTakesPriorityOverBlock() {
        // O can win at (0,2) (completes row 0). X separately threatens to win
        // at (2,2) (would complete row 2). The win must be taken, not the block.
        Board b = new Board();
        b.place(0, 0, Player.O);
        b.place(0, 1, Player.O);
        b.place(2, 0, Player.X);
        b.place(2, 1, Player.X);

        int[] move = ComputerPlayer.chooseMove(b, Player.O, Player.X);

        assertEquals(0, move[0]);
        assertEquals(2, move[1]);
    }

    @Test
    void takesCenterWhenNoWinOrBlockAvailable() {
        Board b = new Board();
        b.place(0, 0, Player.X);

        int[] move = ComputerPlayer.chooseMove(b, Player.O, Player.X);

        assertEquals(1, move[0]);
        assertEquals(1, move[1]);
    }

    @Test
    void takesCornerWhenCenterTakenAndNoWinOrBlock() {
        Board b = new Board();
        b.place(1, 1, Player.X);
        b.place(0, 1, Player.O);

        int[] move = ComputerPlayer.chooseMove(b, Player.O, Player.X);

        boolean isCorner = (move[0] == 0 || move[0] == 2) && (move[1] == 0 || move[1] == 2);
        assertTrue(isCorner);
    }

    @Test
    void takesAnyAvailableCellAsLastResort() {
        // Center and all 4 corners are occupied, with no win or block available
        // to O at either remaining empty edge cell (0,1) or (2,1). The fallback
        // "take any available cell" rule applies, scanning row-major, so (0,1)
        // (the first empty cell encountered) must be chosen.
        Board b = new Board();
        b.place(1, 1, Player.X); // center
        b.place(0, 0, Player.X);
        b.place(0, 2, Player.O);
        b.place(2, 0, Player.X);
        b.place(2, 2, Player.O);
        b.place(1, 0, Player.O);
        b.place(1, 2, Player.X);
        // (0,1) and (2,1) remain empty.

        int[] move = ComputerPlayer.chooseMove(b, Player.O, Player.X);

        assertEquals(0, move[0]);
        assertEquals(1, move[1]);
    }
}
