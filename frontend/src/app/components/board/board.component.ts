import { Component, computed, input, output } from '@angular/core';
import { Board, CellPosition } from '../../models/game.models';

@Component({
  selector: 'app-board',
  standalone: true,
  templateUrl: './board.component.html',
  styleUrl: './board.component.css',
})
export class BoardComponent {
  readonly board = input.required<Board>();
  readonly winningCells = input<CellPosition[]>([]);
  /** true once the game is Won/Draw, or before any game exists — cells stop being clickable. */
  readonly disabled = input<boolean>(false);

  readonly cellClicked = output<{ row: number; col: number }>();

  readonly rows = computed(() => [0, 1, 2]);
  readonly cols = computed(() => [0, 1, 2]);

  isWinningCell(row: number, col: number): boolean {
    return this.winningCells().some((cell) => cell.row === row && cell.col === col);
  }

  onCellClick(row: number, col: number): void {
    if (this.disabled()) {
      return;
    }
    const value = this.board()[row]?.[col];
    if (value !== null && value !== undefined) {
      return;
    }
    this.cellClicked.emit({ row, col });
  }
}
