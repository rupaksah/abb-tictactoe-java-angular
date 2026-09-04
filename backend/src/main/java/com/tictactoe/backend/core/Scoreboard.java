package com.tictactoe.backend.core;

import com.tictactoe.backend.model.Player;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Session-level (server-lifetime) scoreboard, shared across all games. Uses
 * AtomicInteger purely so concurrent requests from a single browser tab
 * (e.g. a fast double-click) can't corrupt the counts; the backend is still
 * the single source of truth per the "Backend State Ownership" clarification.
 */
public final class Scoreboard {
    private final AtomicInteger xWins = new AtomicInteger(0);
    private final AtomicInteger oWins = new AtomicInteger(0);
    private final AtomicInteger draws = new AtomicInteger(0);

    public void recordWin(Player winner) {
        if (winner == Player.X) {
            xWins.incrementAndGet();
        } else {
            oWins.incrementAndGet();
        }
    }

    public void recordDraw() {
        draws.incrementAndGet();
    }

    public void reset() {
        xWins.set(0);
        oWins.set(0);
        draws.set(0);
    }

    public int getXWins() {
        return xWins.get();
    }

    public int getOWins() {
        return oWins.get();
    }

    public int getDraws() {
        return draws.get();
    }
}
