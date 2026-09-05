package com.tictactoe.backend.service;

import com.tictactoe.backend.core.GameSession;
import com.tictactoe.backend.core.Scoreboard;
import com.tictactoe.backend.dto.CellPosition;
import com.tictactoe.backend.dto.GameStateResponse;
import com.tictactoe.backend.dto.MoveHistoryItem;
import com.tictactoe.backend.dto.MoveRequest;
import com.tictactoe.backend.dto.ScoreboardResponse;
import com.tictactoe.backend.exception.GameNotFoundException;
import com.tictactoe.backend.model.GameMode;
import com.tictactoe.backend.model.GameStatus;
import com.tictactoe.backend.model.Move;
import com.tictactoe.backend.model.Player;
import com.tictactoe.backend.persistence.GamePersistenceRepository;
import com.tictactoe.backend.persistence.GameStateCodec;
import com.tictactoe.backend.persistence.PersistedGame;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Application-level orchestration on top of the framework-free
 * {@link GameSession} / {@link Scoreboard} core: owns the in-memory session
 * map, translates DTOs to/from the domain model, and is the only place that
 * knows about game IDs. The scoreboard is a single instance shared across
 * all games for the life of the server process ("session-level scoreboard"
 * per the problem statement).
 *
 * <p>Persistence: when {@code app.persistence.enabled} is true (the
 * default), every mutation is also written through to the SQLite-backed
 * {@link GamePersistenceRepository}, and all games/the scoreboard are
 * reloaded from it on startup - the problem statement explicitly allows
 * SQLite as an alternative to plain in-memory storage. This is deliberately
 * best-effort: a persistence failure is logged and swallowed rather than
 * failing the request, so a SQLite/disk problem degrades to in-memory-only
 * behavior instead of taking the API down. Setting
 * {@code app.persistence.enabled=false} (e.g. via an environment variable)
 * disables it entirely without touching any other configuration.</p>
 *
 * <p>{@code @Slf4j} (Lombok) generates the same
 * {@code private static final Logger log = LoggerFactory.getLogger(...)}
 * field this class previously declared by hand.</p>
 */
@Slf4j
@Service
public class GameService {

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final Scoreboard scoreboard = new Scoreboard();
    private final GamePersistenceRepository repository;
    private final boolean persistenceEnabled;

    /** Spring-managed constructor: wires the real SQLite-backed repository and the feature flag. */
    @Autowired
    public GameService(GamePersistenceRepository repository,
                        @Value("${app.persistence.enabled:true}") boolean persistenceEnabled) {
        this.repository = repository;
        this.persistenceEnabled = persistenceEnabled;
    }

    /**
     * Convenience constructor for plain-Java unit tests (see GameServiceTest):
     * runs with persistence forced off, so it never touches the (unusable,
     * no-DataSource) repository instance underneath it.
     */
    public GameService() {
        this(new GamePersistenceRepository(null), false);
    }

    /** Reloads every persisted game and the scoreboard from SQLite at startup, if persistence is enabled. */
    @PostConstruct
    void loadPersistedState() {
        if (!persistenceEnabled) {
            log.info("Persistence disabled (app.persistence.enabled=false); starting with an empty in-memory store");
            return;
        }
        try {
            repository.loadScoreboard().ifPresent(counts -> scoreboard.restore(counts[0], counts[1], counts[2]));
            for (PersistedGame row : repository.loadAllGames()) {
                GameSession session = GameStateCodec.toSession(row, scoreboard);
                sessions.put(session.getId(), session);
            }
            log.info("Restored {} game(s) and scoreboard ({} X / {} O / {} draws) from SQLite",
                    sessions.size(), scoreboard.getXWins(), scoreboard.getOWins(), scoreboard.getDraws());
        } catch (RuntimeException ex) {
            log.warn("Failed to restore persisted state from SQLite; starting fresh in-memory instead. Cause: {}",
                    ex.getMessage());
        }
    }

    public GameStateResponse createGame(GameMode requestedMode) {
        GameMode mode = requestedMode == null ? GameMode.TWO_PLAYER : requestedMode;
        String id = UUID.randomUUID().toString();
        GameSession session = new GameSession(id, mode, scoreboard);
        sessions.put(id, session);
        log.info("Created game {} in mode {}", id, mode);
        persistGame(session);
        return toResponse(session);
    }

    public GameStateResponse getGame(String gameId) {
        return toResponse(find(gameId));
    }

    public GameStateResponse makeMove(String gameId, MoveRequest request) {
        GameSession session = find(gameId);
        if (request.getPlayer() == null) {
            throw new IllegalArgumentException("player is required");
        }
        int[] target = resolveCell(request);
        try {
            session.makeMove(request.getPlayer(), target[0], target[1]);
        } catch (RuntimeException ex) {
            log.debug("Rejected move for game {} ({}): {}", gameId, request.getPlayer(), ex.getMessage());
            throw ex;
        }
        log.info("Game {}: {} moved to ({},{}); status={}", gameId, request.getPlayer(),
                target[0], target[1], session.getStatus());
        persistGame(session);
        persistScoreboard();
        return toResponse(session);
    }

    public GameStateResponse undo(String gameId) {
        GameSession session = find(gameId);
        session.undo();
        log.info("Game {}: undo applied", gameId);
        persistGame(session);
        return toResponse(session);
    }

    public GameStateResponse resetGame(String gameId) {
        GameSession session = find(gameId);
        session.reset();
        log.info("Game {}: reset (scoreboard untouched)", gameId);
        persistGame(session);
        return toResponse(session);
    }

    public ScoreboardResponse getScoreboard() {
        return toScoreboardResponse();
    }

    public ScoreboardResponse resetScoreboard() {
        scoreboard.reset();
        log.info("Scoreboard reset to zero");
        persistScoreboard();
        return toScoreboardResponse();
    }

    /** Best-effort write-through to SQLite: logs and swallows failures rather than failing the request. */
    private void persistGame(GameSession session) {
        if (!persistenceEnabled) {
            return;
        }
        try {
            repository.saveGame(session);
        } catch (RuntimeException ex) {
            log.warn("Failed to persist game {} to SQLite: {}", session.getId(), ex.getMessage());
        }
    }

    private void persistScoreboard() {
        if (!persistenceEnabled) {
            return;
        }
        try {
            repository.saveScoreboard(scoreboard.getXWins(), scoreboard.getOWins(), scoreboard.getDraws());
        } catch (RuntimeException ex) {
            log.warn("Failed to persist scoreboard to SQLite: {}", ex.getMessage());
        }
    }

    private GameSession find(String gameId) {
        GameSession session = sessions.get(gameId);
        if (session == null) {
            log.warn("Game not found: {}", gameId);
            throw new GameNotFoundException(gameId);
        }
        return session;
    }

    /** Resolves row/col from either explicit row+col or a 0-based cellIndex (row-major). */
    private int[] resolveCell(MoveRequest request) {
        if (request.getRow() != null && request.getCol() != null) {
            return new int[]{request.getRow(), request.getCol()};
        }
        if (request.getCellIndex() != null) {
            int idx = request.getCellIndex();
            return new int[]{idx / 3, idx % 3};
        }
        throw new IllegalArgumentException("Either row+col or cellIndex must be provided");
    }

    private GameStateResponse toResponse(GameSession session) {
        GameStateResponse response = new GameStateResponse();
        response.setGameId(session.getId());
        response.setBoard(session.getBoard().toStringGrid());
        response.setCurrentPlayer(session.getCurrentPlayer().name());
        response.setGameMode(session.getMode().name());
        response.setStatus(toStatusString(session.getStatus()));
        response.setWinner(session.getWinner() == null ? null : session.getWinner().name());
        response.setWinningCells(session.getWinningCells().stream()
                .map(cell -> new CellPosition(cell[0], cell[1]))
                .collect(Collectors.toList()));
        response.setMoveHistory(toMoveHistory(session.getMoveHistory()));
        response.setCanUndo(session.canUndo());
        response.setScoreboard(toScoreboardResponse());
        return response;
    }

    private List<MoveHistoryItem> toMoveHistory(List<Move> moves) {
        return moves.stream()
                .map(m -> new MoveHistoryItem(m.getMoveNumber(), m.getPlayer().name(), m.getRow(), m.getCol()))
                .collect(Collectors.toList());
    }

    private ScoreboardResponse toScoreboardResponse() {
        return new ScoreboardResponse(scoreboard.getXWins(), scoreboard.getOWins(), scoreboard.getDraws());
    }

    private String toStatusString(GameStatus status) {
        return switch (status) {
            case IN_PROGRESS -> "InProgress";
            case WON -> "Won";
            case DRAW -> "Draw";
        };
    }
}
