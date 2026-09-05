package com.tictactoe.backend.persistence;

import com.tictactoe.backend.core.Board;
import com.tictactoe.backend.core.GameSession;
import com.tictactoe.backend.core.Scoreboard;
import com.tictactoe.backend.model.GameMode;
import com.tictactoe.backend.model.GameStatus;
import com.tictactoe.backend.model.Move;
import com.tictactoe.backend.model.Player;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trip tests for the SQLite persistence encode/decode logic
 * ({@link Board#toEncoded()}/{@link Board#fromEncoded}, {@link GameStateCodec},
 * and the {@link GameSession} restore constructor). Deliberately has no
 * Spring/SQLite dependency of its own - like GameSessionTest, this exercises
 * plain Java that can run without a database, so it's real, not
 * stub-compiled, verification of the persistence layer's core logic.
 */
class GameStateCodecTest {

    @Test
    void boardEncodesAndDecodesRoundTrip() {
        Board board = new Board();
        board.place(0, 0, Player.X);
        board.place(1, 1, Player.O);
        board.place(2, 2, Player.X);

        String encoded = board.toEncoded();
        assertEquals("X...O...X", encoded);

        Board decoded = Board.fromEncoded(encoded);
        assertEquals(Player.X, decoded.get(0, 0));
        assertEquals(Player.O, decoded.get(1, 1));
        assertEquals(Player.X, decoded.get(2, 2));
        assertNull(decoded.get(0, 1));
    }

    @Test
    void emptyBoardEncodesToAllDots() {
        assertEquals(".........", new Board().toEncoded());
    }

    @Test
    void winningCellsEncodeAndDecodeRoundTrip() {
        List<int[]> cells = List.of(new int[]{0, 0}, new int[]{1, 1}, new int[]{2, 2});
        String encoded = GameStateCodec.encodeWinningCells(cells);
        assertEquals("0,0;1,1;2,2", encoded);

        List<int[]> decoded = GameStateCodec.decodeWinningCells(encoded);
        assertEquals(3, decoded.size());
        assertEquals(0, decoded.get(0)[0]);
        assertEquals(2, decoded.get(2)[1]);
    }

    @Test
    void emptyWinningCellsRoundTripToEmptyList() {
        assertTrue(GameStateCodec.encodeWinningCells(List.of()).isEmpty());
        assertTrue(GameStateCodec.decodeWinningCells("").isEmpty());
        assertTrue(GameStateCodec.decodeWinningCells(null).isEmpty());
    }

    @Test
    void moveHistoryEncodesAndDecodesRoundTrip() {
        List<Move> moves = List.of(
                new Move(1, Player.X, 0, 0),
                new Move(2, Player.O, 1, 1)
        );
        String encoded = GameStateCodec.encodeMoveHistory(moves);
        assertEquals("1,X,0,0|2,O,1,1", encoded);

        List<Move> decoded = GameStateCodec.decodeMoveHistory(encoded);
        assertEquals(2, decoded.size());
        assertEquals(1, decoded.get(0).getMoveNumber());
        assertEquals(Player.O, decoded.get(1).getPlayer());
        assertEquals(1, decoded.get(1).getRow());
    }

    @Test
    void inProgressGameSurvivesRoundTripAndStaysPlayable() {
        Scoreboard scoreboard = new Scoreboard();
        GameSession original = new GameSession("game-1", GameMode.TWO_PLAYER, scoreboard);
        original.makeMove(Player.X, 0, 0);
        original.makeMove(Player.O, 1, 1);

        PersistedGame row = toRow(original);
        GameSession restored = GameStateCodec.toSession(row, new Scoreboard());

        assertEquals(original.getId(), restored.getId());
        assertEquals(GameStatus.IN_PROGRESS, restored.getStatus());
        assertEquals(original.getCurrentPlayer(), restored.getCurrentPlayer());
        assertEquals(original.getBoard().toEncoded(), restored.getBoard().toEncoded());
        assertEquals(2, restored.getMoveHistory().size());
        assertFalse(restored.isScoreCounted());
        assertFalse(restored.canUndo(), "undo stack is not persisted, so a restored game has nothing to undo");

        // Restored session must still be fully playable going forward.
        restored.makeMove(Player.X, 2, 2);
        assertEquals(3, restored.getMoveHistory().size());
    }

    @Test
    void wonGameSurvivesRoundTripAndRejectsFurtherMoves() {
        Scoreboard scoreboard = new Scoreboard();
        GameSession original = new GameSession("game-2", GameMode.TWO_PLAYER, scoreboard);
        original.makeMove(Player.X, 0, 0);
        original.makeMove(Player.O, 1, 0);
        original.makeMove(Player.X, 0, 1);
        original.makeMove(Player.O, 1, 1);
        original.makeMove(Player.X, 0, 2); // X completes the top row

        assertEquals(GameStatus.WON, original.getStatus());

        PersistedGame row = toRow(original);
        Scoreboard restoredScoreboard = new Scoreboard();
        restoredScoreboard.restore(scoreboard.getXWins(), scoreboard.getOWins(), scoreboard.getDraws());
        GameSession restored = GameStateCodec.toSession(row, restoredScoreboard);

        assertEquals(GameStatus.WON, restored.getStatus());
        assertEquals(Player.X, restored.getWinner());
        assertEquals(3, restored.getWinningCells().size());
        assertTrue(restored.isScoreCounted());
        assertEquals(1, restoredScoreboard.getXWins());
        assertThrows(RuntimeException.class, () -> restored.makeMove(Player.O, 2, 0));
    }

    /** Mirrors exactly what GamePersistenceRepository.saveGame() sends to SQLite. */
    private static PersistedGame toRow(GameSession session) {
        return new PersistedGame(
                session.getId(),
                session.getMode().name(),
                session.getBoard().toEncoded(),
                session.getCurrentPlayer().name(),
                session.getStatus().name(),
                session.getWinner() == null ? null : session.getWinner().name(),
                GameStateCodec.encodeWinningCells(session.getWinningCells()),
                GameStateCodec.encodeMoveHistory(session.getMoveHistory()),
                session.isScoreCounted()
        );
    }
}
