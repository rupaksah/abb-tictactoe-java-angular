package com.tictactoe.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Full game state as returned by every game endpoint (create/get/move/
 * undo/reset), so the frontend can always re-render from a single response
 * per the "Backend State Ownership" clarification.
 *
 * canUndo is a primitive boolean, so Lombok's @Getter generates
 * isCanUndo() (not getCanUndo()) - same accessor name this class already
 * had by hand, so the JSON property name Jackson derives from it
 * ("canUndo") is unchanged.
 */
@Getter
@Setter
@NoArgsConstructor
public class GameStateResponse {
    private String gameId;
    private String[][] board;
    private String currentPlayer;
    private String gameMode;
    private String status;
    private String winner;
    private List<CellPosition> winningCells;
    private List<MoveHistoryItem> moveHistory;
    private boolean canUndo;
    private ScoreboardResponse scoreboard;
}
