package com.tictactoe.backend.core;

import com.tictactoe.backend.model.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WinCheckerTest {

    @Test
    void detectsRowWin() {
        Board b = new Board();
        b.place(1, 0, Player.X);
        b.place(1, 1, Player.X);
        b.place(1, 2, Player.X);
        WinResult result = WinChecker.checkWin(b);
        assertTrue(result.isWon());
        assertEquals(Player.X, result.getWinner());
        assertEquals(3, result.getWinningCells().size());
    }

    @Test
    void detectsColumnWin() {
        Board b = new Board();
        b.place(0, 2, Player.O);
        b.place(1, 2, Player.O);
        b.place(2, 2, Player.O);
        WinResult result = WinChecker.checkWin(b);
        assertTrue(result.isWon());
        assertEquals(Player.O, result.getWinner());
    }

    @Test
    void detectsMainDiagonalWin() {
        Board b = new Board();
        b.place(0, 0, Player.X);
        b.place(1, 1, Player.X);
        b.place(2, 2, Player.X);
        assertTrue(WinChecker.checkWin(b).isWon());
    }

    @Test
    void detectsAntiDiagonalWin() {
        Board b = new Board();
        b.place(0, 2, Player.O);
        b.place(1, 1, Player.O);
        b.place(2, 0, Player.O);
        assertTrue(WinChecker.checkWin(b).isWon());
    }

    @Test
    void noWinOnEmptyBoard() {
        Board b = new Board();
        assertFalse(WinChecker.checkWin(b).isWon());
    }

    @Test
    void detectsDrawWhenBoardFullWithNoWinner() {
        Board b = new Board();
        // X O X / X O O / O X X
        b.place(0, 0, Player.X);
        b.place(0, 1, Player.O);
        b.place(0, 2, Player.X);
        b.place(1, 0, Player.X);
        b.place(1, 1, Player.O);
        b.place(1, 2, Player.O);
        b.place(2, 0, Player.O);
        b.place(2, 1, Player.X);
        b.place(2, 2, Player.X);
        WinResult result = WinChecker.checkWin(b);
        assertFalse(result.isWon());
        assertTrue(WinChecker.isDraw(b, result));
    }

    @Test
    void notADrawWhenBoardIsNotFull() {
        Board b = new Board();
        b.place(0, 0, Player.X);
        WinResult result = WinChecker.checkWin(b);
        assertFalse(WinChecker.isDraw(b, result));
    }
}
