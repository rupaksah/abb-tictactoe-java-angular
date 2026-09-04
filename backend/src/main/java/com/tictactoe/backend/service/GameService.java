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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 */
@Service
public class GameService {

    private static final Logger log = LoggerFactory.getLogger(GameService.class);

    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    private final Scoreboard scoreboard = new Scoreboard();

    public GameStateResponse createGame(GameMode requestedMode) {
        GameMode mode = requestedMode == null ? GameMode.TWO_PLAYER : requestedMode;
        String id = UUID.randomUUID().toString();
        GameSession session = new GameSession(id, mode, scoreboard);
        sessions.put(id, session);
        log.info("Created game {} in mode {}", id, mode);
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
        return toResponse(session);
    }

    public GameStateResponse undo(String gameId) {
        GameSession session = find(gameId);
        session.undo();
        log.info("Game {}: undo applied", gameId);
        return toResponse(session);
    }

    public GameStateResponse resetGame(String gameId) {
        GameSession session = find(gameId);
        session.reset();
        log.info("Game {}: reset (scoreboard untouched)", gameId);
        return toResponse(session);
    }

    public ScoreboardResponse getScoreboard() {
        return toScoreboardResponse();
    }

    public ScoreboardResponse resetScoreboard() {
        scoreboard.reset();
        log.info("Scoreboard reset to zero");
        return toScoreboardResponse();
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
