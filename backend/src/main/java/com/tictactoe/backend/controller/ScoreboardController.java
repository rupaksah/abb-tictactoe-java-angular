package com.tictactoe.backend.controller;

import com.tictactoe.backend.dto.ScoreboardResponse;
import com.tictactoe.backend.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scoreboard")
@Tag(name = "Scoreboard", description = "Session-level win/draw counts, shared across all games")
public class ScoreboardController {

    private final GameService gameService;

    public ScoreboardController(GameService gameService) {
        this.gameService = gameService;
    }

    @Operation(summary = "Get the scoreboard", description = "Returns X wins / O wins / draws for the server's lifetime.")
    @GetMapping
    public ScoreboardResponse getScoreboard() {
        return gameService.getScoreboard();
    }

    @Operation(summary = "Reset the scoreboard", description = "Zeroes X wins / O wins / draws. Does not affect any in-progress game.")
    @PostMapping("/reset")
    public ScoreboardResponse resetScoreboard() {
        return gameService.resetScoreboard();
    }
}
