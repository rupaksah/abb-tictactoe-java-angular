package com.tictactoe.backend.core;

import com.tictactoe.backend.model.GameMode;
import com.tictactoe.backend.model.GameStatus;
import com.tictactoe.backend.model.Move;
import com.tictactoe.backend.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers every scenario listed under "Testing Expectations" in the problem
 * statement: valid/invalid move, turn switching, row/column/diagonal win,
 * draw, reset, undo in both modes, scoreboard update (once per game), and
 * move-after-completion.
 *
 * These assertions were first validated with an identical, dependency-free
 * copy of GameSession/Board/WinChecker/ComputerPlayer run through a plain
 * javac/java harness (Maven Central was not reachable in the sandbox this
 * project was developed in) — see README "AI Tools and Prompt Summary" for
 * details. This class is the idiomatic JUnit 5 version meant to run via
 * `mvn test` wherever normal internet access is available.
 */
class GameSessionTest {

    private Scoreboard scoreboard;

    @BeforeEach
    void setUp() {
        scoreboard = new Scoreboard();
    }

    private GameSession twoPlayerSession() {
        return new GameSession("t1", GameMode.TWO_PLAYER, scoreboard);
    }

    private GameSession computerSession() {
        return new GameSession("c1", GameMode.VS_COMPUTER, scoreboard);
    }

    @Test
    void validMoveIsApplied() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        assertEquals(Player.X, s.getBoard().get(0, 0));
        assertEquals(1, s.getMoveHistory().size());
    }

    @Test
    void invalidMove_occupiedCellIsRejected() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        assertThrows(InvalidMoveException.class, () -> s.makeMove(Player.O, 0, 0));
    }

    @Test
    void invalidMove_outOfBoundsIsRejected() {
        GameSession s = twoPlayerSession();
        assertThrows(InvalidMoveException.class, () -> s.makeMove(Player.X, 5, 5));
        assertThrows(InvalidMoveException.class, () -> s.makeMove(Player.X, -1, 0));
    }

    @Test
    void invalidMove_wrongPlayerDoesNotChangeTurn() {
        GameSession s = twoPlayerSession();
        assertThrows(InvalidMoveException.class, () -> s.makeMove(Player.O, 0, 0));
        assertEquals(Player.X, s.getCurrentPlayer());
        assertTrue(s.getMoveHistory().isEmpty());
    }

    @Test
    void turnsAlternateAfterEveryValidMove() {
        GameSession s = twoPlayerSession();
        assertEquals(Player.X, s.getCurrentPlayer());
        s.makeMove(Player.X, 0, 0);
        assertEquals(Player.O, s.getCurrentPlayer());
        s.makeMove(Player.O, 1, 1);
        assertEquals(Player.X, s.getCurrentPlayer());
    }

    @Test
    void rowWinIsDetected() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        s.makeMove(Player.O, 1, 0);
        s.makeMove(Player.X, 0, 1);
        s.makeMove(Player.O, 1, 1);
        s.makeMove(Player.X, 0, 2);

        assertEquals(GameStatus.WON, s.getStatus());
        assertEquals(Player.X, s.getWinner());
        assertEquals(3, s.getWinningCells().size());
        assertThrows(InvalidMoveException.class, () -> s.makeMove(Player.O, 2, 2));
    }

    @Test
    void columnWinIsDetected() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        s.makeMove(Player.O, 0, 1);
        s.makeMove(Player.X, 1, 0);
        s.makeMove(Player.O, 0, 2);
        s.makeMove(Player.X, 2, 0);

        assertEquals(GameStatus.WON, s.getStatus());
        assertEquals(Player.X, s.getWinner());
    }

    @Test
    void diagonalWinIsDetected() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        s.makeMove(Player.O, 0, 1);
        s.makeMove(Player.X, 1, 1);
        s.makeMove(Player.O, 0, 2);
        s.makeMove(Player.X, 2, 2);

        assertEquals(GameStatus.WON, s.getStatus());
        assertEquals(Player.X, s.getWinner());
    }

    @Test
    void drawIsDetectedWhenBoardFillsWithNoWinner() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        s.makeMove(Player.O, 0, 1);
        s.makeMove(Player.X, 0, 2);
        s.makeMove(Player.O, 1, 1);
        s.makeMove(Player.X, 1, 0);
        s.makeMove(Player.O, 1, 2);
        s.makeMove(Player.X, 2, 1);
        s.makeMove(Player.O, 2, 0);
        s.makeMove(Player.X, 2, 2);

        assertEquals(GameStatus.DRAW, s.getStatus());
        assertNull(s.getWinner());
    }

    @Test
    void resetClearsGameButKeepsScoreboard() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        s.makeMove(Player.O, 1, 0);
        s.makeMove(Player.X, 0, 1);
        s.makeMove(Player.O, 1, 1);
        s.makeMove(Player.X, 0, 2); // X wins, scoreboard updated
        assertEquals(1, scoreboard.getXWins());

        s.reset();

        assertNull(s.getBoard().get(0, 0));
        assertTrue(s.getMoveHistory().isEmpty());
        assertEquals(Player.X, s.getCurrentPlayer());
        assertEquals(GameStatus.IN_PROGRESS, s.getStatus());
        assertFalse(s.canUndo());
        assertEquals(1, scoreboard.getXWins(), "Reset Game must not change the scoreboard");
    }

    @Test
    void undoInTwoPlayerModeRemovesOnlyTheLastMove() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        s.makeMove(Player.O, 1, 1);

        s.undo();

        assertNull(s.getBoard().get(1, 1));
        assertEquals(Player.X, s.getBoard().get(0, 0));
        assertEquals(Player.O, s.getCurrentPlayer());
        assertEquals(1, s.getMoveHistory().size());
    }

    @Test
    void undoInComputerModeRemovesTheMovePairTogether() {
        GameSession s = computerSession();
        s.makeMove(Player.X, 0, 0); // human plays, computer auto-replies
        assertEquals(2, s.getMoveHistory().size());
        Move computerMove = s.getMoveHistory().get(1);
        assertEquals(Player.O, computerMove.getPlayer());

        s.undo();

        assertNull(s.getBoard().get(0, 0));
        assertNull(s.getBoard().get(computerMove.getRow(), computerMove.getCol()));
        assertEquals(Player.X, s.getCurrentPlayer());
        assertTrue(s.getMoveHistory().isEmpty());
    }

    @Test
    void undoIsDisabledWhenThereAreNoMovesToUndo() {
        GameSession s = twoPlayerSession();
        assertFalse(s.canUndo());
        assertThrows(UndoNotAllowedException.class, s::undo);
    }

    @Test
    void undoIsDisabledAfterGameCompletion() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        s.makeMove(Player.O, 1, 0);
        s.makeMove(Player.X, 0, 1);
        s.makeMove(Player.O, 1, 1);
        s.makeMove(Player.X, 0, 2); // X wins

        assertFalse(s.canUndo());
        assertThrows(UndoNotAllowedException.class, s::undo);
    }

    @Test
    void scoreboardUpdatesExactlyOnceForACompletedGame() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        s.makeMove(Player.O, 1, 0);
        s.makeMove(Player.X, 0, 1);
        s.makeMove(Player.O, 1, 1);
        s.makeMove(Player.X, 0, 2); // X wins

        assertEquals(1, scoreboard.getXWins());

        assertThrows(InvalidMoveException.class, () -> s.makeMove(Player.O, 2, 2));
        assertEquals(1, scoreboard.getXWins(), "score must not be double-counted");
    }

    @Test
    void moveAfterGameCompletionIsRejected() {
        GameSession s = twoPlayerSession();
        s.makeMove(Player.X, 0, 0);
        s.makeMove(Player.O, 1, 0);
        s.makeMove(Player.X, 0, 1);
        s.makeMove(Player.O, 1, 1);
        s.makeMove(Player.X, 0, 2); // X wins

        assertThrows(InvalidMoveException.class, () -> s.makeMove(Player.O, 2, 2));
    }

    @Test
    void computerModeRejectsClientSubmittedMovesForO() {
        GameSession s = computerSession();
        assertThrows(InvalidMoveException.class, () -> s.makeMove(Player.O, 0, 0));
    }

    @Test
    void computerAutoMovesImmediatelyAfterHumanInVsComputerMode() {
        GameSession s = computerSession();
        s.makeMove(Player.X, 1, 1);

        assertEquals(2, s.getMoveHistory().size());
        assertEquals(Player.X, s.getCurrentPlayer());
        assertEquals(GameStatus.IN_PROGRESS, s.getStatus());
    }
}
