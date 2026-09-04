/**
 * TypeScript mirrors of the backend's DTOs (see
 * backend/src/main/java/com/tictactoe/backend/dto). Field names match the
 * JSON produced by Jackson's default bean serialization, so no mapping
 * layer is needed between the HTTP response and these types.
 */

export type Player = 'X' | 'O';

export type GameMode = 'TWO_PLAYER' | 'VS_COMPUTER';

export type GameStatus = 'InProgress' | 'Won' | 'Draw';

/** 3x3 grid; each cell is 'X', 'O', or null when empty. */
export type Board = (Player | null)[][];

export interface CellPosition {
  row: number;
  col: number;
}

export interface MoveHistoryItem {
  moveNumber: number;
  player: Player;
  row: number;
  col: number;
}

export interface ScoreboardState {
  xWins: number;
  oWins: number;
  draws: number;
}

export interface GameState {
  gameId: string;
  board: Board;
  currentPlayer: Player;
  gameMode: GameMode;
  status: GameStatus;
  winner: Player | null;
  winningCells: CellPosition[];
  moveHistory: MoveHistoryItem[];
  canUndo: boolean;
  scoreboard: ScoreboardState;
}

export interface ApiErrorBody {
  error: string;
  message: string;
}
