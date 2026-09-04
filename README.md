# Tic Tac Toe — Angular + Java (Spring Boot) REST API

A browser-based Tic Tac Toe game built to the attached Round 2 problem
statement, with one deliberate substitution: **the backend is implemented in
Java (Spring Boot) instead of .NET**. See "Clarifications and Assumptions"
below for why, and confirm this substitution is acceptable to the review
panel before relying on this submission.

## Project Overview

Two players (or one player against a basic computer opponent) play Tic Tac
Toe in an Angular single-page app. The Angular frontend never computes game
rules itself — every move, undo, reset and scoreboard update is validated
and applied by the backend, and the frontend just renders whatever state
the backend returns. This mirrors the .NET/Angular split the original spec
describes, with a Spring Boot REST API standing in for the .NET Web API.

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | Angular 20 (standalone components, signals, `@if`/`@for` control flow), TypeScript |
| Backend | Java 21, Spring Boot 3.3 (Spring Web), springdoc-openapi (Swagger UI) |
| API style | REST, JSON |
| Storage | In-memory (`ConcurrentHashMap` of game sessions + a shared scoreboard) — no database |
| Source control | Git / GitHub |

## Features Implemented

- 3×3 clickable board; cells lock once filled.
- Two Player mode and Play Against Computer mode (human is always X,
  computer is always O).
- Turn indicator, alternating turns, invalid moves rejected without
  changing whose turn it is.
- Win detection (row, column, diagonal) with the winning cells highlighted,
  and draw detection when the board fills with no winner.
- Move history (move number, player, row/column) that updates after every
  valid move.
- Undo Last Move, mode-aware: removes one move in Two Player mode, removes
  the computer's move and the preceding human move together in Computer
  mode. Disabled when there's nothing to undo.
- Session-level scoreboard (X wins / O wins / draws), served by the
  backend, updated exactly once per completed game, unaffected by Reset
  Game, with its own Reset Scoreboard action.
- Computer opponent using the exact priority order from the spec: win >
  block > center > corner > any available cell.
- Basic unit tests for the backend's game logic, HTTP-layer tests for the
  REST controllers, and Angular component/service tests for the frontend.
- A GitHub Actions CI workflow that runs both test suites on every push/PR.
- Interactive API docs (Swagger UI / OpenAPI) generated from the
  controllers, no hand-written spec file.
- Request-shape validation on `POST /api/games/{id}/moves` (missing
  `player`, or a `row`/`col`/`cellIndex` outside its valid range, is
  rejected with `400 VALIDATION_ERROR` before it reaches the game logic).

## How to Run the Backend Locally

Requires JDK 21 and Maven (a `mvn` on your PATH; no separate install needed
beyond that — Maven will pull the Spring Boot dependencies itself).

```bash
cd backend
mvn spring-boot:run
```

The API starts on **http://localhost:8080**. CORS is pre-configured to
allow requests from `http://localhost:4200` (the Angular dev server).

Interactive API docs (Swagger UI) are at **http://localhost:8080/swagger-ui.html**,
generated automatically from the controllers — the raw OpenAPI JSON is at
`/v3/api-docs`. Useful for the panel to poke at the API directly without
the frontend, or without reading the table below.

## How to Run the Frontend Locally

Requires Node.js 20+ and npm.

```bash
cd frontend
npm install
npm start
```

Open **http://localhost:4200**. The app creates a new Two Player game on
load; use the mode toggle at the top to switch to Play Against Computer
(switching modes starts a fresh game — the scoreboard carries over, since
it's tracked server-side independent of any one game).

Start the backend first, or before making the first move — the frontend
will show an inline error banner if it can't reach `http://localhost:8080`.

## API Endpoint Summary

All endpoints are under `/api`, exchange JSON, and return the errors
described below as `{ "error": "<CODE>", "message": "<human-readable>" }`.
This table is a quick reference; the live, always-in-sync version is the
Swagger UI at `/swagger-ui.html` once the backend is running (see above).

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/games` | Create a new game. Body: `{ "mode": "TWO_PLAYER" \| "VS_COMPUTER" }` (optional, defaults to `TWO_PLAYER`). |
| GET | `/api/games/{id}` | Get the current state of a game. |
| POST | `/api/games/{id}/moves` | Submit a move. Body: `{ "player": "X"\|"O", "row": 0-2, "col": 0-2 }` (or `"cellIndex": 0-8` instead of row/col). |
| POST | `/api/games/{id}/undo` | Undo the last move (or move pair in Computer mode). |
| POST | `/api/games/{id}/reset` | Reset the board/history/status for a fresh game in the same mode; scoreboard untouched. |
| GET | `/api/scoreboard` | Get the session-level scoreboard. |
| POST | `/api/scoreboard/reset` | Reset the scoreboard to zero. |

**Game state response** (returned by create/get/move/undo/reset):

```json
{
  "gameId": "b3f1...",
  "board": [["X", null, null], [null, "O", null], [null, null, null]],
  "currentPlayer": "X",
  "gameMode": "TWO_PLAYER",
  "status": "InProgress",
  "winner": null,
  "winningCells": [],
  "moveHistory": [{ "moveNumber": 1, "player": "X", "row": 0, "col": 0 }],
  "canUndo": true,
  "scoreboard": { "xWins": 0, "oWins": 0, "draws": 0 }
}
```

`status` is one of `InProgress`, `Won`, `Draw`. Invalid moves return
`400 INVALID_MOVE`; a malformed move request (missing `player`, or a
row/col/cellIndex out of range) returns `400 VALIDATION_ERROR`; undo with
nothing to undo (or after completion) returns `409 UNDO_NOT_ALLOWED`; an
unknown game id returns `404 GAME_NOT_FOUND`.

### Trying the API directly (curl)

With the backend running (`mvn spring-boot:run`, see below), these work
standalone — no frontend needed:

```bash
# Create a Two Player game (note the "gameId" in the response)
curl -s -X POST http://localhost:8080/api/games \
  -H "Content-Type: application/json" \
  -d '{"mode":"TWO_PLAYER"}'

# Create a Play-Against-Computer game instead
curl -s -X POST http://localhost:8080/api/games \
  -H "Content-Type: application/json" \
  -d '{"mode":"VS_COMPUTER"}'

# Submit X's move at the center cell (replace GAME_ID with the id from create)
curl -s -X POST http://localhost:8080/api/games/GAME_ID/moves \
  -H "Content-Type: application/json" \
  -d '{"player":"X","row":1,"col":1}'

# Get current state
curl -s http://localhost:8080/api/games/GAME_ID

# Undo the last move
curl -s -X POST http://localhost:8080/api/games/GAME_ID/undo

# Reset the board (scoreboard untouched)
curl -s -X POST http://localhost:8080/api/games/GAME_ID/reset

# Scoreboard
curl -s http://localhost:8080/api/scoreboard
curl -s -X POST http://localhost:8080/api/scoreboard/reset
```

A ready-made request collection covering all seven endpoints (including the
validation-error and game-not-found cases) is also included at
[`postman_collection.json`](./postman_collection.json) — import it into
Postman/Insomnia and set the `baseUrl` variable (defaults to
`http://localhost:8080`).

## How to Run Tests

**Backend** (JUnit 5, via Maven):

```bash
cd backend
mvn test
```

Covers two layers: `GameSessionTest`/`ComputerPlayerTest`/`WinCheckerTest`/
`GameServiceTest` exercise the domain logic directly (valid/invalid move,
turn switching, row/column/diagonal win, draw, reset, undo in both modes,
undo-disabled cases, scoreboard updating exactly once, move-after-completion,
and the computer's move-selection priority); `GameControllerTest`/
`ScoreboardControllerTest` drive the actual REST endpoints through
`MockMvc` with `GameService` mocked out, asserting HTTP status codes and
JSON response shape (including the `GlobalExceptionHandler`'s error
mapping) — a level neither of the other test classes touches.

**Frontend** (Jasmine/Karma, via Angular CLI):

```bash
cd frontend
npm test
```

Covers component rendering (board clicks and disabled/winning states,
scoreboard and move-history rendering) and the `GameService`'s HTTP
integration (request shapes, response handling, and error surfacing),
using Angular's `HttpClientTestingModule`-style testing providers so no
real backend is needed to run them.

## Continuous Integration

`.github/workflows/ci.yml` runs on every push/PR to `main`: one job runs
`mvn test` (Java 21, Temurin), a separate job runs `npm ci`, `npm run
build`, and `npm test -- --watch=false --browsers=ChromeHeadless` for the
frontend. Both must pass before a PR is considered mergeable in normal use.

## AI Tools and Prompt Summary

This solution was built end-to-end by Claude (Anthropic), in an agentic
coding session, from this repository's problem statement plus one
directive from the requester: **"build it with Java as the backend."**

- **Specification → plan**: the problem statement (a `.docx`) was parsed
  and turned into an implementation plan covering the game engine, the
  seven REST endpoints, the undo-by-mode rule, the computer's move
  priority, and the required test scenarios, before any code was written.
- **What the AI generated**: all backend Java (model/core game engine/DTOs/
  service/controllers/config/exception handling), all Angular
  TypeScript/HTML/CSS, and both test suites.
- **A constraint that shaped the build, and how it was handled**: the
  sandboxed development environment this was built in had no network
  access to Maven Central (or any Maven mirror) — only npm, PyPI and a few
  other package registries were reachable. That meant the Spring Boot
  project could be *written* but not `mvn`-compiled or `mvn test`-run
  inside that sandbox. To still get real verification rather than shipping
  untested code:
  1. The entire game engine (`Board`, `WinChecker`, `ComputerPlayer`,
     `GameSession`, `Scoreboard` — everything under
     `backend/.../core`) was written with zero framework dependencies,
     specifically so it could be compiled and run with plain `javac`/`java`.
  2. A standalone harness exercising all 19 "Testing Expectations" scenarios
     (plus the computer AI's 5 priority branches) was run against that
     exact code: **49/49 checks passed.**
  3. The Spring MVC/Boot-facing code (controllers, `GameService`,
     `GlobalExceptionHandler`, `CorsConfig`) was then compile-checked
     against hand-written stubs of the real Spring Boot 3.3 API surface
     (`@RestController`, `ResponseEntity`, `@RestControllerAdvice`, etc.)
     to catch wiring/syntax errors before they'd surface in `mvn test` —
     this compiled clean, including the JUnit test files.
  4. The Angular side had no such restriction (npm was reachable), so it
     was scaffolded, built (`ng build`), and its full test suite was
     actually run headlessly — **19/19 tests passed** — inside the sandbox.
  5. Net effect: `mvn test` and `mvn spring-boot:run` were not directly
     exercised by the AI in that sandbox. **This has since been confirmed
     working**: the requester ran the full stack locally (`mvn test`, `mvn
     spring-boot:run`, `ng serve`) and played the game end-to-end — see
     "Known Limitations" for what that did and didn't cover.
  6. A follow-up round added `GameControllerTest`/`ScoreboardControllerTest`
     (HTTP-layer tests via `MockMvc`), SLF4J logging in `GameService`, and
     the GitHub Actions CI workflow — same compile-checked-against-stubs
     verification approach as the rest of the Spring-facing code, for the
     same underlying sandbox-network reason.
  7. A further round added springdoc-openapi (Swagger UI) to `pom.xml` plus
     `@Tag`/`@Operation` annotations on the controllers and an
     `OpenApiConfig` bean. This was a *new* Maven dependency, so it carried
     one more unknown on top of the usual stub-compiled-only caveat: whether
     the coordinates/version resolve cleanly at all. **Confirmed working**
     by the requester running `mvn spring-boot:run` and opening
     `/swagger-ui.html` locally.
  8. A final round added Bean Validation (`spring-boot-starter-validation`,
     `jakarta.validation.constraints` on `MoveRequest`, `@Valid` on the move
     endpoint, and a `MethodArgumentNotValidException` handler in
     `GlobalExceptionHandler`) plus two new `GameControllerTest` cases for
     it, curl examples, and a Postman collection. Same verification
     approach as the rest of the Spring-facing code (compiled clean against
     hand-written stubs of `jakarta.validation`/
     `MethodArgumentNotValidException`/`BindingResult`/`FieldError`, never
     run against the real dependency).
  9. The requester then ran `mvn test` for real for the first time (Java 21,
     against actual Spring Boot/Jackson jars, not the sandbox's stubs) and it
     caught a genuine bug the stub-based approach structurally could not:
     `ScoreboardControllerTest` failed with `No results for path: $.xWins`.
     Root cause: `ScoreboardResponse`'s `getXWins()`/`getOWins()` getters hit
     a JavaBeans naming edge case — `java.beans.Introspector.decapitalize`
     only lowercases a getter's leading capital when the *second* character
     is lowercase; since "XWins" and "OWins" both start with two capitals
     (`X`/`W`, `O`/`W`), Jackson's default bean-property naming left them
     capitalized, so the API was actually serving `{"XWins":...,"OWins":...}`
     instead of the documented `{"xWins":...,"oWins":...}` — silently
     mismatched against the Angular scoreboard component's field names.
     Fixed with explicit `@JsonProperty("xWins")`/`@JsonProperty("oWins")` on
     the two fields, confirmed no other DTO has the same
     two-capitals-in-a-row pattern, and recompiled clean against the stubs
     (with a new `JsonProperty` stub added). **This fix has not yet been
     re-confirmed by an actual `mvn test` run** — that's the immediate next
     step before treating it as done; see "Known Limitations."
- **What was changed manually**: nothing — this was reviewed but not
  hand-edited after generation. It's the AI-generated code as-is, so it
  should be reviewed with that in mind rather than presented as
  independently hand-verified line-by-line.
- **What was reviewed carefully**: the undo semantics (per-mode move-pair
  removal, and the Option A "undo disabled after completion" policy), the
  scoreboard's exactly-once-per-game update, the computer opponent's
  priority order, and the REST contract's status codes/error shapes.
- **Assumptions made**: see "Clarifications and Assumptions" below.
- **Trade-offs chosen**: Java/Spring Boot over .NET (explicit ask, but a
  deviation from the written spec — flagged, not hidden); Option A over
  Option B for the undo/scoreboard interaction (simpler, and removes an
  entire class of scoreboard-consistency bugs); framework-free core game
  logic over a "thinner" Spring-only design, specifically to make the logic
  independently testable and reviewable outside the framework.

## Design Decisions

- **Undo + Scoreboard (Clarification 2): Option A — Disable Undo After
  Completion.** Once a game is Won or Draw, Undo is disabled and that
  game's scoreboard entry is final. This was chosen over Option B because
  it keeps "the scoreboard updates exactly once per completed game" true
  by construction, with no need to reverse or re-derive a score.
- **Undo granularity via snapshots.** Rather than replaying moves, each
  `GameSession` pushes a full state snapshot (board, turn, status, move
  history) before each "turn" (one move in Two Player mode; the human+
  computer pair in Computer mode) and undo simply restores the most recent
  snapshot. This makes the mode-dependent undo rule a natural consequence
  of *when* a snapshot is taken, rather than special-cased logic in the
  undo path itself.
- **The computer's reply is applied server-side, synchronously, as part of
  the human's move request.** The frontend never asks separately for the
  computer's move, and the API rejects a client-submitted move for O in
  Computer mode — this keeps "the backend owns game session state" simple
  and makes the undo-pair behavior well-defined.
- **The scoreboard is a single shared instance for the life of the server
  process** (session-level, not per-game), matching "maintain a
  session-level scoreboard" in the spec.
- **Framework-free core game logic** (`backend/.../core/*`), with Spring
  only in the outer service/controller layer. This was also what made it
  possible to genuinely unit-test and verify the logic inside a sandbox
  that couldn't reach Maven Central (see "AI Tools and Prompt Summary").

## Clarifications and Assumptions

- **Backend language substitution.** The problem statement specifies a
  .NET Web API backend; this submission uses Java/Spring Boot instead, per
  an explicit request from the person this was built for. **If you're
  submitting this for review against the original spec, confirm with the
  panel first that a Java backend is acceptable** — this is flagged here
  rather than silently substituted.
- Move requests accept either `row`+`col` or a single `cellIndex`
  (row-major, 0–8); the frontend always sends `row`+`col`.
- `currentPlayer` is left as whoever just moved once a game ends (rather
  than being cleared), since no further move is possible anyway; the
  frontend doesn't rely on it once `status !== "InProgress"`.
- The Angular app auto-starts a Two Player game on load rather than
  showing a separate "start screen," to match "the UI should show: game
  board, current player, …" as an always-on-screen state.
- No authentication, persistence beyond process lifetime, or multi-user
  session isolation was in scope per "in-memory storage is acceptable."

## Known Limitations

- **The AI still hasn't run the backend's real build directly** — every
  Spring-facing file (controllers, `GameService`, `GlobalExceptionHandler`,
  `CorsConfig`) was only compile-checked against hand-written API stubs in
  the sandbox, never against real Spring Boot jars (no Maven Central access
  there). What *has* been confirmed by the requester running locally:
  `mvn test` / `mvn spring-boot:run` / `ng serve` and playing full games,
  separately the Swagger UI at `/swagger-ui.html`, and — most recently —
  a full `mvn test` run against the real toolchain, which is what actually
  caught the `ScoreboardResponse` xWins/oWins JSON-naming bug described in
  "AI Tools and Prompt Summary" above (point 9). That's the clearest
  evidence yet of why "compiled against stubs" and "actually run" aren't
  the same claim: the stub compile passed every time; only the real
  Jackson runtime exposed the bug. Plus, independent of any local run, the
  framework-free game engine via 49/49 real assertions in a standalone
  harness. **Not yet independently re-confirmed**: the `@JsonProperty` fix
  for xWins/oWins — run `mvn test` again and confirm
  `ScoreboardControllerTest` now passes, and separately eyeball the
  frontend's scoreboard display (win counts, not just the draws counter)
  during a real game, since that's the field that was silently broken.
- No persistence: restarting the backend clears all games and the
  scoreboard (acceptable per the spec's "in-memory storage is
  acceptable").
- No reconnect/multi-tab story: the frontend holds one active `gameId` in
  memory; refreshing the browser starts a new game rather than resuming.
- The API base URL (`http://localhost:8080/api`) is hardcoded in
  `game.service.ts` rather than pulled from an Angular `environment.ts` —
  fine for local review, not for a real deployment.
- No rate limiting, auth, or protection against a client spoofing another
  player's moves beyond the current-turn/mode checks already in place.

## Future Improvements

- A `Dockerfile`/`docker-compose.yml` so the whole stack runs with one
  command instead of two terminals.
- Environment-based API URL configuration for non-local deployments.
- Optional SQLite persistence for games/scoreboard across restarts (the
  spec allows this).
- A minimax-based "hard" computer difficulty alongside the current
  priority-rule opponent.
- WebSocket/SSE push for a real two-browser-tab multiplayer experience
  instead of the current single-tab, backend-as-source-of-truth model.
