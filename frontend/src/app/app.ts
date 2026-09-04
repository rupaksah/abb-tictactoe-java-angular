import { Component, OnInit, computed, inject } from '@angular/core';
import { BoardComponent } from './components/board/board.component';
import { MoveHistoryComponent } from './components/move-history/move-history.component';
import { ScoreboardComponent } from './components/scoreboard/scoreboard.component';
import { GameService } from './services/game.service';
import { GameMode } from './models/game.models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [BoardComponent, ScoreboardComponent, MoveHistoryComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App implements OnInit {
  private readonly gameService = inject(GameService);

  readonly state = this.gameService.state;
  readonly errorMessage = this.gameService.errorMessage;

  readonly boardDisabled = computed(() => {
    const s = this.state();
    return s === null || s.status !== 'InProgress';
  });

  readonly statusMessage = computed(() => {
    const s = this.state();
    if (!s) {
      return 'Starting a new game…';
    }
    if (s.status === 'Won') {
      return `${s.winner} wins!`;
    }
    if (s.status === 'Draw') {
      return "It's a draw!";
    }
    return `${s.currentPlayer}'s turn`;
  });

  ngOnInit(): void {
    this.startNewGame('TWO_PLAYER');
  }

  startNewGame(mode: GameMode): void {
    this.gameService.createGame(mode).subscribe();
  }

  onCellClicked(cell: { row: number; col: number }): void {
    const s = this.state();
    if (!s) {
      return;
    }
    this.gameService.makeMove(s.gameId, s.currentPlayer, cell.row, cell.col).subscribe();
  }

  onUndo(): void {
    const s = this.state();
    if (!s) {
      return;
    }
    this.gameService.undo(s.gameId).subscribe();
  }

  onResetGame(): void {
    const s = this.state();
    if (!s) {
      return;
    }
    this.gameService.resetGame(s.gameId).subscribe();
  }

  onResetScoreboard(): void {
    const s = this.state();
    if (!s) {
      return;
    }
    this.gameService.resetScoreboard(s.gameId).subscribe();
  }

  dismissError(): void {
    this.gameService.clearError();
  }
}
