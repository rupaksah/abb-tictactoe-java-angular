package com.tictactoe.backend.persistence;

import com.tictactoe.backend.core.Board;
import com.tictactoe.backend.core.GameSession;
import com.tictactoe.backend.core.Scoreboard;
import com.tictactoe.backend.model.GameMode;
import com.tictactoe.backend.model.GameStatus;
import com.tictactoe.backend.model.Move;
import com.tictactoe.backend.model.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts between a live {@link GameSession} and the flat string columns
 * stored in the SQLite {@code games} table. Hand-rolled with simple
 * delimited strings rather than routed through Jackson/JSON, so the
 * persistence layer has no shared code path with (and can't be broken by)
 * the same kind of JSON bean-naming bug documented on
 * {@code ScoreboardResponse} in the README.
 */
public final class GameStateCodec {

    private GameStateCodec() {
    }

    /** "row,col;row,col;..." - empty string when there are no winning cells. */
    public static String encodeWinningCells(List<int[]> cells) {
        StringBuilder sb = new StringBuilder();
        for (int[] cell : cells) {
            if (sb.length() > 0) {
                sb.append(';');
            }
            sb.append(cell[0]).append(',').append(cell[1]);
        }
        return sb.toString();
    }

    public static List<int[]> decodeWinningCells(String encoded) {
        List<int[]> cells = new ArrayList<>();
        if (encoded == null || encoded.isBlank()) {
            return cells;
        }
        for (String part : encoded.split(";")) {
            String[] rc = part.split(",");
            cells.add(new int[]{Integer.parseInt(rc[0]), Integer.parseInt(rc[1])});
        }
        return cells;
    }

    /** "moveNumber,player,row,col|moveNumber,player,row,col|..." */
    public static String encodeMoveHistory(List<Move> moves) {
        StringBuilder sb = new StringBuilder();
        for (Move m : moves) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(m.getMoveNumber()).append(',').append(m.getPlayer().name())
                    .append(',').append(m.getRow()).append(',').append(m.getCol());
        }
        return sb.toString();
    }

    public static List<Move> decodeMoveHistory(String encoded) {
        List<Move> moves = new ArrayList<>();
        if (encoded == null || encoded.isBlank()) {
            return moves;
        }
        for (String part : encoded.split("\\|")) {
            String[] f = part.split(",");
            moves.add(new Move(Integer.parseInt(f[0]), Player.valueOf(f[1]),
                    Integer.parseInt(f[2]), Integer.parseInt(f[3])));
        }
        return moves;
    }

    /** Rebuilds a live {@link GameSession} from a persisted row. Undo history is not restored. */
    public static GameSession toSession(PersistedGame row, Scoreboard scoreboard) {
        Board board = Board.fromEncoded(row.board());
        Player winner = (row.winner() == null || row.winner().isBlank())
                ? null : Player.valueOf(row.winner());
        return new GameSession(
                row.id(),
                GameMode.valueOf(row.mode()),
                scoreboard,
                board,
                Player.valueOf(row.currentPlayer()),
                GameStatus.valueOf(row.status()),
                winner,
                decodeWinningCells(row.winningCells()),
                decodeMoveHistory(row.moveHistory()),
                row.scoreCounted()
        );
    }
}
