package com.tictactoe.backend.dto;

import com.tictactoe.backend.model.GameMode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body for POST /api/games. `mode` is optional; defaults to TWO_PLAYER when
 * omitted. Accepted values: "TWO_PLAYER", "VS_COMPUTER".
 */
@Getter
@Setter
@NoArgsConstructor
public class CreateGameRequest {
    private GameMode mode;
}
