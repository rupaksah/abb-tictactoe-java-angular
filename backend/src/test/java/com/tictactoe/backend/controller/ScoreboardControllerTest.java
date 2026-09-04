package com.tictactoe.backend.controller;

import com.tictactoe.backend.dto.ScoreboardResponse;
import com.tictactoe.backend.service.GameService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScoreboardController.class)
class ScoreboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @Test
    void getScoreboard_returns200WithCounts() throws Exception {
        when(gameService.getScoreboard()).thenReturn(new ScoreboardResponse(2, 1, 0));

        mockMvc.perform(get("/api/scoreboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xWins").value(2))
                .andExpect(jsonPath("$.oWins").value(1))
                .andExpect(jsonPath("$.draws").value(0));
    }

    @Test
    void resetScoreboard_returns200WithZeroedCounts() throws Exception {
        when(gameService.resetScoreboard()).thenReturn(new ScoreboardResponse(0, 0, 0));

        mockMvc.perform(post("/api/scoreboard/reset"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.xWins").value(0))
                .andExpect(jsonPath("$.oWins").value(0))
                .andExpect(jsonPath("$.draws").value(0));
    }
}
