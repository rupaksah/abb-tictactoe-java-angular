package com.tictactoe.backend.core;

/**
 * Thrown when Undo is requested but there is nothing to undo, or (under the
 * chosen Option A policy) the game has already completed.
 */
public class UndoNotAllowedException extends RuntimeException {
    public UndoNotAllowedException(String message) {
        super(message);
    }
}
