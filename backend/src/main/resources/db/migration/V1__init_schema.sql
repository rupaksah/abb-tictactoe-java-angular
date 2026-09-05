-- Flyway migration, applied automatically at startup and tracked in
-- flyway_schema_history (see spring.flyway.* in application.yml). Content is
-- identical to the schema.sql this replaces; IF NOT EXISTS is kept as a
-- second line of defense on top of baseline-on-migrate for a tictactoe.db
-- that already has these tables from before Flyway was introduced.

CREATE TABLE IF NOT EXISTS scoreboard (
    id       INTEGER PRIMARY KEY CHECK (id = 1),
    x_wins   INTEGER NOT NULL DEFAULT 0,
    o_wins   INTEGER NOT NULL DEFAULT 0,
    draws    INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS games (
    id              TEXT PRIMARY KEY,
    mode            TEXT NOT NULL,
    board           TEXT NOT NULL,
    current_player  TEXT NOT NULL,
    status          TEXT NOT NULL,
    winner          TEXT,
    winning_cells   TEXT NOT NULL DEFAULT '',
    move_history    TEXT NOT NULL DEFAULT '',
    score_counted   INTEGER NOT NULL DEFAULT 0,
    updated_at      TEXT NOT NULL DEFAULT (datetime('now'))
);
