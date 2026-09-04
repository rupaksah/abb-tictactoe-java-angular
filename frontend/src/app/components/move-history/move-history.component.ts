import { Component, input } from '@angular/core';
import { MoveHistoryItem } from '../../models/game.models';

@Component({
  selector: 'app-move-history',
  standalone: true,
  templateUrl: './move-history.component.html',
  styleUrl: './move-history.component.css',
})
export class MoveHistoryComponent {
  readonly moves = input.required<MoveHistoryItem[]>();

  positionLabel(move: MoveHistoryItem): string {
    return `Row ${move.row + 1}, Column ${move.col + 1}`;
  }
}
