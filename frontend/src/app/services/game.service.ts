import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable, signal } from '@angular/core';
import { catchError, Observable, of, switchMap } from 'rxjs';
import { ApiErrorBody, GameMode, GameState, ScoreboardState } from '../models/game.models';

/**
 * Base URL of the Java (Spring Boot) backend that replaces the .NET Web API
 * named in the original spec. Not environment-configured on purpose, to
 * keep "npm start" + "mvn spring-boot:run" a true zero-config pairing for
 * local review, per the assignment's "easy for the panel to run"
 * requirement. See README "Known Limitations" for the production follow-up
 * (move to an environment.ts file per Angular convention).
 */
const API_BASE = 'http://localhost:8080/api';

/**
 * Single point of contact with the backend. The backend is the source of
 * truth for all game rules, move validation, status and the scoreboard
 * (see the problem statement's "Backend State Ownership" clarification) —
 * this service never computes game logic itself, it only sends requests
 * and republishes whatever state the backend returns.
 */
@Injectable({ providedIn: 'root' })
export class GameService {
  /** Current game state, or null before any game has been created. */
  readonly state = signal<GameState | null>(null);

  /** Most recent error message from a rejected request (e.g. an invalid move), or null. */
  readonly errorMessage = signal<string | null>(null);

  constructor(private readonly http: HttpClient) {}

  createGame(mode: GameMode): Observable<GameState> {
    return this.request(this.http.post<GameState>(`${API_BASE}/games`, { mode }));
  }

  refresh(gameId: string): Observable<GameState> {
    return this.request(this.http.get<GameState>(`${API_BASE}/games/${gameId}`));
  }

  makeMove(gameId: string, player: string, row: number, col: number): Observable<GameState> {
    return this.request(
      this.http.post<GameState>(`${API_BASE}/games/${gameId}/moves`, { player, row, col })
    );
  }

  undo(gameId: string): Observable<GameState> {
    return this.request(this.http.post<GameState>(`${API_BASE}/games/${gameId}/undo`, {}));
  }

  resetGame(gameId: string): Observable<GameState> {
    return this.request(this.http.post<GameState>(`${API_BASE}/games/${gameId}/reset`, {}));
  }

  /**
   * The reset-scoreboard endpoint returns a bare ScoreboardResponse, not a
   * full GameState, so this chains a refresh() of the current game
   * afterwards to keep the embedded scoreboard the UI reads in sync.
   */
  resetScoreboard(gameId: string): Observable<GameState> {
    this.errorMessage.set(null);
    return this.http.post<ScoreboardState>(`${API_BASE}/scoreboard/reset`, {}).pipe(
      switchMap(() => this.refresh(gameId)),
      catchError((err: HttpErrorResponse) => {
        this.setErrorFrom(err);
        return of(this.state() as GameState);
      })
    );
  }

  clearError(): void {
    this.errorMessage.set(null);
  }

  /** Shared plumbing: on success, publish the new state and clear any error; on failure, surface the error and leave state untouched. */
  private request(source: Observable<GameState>): Observable<GameState> {
    this.errorMessage.set(null);
    return source.pipe(
      switchMap((next) => {
        this.state.set(next);
        return of(next);
      }),
      catchError((err: HttpErrorResponse) => {
        this.setErrorFrom(err);
        return of(this.state() as GameState);
      })
    );
  }

  private setErrorFrom(err: HttpErrorResponse): void {
    const body = err.error as ApiErrorBody | undefined;
    this.errorMessage.set(
      body?.message ?? 'Request failed. Is the backend running on http://localhost:8080?'
    );
  }
}
