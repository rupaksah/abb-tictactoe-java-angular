package com.tictactoe.backend.exception;

public class GameNotFoundException extends RuntimeException {
    public GameNotFoundException(String gameId) {
        super("No game found with id " + gameId);
    }
}
