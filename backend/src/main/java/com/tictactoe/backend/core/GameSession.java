package com.tictactoe.backend.core;

import com.tictactoe.backend.model.GameMode;
import com.tictactoe.backend.model.GameStatus;
import com.tictactoe.backend.model.Move;
import com.tictactoe.backend.model.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Owns a single game's full state: board, whose turn it is, status,
 * move history, and the undo stack. This is the backend's source of truth
 * for one game (see the "Backend State Ownership" clarification) and is
 * deliberately framework-free so its behaviour can be verified with a plain
 * `javac`/`java` harness, independent of Spring/Maven being resolvable.
 *
 * Undo policy: Option A ("Disable Undo After Completion") from the problem
 * statement's Clarification 2 was chosen. Once a game is Won or Draw, Undo
 * is disabled and the scoreboard entry for that game is final. This keeps
 * the scoreboard's "update only once per completed game" rule trivially
 * true, since a completed game's result can never be reversed.
 */
public final class GameSession {

    private final String id;
    private final GameMode mode;
    private final Scoreboard scoreboard;

    private Board board;
    private Player currentPlayer;
    private GameStatus status;
    private Player winner;
    private List<int[]> winningCells;
    private List<Move> moveHistory;
    private List<Snapshot> undoStack;
    private boolean scoreCounted;

    public GameSession(String id, GameMode mode, Scoreboard scoreboard) {
        this.id = id;
        this.mode = mode;
        this.scoreboard = scoreboard;
        this.board = new Board();
        this.currentPlayer = Player.X;
        this.status = GameStatus.IN_PROGRESS;
        this.winner = null;
        this.winningCells = new ArrayList<>();
        this.moveHistory = new ArrayList<>();
        this.undoStack = new ArrayList<>();
        this.scoreCounted = false;
    }

    /**
     * Applies a move submitted by `player` at (row, col). In VS_COMPUTER
     * mode, if the human's move doesn't end the game, the computer's reply
     * is computed and applied automatically as part of the same call, so
     * the caller always gets back a fully-settled state.
     */
    public synchronized void makeMove(Player player, int row, int col) {
        if (status != GameStatus.IN_PROGRESS) {
            throw new InvalidMoveException("Move rejected: the game has already completed");
        }
        if (!board.isInBounds(row, col)) {
            throw new InvalidMoveException("Move rejected: cell is outside the board");
        }
        if (!board.isEmpty(row, col)) {
            throw new InvalidMoveException("Move rejected: cell is already occupied");
        }
        if (player != currentPlayer) {
            throw new InvalidMoveException("Move rejected: it is not " + player + "'s turn");
        }
        if (mode == GameMode.VS_COMPUTER && player == Player.O) {
            throw new InvalidMoveException("Move rejected: O is controlled by the computer in this mode");
        }

        pushSnapshot();

        placeAndSettle(player, row, col);

        if (mode == GameMode.VS_COMPUTER && status == GameStatus.IN_PROGRESS) {
            int[] computerMove = ComputerPlayer.chooseMove(board, Player.O, Player.X);
            if (computerMove != null) {
                placeAndSettle(Player.O, computerMove[0], computerMove[1]);
            }
        }
    }

    /** Places the mark, records the move, and updates win/draw/scoreboard state. */
    private void placeAndSettle(Player player, int row, int col) {
        board.place(row, col, player);
        int moveNumber = moveHistory.size() + 1;
        moveHistory.add(new Move(moveNumber, player, row, col));

        WinResult result = WinChecker.checkWin(board);
        if (result.isWon()) {
            status = GameStatus.WON;
            winner = result.getWinner();
            winningCells = new ArrayList<>(result.getWinningCells());
            settleScoreboardOnce();
        } else if (WinChecker.isDraw(board, result)) {
            status = GameStatus.DRAW;
            winner = null;
            winningCells = new ArrayList<>();
            settleScoreboardOnce();
        } else {
            currentPlayer = player.opponent();
        }
    }

    private void settleScoreboardOnce() {
        if (scoreCounted) {
            return;
        }
        scoreCounted = true;
        if (status == GameStatus.WON) {
            scoreboard.recordWin(winner);
        } else if (status == GameStatus.DRAW) {
            scoreboard.recordDraw();
        }
    }

    /**
     * Reverts the board to the state before the most recent turn. In
     * TWO_PLAYER mode a "turn" is one move; in VS_COMPUTER mode a "turn" is
     * the human move plus the computer's automatic reply, so a single Undo
     * removes both together, per the problem statement's "Undo Behavior by
     * Mode" section.
     */
    public synchronized void undo() {
        if (undoStack.isEmpty()) {
            throw new UndoNotAllowedException("No moves to undo");
        }
        if (status != GameStatus.IN_PROGRESS) {
            throw new UndoNotAllowedException("Undo is disabled once the game has completed");
        }
        Snapshot snapshot = undoStack.remove(undoStack.size() - 1);
        this.board = new Board(snapshot.board);
        this.currentPlayer = snapshot.currentPlayer;
        this.status = snapshot.status;
        this.winner = snapshot.winner;
        this.winningCells = new ArrayList<>(snapshot.winningCells);
        this.moveHistory = new ArrayList<>(snapshot.moveHistory);
        // status was IN_PROGRESS at snapshot time (we only ever snapshot
        // before a turn is applied), so scoreCounted must revert to false.
        this.scoreCounted = false;
    }

    public boolean canUndo() {
        return !undoStack.isEmpty() && status == GameStatus.IN_PROGRESS;
    }

    /** Clears the board/history/status for a fresh game, keeping mode and leaving the scoreboard untouched. */
    public synchronized void reset() {
        this.board = new Board();
        this.currentPlayer = Player.X;
        this.status = GameStatus.IN_PROGRESS;
        this.winner = null;
        this.winningCells = new ArrayList<>();
        this.moveHistory = new ArrayList<>();
        this.undoStack = new ArrayList<>();
        this.scoreCounted = false;
    }

    private void pushSnapshot() {
        undoStack.add(new Snapshot(
                new Board(board),
                currentPlayer,
                status,
                winner,
                new ArrayList<>(winningCells),
                new ArrayList<>(moveHistory)
        ));
    }

    // ---- read-only accessors for the service/DTO layer ----

    public String getId() {
        return id;
    }

    public GameMode getMode() {
        return mode;
    }

    public Board getBoard() {
        return board;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Player getWinner() {
        return winner;
    }

    public List<int[]> getWinningCells() {
        return Collections.unmodifiableList(winningCells);
    }

    public List<Move> getMoveHistory() {
        return Collections.unmodifiableList(moveHistory);
    }

    /** Nested, immutable-in-practice snapshot used purely for undo. */
    private static final class Snapshot {
        final Board board;
        final Player currentPlayer;
        final GameStatus status;
        final Player winner;
        final List<int[]> winningCells;
        final List<Move> moveHistory;

        Snapshot(Board board, Player currentPlayer, GameStatus status, Player winner,
                 List<int[]> winningCells, List<Move> moveHistory) {
            this.board = board;
            this.currentPlayer = currentPlayer;
            this.status = status;
            this.winner = winner;
            this.winningCells = winningCells;
            this.moveHistory = moveHistory;
        }
    }
}
