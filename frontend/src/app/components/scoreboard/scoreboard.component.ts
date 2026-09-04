import { Component, input, output } from '@angular/core';
import { ScoreboardState } from '../../models/game.models';

@Component({
  selector: 'app-scoreboard',
  standalone: true,
  templateUrl: './scoreboard.component.html',
  styleUrl: './scoreboard.component.css',
})
export class ScoreboardComponent {
  readonly scoreboard = input.required<ScoreboardState>();
  readonly resetScoreboard = output<void>();
}
