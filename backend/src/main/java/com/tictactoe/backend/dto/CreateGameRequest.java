package com.tictactoe.backend.dto;

import com.tictactoe.backend.model.GameMode;

/**
 * Body for POST /api/games. `mode` is optional; defaults to TWO_PLAYER when
 * omitted. Accepted values: "TWO_PLAYER", "VS_COMPUTER".
 */
public class CreateGameRequest {
    private GameMode mode;

    public CreateGameRequest() {
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }
}
