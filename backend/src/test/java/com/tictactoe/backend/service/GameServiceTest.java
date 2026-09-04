package com.tictactoe.backend.service;

import com.tictactoe.backend.dto.GameStateResponse;
import com.tictactoe.backend.dto.MoveRequest;
import com.tictactoe.backend.exception.GameNotFoundException;
import com.tictactoe.backend.model.GameMode;
import com.tictactoe.backend.model.Player;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Service-level tests: DTO mapping, cellIndex resolution, and the
 * request/response contract the Angular frontend relies on. GameService has
 * no Spring-managed dependencies of its own, so it's instantiated directly
 * here rather than via a full Spring context, keeping these tests fast.
 */
class GameServiceTest {

    @Test
    void createGameDefaultsToTwoPlayerMode() {
        GameService service = new GameService();
        GameStateResponse state = service.createGame(null);
        assertEquals("TWO_PLAYER", state.getGameMode());
        assertEquals("InProgress", state.getStatus());
        assertEquals("X", state.getCurrentPlayer());
        assertNotNull(state.getGameId());
    }

    @Test
    void createGameHonoursRequestedMode() {
        GameService service = new GameService();
        GameStateResponse state = service.createGame(GameMode.VS_COMPUTER);
        assertEquals("VS_COMPUTER", state.getGameMode());
    }

    @Test
    void getUnknownGameThrowsGameNotFound() {
        GameService service = new GameService();
        assertThrows(GameNotFoundException.class, () -> service.getGame("does-not-exist"));
    }

    @Test
    void moveByRowAndColIsApplied() {
        GameService service = new GameService();
        String id = service.createGame(GameMode.TWO_PLAYER).getGameId();

        MoveRequest move = new MoveRequest();
        move.setPlayer(Player.X);
        move.setRow(0);
        move.setCol(0);
        GameStateResponse state = service.makeMove(id, move);

        assertEquals("X", state.getBoard()[0][0]);
        assertEquals("O", state.getCurrentPlayer());
    }

    @Test
    void moveByCellIndexIsResolvedToRowCol() {
        GameService service = new GameService();
        String id = service.createGame(GameMode.TWO_PLAYER).getGameId();

        MoveRequest move = new MoveRequest();
        move.setPlayer(Player.X);
        move.setCellIndex(4); // row 1, col 1 (center)
        GameStateResponse state = service.makeMove(id, move);

        assertEquals("X", state.getBoard()[1][1]);
    }

    @Test
    void winStatusIsReportedWithSpecCasing() {
        GameService service = new GameService();
        String id = service.createGame(GameMode.TWO_PLAYER).getGameId();

        playMove(service, id, Player.X, 0, 0);
        playMove(service, id, Player.O, 1, 0);
        playMove(service, id, Player.X, 0, 1);
        playMove(service, id, Player.O, 1, 1);
        GameStateResponse state = playMove(service, id, Player.X, 0, 2);

        assertEquals("Won", state.getStatus());
        assertEquals("X", state.getWinner());
        assertEquals(1, state.getScoreboard().getXWins());
    }

    @Test
    void resetScoreboardZeroesAllCounts() {
        GameService service = new GameService();
        String id = service.createGame(GameMode.TWO_PLAYER).getGameId();
        playMove(service, id, Player.X, 0, 0);
        playMove(service, id, Player.O, 1, 0);
        playMove(service, id, Player.X, 0, 1);
        playMove(service, id, Player.O, 1, 1);
        playMove(service, id, Player.X, 0, 2); // X wins
        assertEquals(1, service.getScoreboard().getXWins());

        service.resetScoreboard();

        assertEquals(0, service.getScoreboard().getXWins());
        assertEquals(0, service.getScoreboard().getOWins());
        assertEquals(0, service.getScoreboard().getDraws());
    }

    @Test
    void undoIsReflectedInCanUndoFlag() {
        GameService service = new GameService();
        String id = service.createGame(GameMode.TWO_PLAYER).getGameId();
        assertTrue(!service.getGame(id).isCanUndo());

        playMove(service, id, Player.X, 0, 0);
        assertTrue(service.getGame(id).isCanUndo());

        service.undo(id);
        assertTrue(!service.getGame(id).isCanUndo());
    }

    private GameStateResponse playMove(GameService service, String gameId, Player player, int row, int col) {
        MoveRequest move = new MoveRequest();
        move.setPlayer(player);
        move.setRow(row);
        move.setCol(col);
        return service.makeMove(gameId, move);
    }
}
