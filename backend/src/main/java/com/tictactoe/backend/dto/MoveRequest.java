package com.tictactoe.backend.dto;

import com.tictactoe.backend.model.Player;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body for POST /api/games/{id}/moves.
 *
 * The board position can be given either as (row, col) — both 0-based,
 * 0..2 — or as a single 0-based cellIndex (0..8, row-major: index = row*3 +
 * col). If both are supplied, row/col take precedence. `gameId` is not
 * included here since it's already part of the URL path.
 *
 * The @Min/@Max bounds below only reject obviously out-of-range input
 * (e.g. row: 9, cellIndex: -1) with a clean 400 before the request ever
 * reaches the service layer. They're deliberately loose: a request that
 * omits both (row, col) and cellIndex, or that names a cell already
 * occupied, is still a legitimate "which representation, which rule was
 * broken" question for GameService/GameSession to answer (see
 * InvalidMoveException in the README's error table) — bean validation is
 * for shape/range, not game rules.
 */
@Getter
@Setter
@NoArgsConstructor
public class MoveRequest {
    @NotNull(message = "player is required (X or O)")
    private Player player;

    @Min(value = 0, message = "row must be between 0 and 2")
    @Max(value = 2, message = "row must be between 0 and 2")
    private Integer row;

    @Min(value = 0, message = "col must be between 0 and 2")
    @Max(value = 2, message = "col must be between 0 and 2")
    private Integer col;

    @Min(value = 0, message = "cellIndex must be between 0 and 8")
    @Max(value = 8, message = "cellIndex must be between 0 and 8")
    private Integer cellIndex;
}
