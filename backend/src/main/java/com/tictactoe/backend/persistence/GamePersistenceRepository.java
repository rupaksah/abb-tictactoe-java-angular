package com.tictactoe.backend.persistence;

import com.tictactoe.backend.core.GameSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Plain-JDBC persistence for games and the scoreboard, backed by SQLite (the
 * problem statement explicitly allows this as an alternative to the default
 * in-memory storage). Uses {@link JdbcTemplate} directly rather than
 * JPA/Hibernate, to avoid depending on a Hibernate SQLite dialect and to
 * keep the SQL - and its failure modes - fully visible and easy to review.
 *
 * Every method here is a straightforward upsert/select; the schema lives in
 * {@code src/main/resources/schema.sql} and is applied automatically by
 * Spring Boot at startup (see {@code spring.sql.init.mode=always} in
 * application.yml).
 */
@Repository
public class GamePersistenceRepository {

    private final JdbcTemplate jdbc;

    public GamePersistenceRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public void saveScoreboard(int xWins, int oWins, int draws) {
        jdbc.update(
                "INSERT INTO scoreboard (id, x_wins, o_wins, draws) VALUES (1, ?, ?, ?) " +
                        "ON CONFLICT(id) DO UPDATE SET x_wins = excluded.x_wins, " +
                        "o_wins = excluded.o_wins, draws = excluded.draws",
                xWins, oWins, draws);
    }

    public Optional<int[]> loadScoreboard() {
        List<int[]> rows = jdbc.query(
                "SELECT x_wins, o_wins, draws FROM scoreboard WHERE id = 1",
                (rs, rowNum) -> new int[]{rs.getInt("x_wins"), rs.getInt("o_wins"), rs.getInt("draws")});
        return rows.isEmpty() ? Optional.empty() : Optional.of(rows.get(0));
    }

    public void saveGame(GameSession session) {
        jdbc.update(
                "INSERT INTO games (id, mode, board, current_player, status, winner, " +
                        "winning_cells, move_history, score_counted, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now')) " +
                        "ON CONFLICT(id) DO UPDATE SET mode = excluded.mode, board = excluded.board, " +
                        "current_player = excluded.current_player, status = excluded.status, " +
                        "winner = excluded.winner, winning_cells = excluded.winning_cells, " +
                        "move_history = excluded.move_history, score_counted = excluded.score_counted, " +
                        "updated_at = excluded.updated_at",
                session.getId(),
                session.getMode().name(),
                session.getBoard().toEncoded(),
                session.getCurrentPlayer().name(),
                session.getStatus().name(),
                session.getWinner() == null ? null : session.getWinner().name(),
                GameStateCodec.encodeWinningCells(session.getWinningCells()),
                GameStateCodec.encodeMoveHistory(session.getMoveHistory()),
                session.isScoreCounted() ? 1 : 0
        );
    }

    public List<PersistedGame> loadAllGames() {
        return jdbc.query(
                "SELECT id, mode, board, current_player, status, winner, " +
                        "winning_cells, move_history, score_counted FROM games",
                (rs, rowNum) -> new PersistedGame(
                        rs.getString("id"),
                        rs.getString("mode"),
                        rs.getString("board"),
                        rs.getString("current_player"),
                        rs.getString("status"),
                        rs.getString("winner"),
                        rs.getString("winning_cells"),
                        rs.getString("move_history"),
                        rs.getInt("score_counted") != 0
                ));
    }
}
