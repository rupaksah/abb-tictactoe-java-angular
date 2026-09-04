package com.tictactoe.backend.core;

/**
 * Thrown for any rejected move: out of bounds, occupied cell, wrong
 * player's turn, or a move submitted after the game already completed.
 */
public class InvalidMoveException extends RuntimeException {
    public InvalidMoveException(String message) {
        super(message);
    }
}
