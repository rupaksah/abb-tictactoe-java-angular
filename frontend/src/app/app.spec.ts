import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { App } from './app';
import { GameState } from './models/game.models';

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

describe('App', () => {
  let fixture: ComponentFixture<App>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    httpMock = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(App);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('creates a new Two Player game on init', () => {
    fixture.detectChanges(); // triggers ngOnInit

    const req = httpMock.expectOne('http://localhost:8080/api/games');
    expect(req.request.body).toEqual({ mode: 'TWO_PLAYER' });
    req.flush(sampleState());

    fixture.detectChanges();
    expect(fixture.componentInstance.state()?.gameId).toBe('game-1');
  });

  it('shows whose turn it is once a game has loaded', () => {
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/games').flush(sampleState());
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain("X's turn");
  });

  it('shows the winner message when the game is won', () => {
    fixture.detectChanges();
    httpMock
      .expectOne('http://localhost:8080/api/games')
      .flush(sampleState({ status: 'Won', winner: 'X' }));
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('X wins!');
  });

  it('starting a new game against the computer requests VS_COMPUTER mode', () => {
    fixture.detectChanges();
    httpMock.expectOne('http://localhost:8080/api/games').flush(sampleState());
    fixture.detectChanges();

    fixture.componentInstance.startNewGame('VS_COMPUTER');

    const req = httpMock.expectOne('http://localhost:8080/api/games');
    expect(req.request.body).toEqual({ mode: 'VS_COMPUTER' });
    req.flush(sampleState({ gameMode: 'VS_COMPUTER' }));
  });
});
