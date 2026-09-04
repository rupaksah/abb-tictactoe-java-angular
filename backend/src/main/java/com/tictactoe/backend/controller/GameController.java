package com.tictactoe.backend.controller;

import com.tictactoe.backend.dto.CreateGameRequest;
import com.tictactoe.backend.dto.GameStateResponse;
import com.tictactoe.backend.dto.MoveRequest;
import com.tictactoe.backend.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for game operations, matching the "Suggested API Scope" table in
 * the problem statement. Full contract is documented in the README, and
 * served interactively at /swagger-ui.html once springdoc-openapi picks up
 * the annotations below.
 */
@RestController
@RequestMapping("/api/games")
@Tag(name = "Games", description = "Create and play Tic Tac Toe games")
public class GameController {

    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "Create a new game",
            description = "Starts a new game session. mode defaults to TWO_PLAYER when omitted.")
    @PostMapping
    public ResponseEntity<GameStateResponse> createGame(@RequestBody(required = false) CreateGameRequest request) {
        var mode = request == null ? null : request.getMode();
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createGame(mode));
    }

    @Operation(summary = "Get game state",
            description = "Returns the current state of a game: board, turn, status, move history, and scoreboard.")
    @GetMapping("/{id}")
    public ResponseEntity<GameStateResponse> getGame(@PathVariable String id) {
        return ResponseEntity.ok(gameService.getGame(id));
    }

    @Operation(summary = "Submit a move",
            description = "Validates and applies a move. In VS_COMPUTER mode, the computer's reply "
                    + "(if the game doesn't end on the human's move) is applied automatically as part "
                    + "of this same call.")
    @PostMapping("/{id}/moves")
    public ResponseEntity<GameStateResponse> makeMove(@PathVariable String id, @Valid @RequestBody MoveRequest request) {
        return ResponseEntity.ok(gameService.makeMove(id, request));
    }

    @Operation(summary = "Undo the last move",
            description = "Reverts to the state before the most recent turn: one move in TWO_PLAYER "
                    + "mode, or the human+computer move pair together in VS_COMPUTER mode. Disabled "
                    + "once the game has completed (see README, Clarification 2, Option A).")
    @PostMapping("/{id}/undo")
    public ResponseEntity<GameStateResponse> undo(@PathVariable String id) {
        return ResponseEntity.ok(gameService.undo(id));
    }

    @Operation(summary = "Reset the game",
            description = "Clears the board, move history, and status for a fresh game in the same "
                    + "mode. The scoreboard is left untouched.")
    @PostMapping("/{id}/reset")
    public ResponseEntity<GameStateResponse> resetGame(@PathVariable String id) {
        return ResponseEntity.ok(gameService.resetGame(id));
    }
}
