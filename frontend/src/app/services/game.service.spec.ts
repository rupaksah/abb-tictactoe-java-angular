import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { GameService } from './game.service';
import { GameState } from '../models/game.models';

function sampleState(overrides: Partial<GameState> = {}): GameState {
  return {
    gameId: 'game-1',
    board: [
      [null, null, null],
      [null, null, null],
      [null, null, null],
    ],
    currentPlayer: 'X',
    gameMode: 'TWO_PLAYER',
    status: 'InProgress',
    winner: null,
    winningCells: [],
    moveHistory: [],
    canUndo: false,
    scoreboard: { xWins: 0, oWins: 0, draws: 0 },
    ...overrides,
  };
}

describe('GameService', () => {
  let service: GameService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [GameService, provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GameService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('createGame posts the requested mode and publishes the returned state', () => {
    service.createGame('VS_COMPUTER').subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/games');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ mode: 'VS_COMPUTER' });
    req.flush(sampleState({ gameMode: 'VS_COMPUTER' }));

    expect(service.state()?.gameMode).toBe('VS_COMPUTER');
    expect(service.errorMessage()).toBeNull();
  });

  it('makeMove posts to the moves endpoint with player/row/col', () => {
    service.makeMove('game-1', 'X', 1, 2).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/games/game-1/moves');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ player: 'X', row: 1, col: 2 });
    req.flush(sampleState());
  });

  it('undo posts to the undo endpoint', () => {
    service.undo('game-1').subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/games/game-1/undo');
    expect(req.request.method).toBe('POST');
    req.flush(sampleState());
  });

  it('resetGame posts to the reset endpoint', () => {
    service.resetGame('game-1').subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/games/game-1/reset');
    expect(req.request.method).toBe('POST');
    req.flush(sampleState());
  });

  it('resetScoreboard resets then refreshes the current game state', () => {
    service.resetScoreboard('game-1').subscribe();

    const resetReq = httpMock.expectOne('http://localhost:8080/api/scoreboard/reset');
    expect(resetReq.request.method).toBe('POST');
    resetReq.flush({ xWins: 0, oWins: 0, draws: 0 });

    const refreshReq = httpMock.expectOne('http://localhost:8080/api/games/game-1');
    expect(refreshReq.request.method).toBe('GET');
    refreshReq.flush(sampleState());

    expect(service.state()?.gameId).toBe('game-1');
  });

  it('publishes a friendly error message and leaves prior state alone on a rejected move', () => {
    service.createGame('TWO_PLAYER').subscribe();
    httpMock.expectOne('http://localhost:8080/api/games').flush(sampleState());

    service.makeMove('game-1', 'X', 0, 0).subscribe();
    const req = httpMock.expectOne('http://localhost:8080/api/games/game-1/moves');
    req.flush(
      { error: 'INVALID_MOVE', message: 'Move rejected: cell is already occupied' },
      { status: 400, statusText: 'Bad Request' }
    );

    expect(service.errorMessage()).toBe('Move rejected: cell is already occupied');
    expect(service.state()?.gameId).toBe('game-1');
  });
});
