package com.tictactoe.backend.controller;

import com.tictactoe.backend.core.InvalidMoveException;
import com.tictactoe.backend.dto.CellPosition;
import com.tictactoe.backend.dto.GameStateResponse;
import com.tictactoe.backend.dto.MoveHistoryItem;
import com.tictactoe.backend.dto.ScoreboardResponse;
import com.tictactoe.backend.exception.GameNotFoundException;
import com.tictactoe.backend.model.GameMode;
import com.tictactoe.backend.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-layer tests for GameController: these verify the actual REST
 * contract (status codes, JSON field names, and the GlobalExceptionHandler's
 * status-code mapping) with GameService mocked out, as a complement to
 * GameSessionTest/GameServiceTest, which cover the domain logic underneath
 * without going through HTTP at all.
 */
@WebMvcTest(GameController.class)
class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @Test
    void createGame_returns201WithGameState() throws Exception {
        when(gameService.createGame(any())).thenReturn(sampleState());

        mockMvc.perform(post("/api/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"mode\":\"TWO_PLAYER\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.gameId").value("game-1"))
                .andExpect(jsonPath("$.status").value("InProgress"))
                .andExpect(jsonPath("$.currentPlayer").value("X"));
    }

    @Test
    void createGame_withNoBody_stillSucceeds() throws Exception {
        when(gameService.createGame(any())).thenReturn(sampleState());

        mockMvc.perform(post("/api/games"))
                .andExpect(status().isCreated());
    }

    @Test
    void getGame_returns200WithGameState() throws Exception {
        when(gameService.getGame("game-1")).thenReturn(sampleState());

        mockMvc.perform(get("/api/games/game-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("game-1"));
    }

    @Test
    void getGame_unknownId_returns404WithErrorBody() throws Exception {
        when(gameService.getGame("missing")).thenThrow(new GameNotFoundException("missing"));

        mockMvc.perform(get("/api/games/missing"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("GAME_NOT_FOUND"));
    }

    @Test
    void makeMove_returns200WithUpdatedState() throws Exception {
        when(gameService.makeMove(anyString(), any())).thenReturn(sampleState());

        mockMvc.perform(post("/api/games/game-1/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"player\":\"X\",\"row\":0,\"col\":0}"))
                .andExpect(status().isOk());
    }

    @Test
    void makeMove_invalidMove_returns400WithErrorBody() throws Exception {
        when(gameService.makeMove(anyString(), any()))
                .thenThrow(new InvalidMoveException("Move rejected: cell is already occupied"));

        mockMvc.perform(post("/api/games/game-1/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"player\":\"X\",\"row\":0,\"col\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_MOVE"));
    }

    @Test
    void makeMove_missingPlayer_returns400ValidationError() throws Exception {
        mockMvc.perform(post("/api/games/game-1/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"row\":0,\"col\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void makeMove_rowOutOfRange_returns400ValidationError() throws Exception {
        mockMvc.perform(post("/api/games/game-1/moves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"player\":\"X\",\"row\":9,\"col\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void undo_returns200WithUpdatedState() throws Exception {
        when(gameService.undo("game-1")).thenReturn(sampleState());

        mockMvc.perform(post("/api/games/game-1/undo"))
                .andExpect(status().isOk());
    }

    @Test
    void reset_returns200WithFreshState() throws Exception {
        when(gameService.resetGame("game-1")).thenReturn(sampleState());

        mockMvc.perform(post("/api/games/game-1/reset"))
                .andExpect(status().isOk());
    }

    private GameStateResponse sampleState() {
        GameStateResponse state = new GameStateResponse();
        state.setGameId("game-1");
        state.setBoard(new String[][]{{null, null, null}, {null, null, null}, {null, null, null}});
        state.setCurrentPlayer("X");
        state.setGameMode(GameMode.TWO_PLAYER.name());
        state.setStatus("InProgress");
        state.setWinner(null);
        state.setWinningCells(Collections.<CellPosition>emptyList());
        state.setMoveHistory(Collections.<MoveHistoryItem>emptyList());
        state.setCanUndo(false);
        state.setScoreboard(new ScoreboardResponse(0, 0, 0));
        return state;
    }
}
