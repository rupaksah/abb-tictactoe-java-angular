package com.tictactoe.backend.model;

/**
 * The two players in a Tic Tac Toe game. In VS_COMPUTER mode, X is always
 * the human and O is always the computer (see GameMode).
 */
public enum Player {
    X,
    O;

    public Player opponent() {
        return this == X ? O : X;
    }
}
