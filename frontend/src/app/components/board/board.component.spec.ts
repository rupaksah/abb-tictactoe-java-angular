import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BoardComponent } from './board.component';
import { Board } from '../../models/game.models';

describe('BoardComponent', () => {
  let fixture: ComponentFixture<BoardComponent>;
  let component: BoardComponent;

  const emptyBoard: Board = [
    [null, null, null],
    [null, null, null],
    [null, null, null],
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [BoardComponent] });
    fixture = TestBed.createComponent(BoardComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('board', emptyBoard);
    fixture.detectChanges();
  });

  it('renders 9 cells', () => {
    const buttons = fixture.nativeElement.querySelectorAll('button.cell');
    expect(buttons.length).toBe(9);
  });

  it('emits cellClicked with row/col when an empty cell is clicked', () => {
    const emitted: { row: number; col: number }[] = [];
    component.cellClicked.subscribe((cell) => emitted.push(cell));

    const firstCell = fixture.nativeElement.querySelector('button.cell') as HTMLButtonElement;
    firstCell.click();

    expect(emitted).toEqual([{ row: 0, col: 0 }]);
  });

  it('does not emit for an already-occupied cell', () => {
    const board: Board = [
      ['X', null, null],
      [null, null, null],
      [null, null, null],
    ];
    fixture.componentRef.setInput('board', board);
    fixture.detectChanges();

    const emitted: { row: number; col: number }[] = [];
    component.cellClicked.subscribe((cell) => emitted.push(cell));

    const firstCell = fixture.nativeElement.querySelector('button.cell') as HTMLButtonElement;
    // Occupied cells are rendered disabled, so a real click wouldn't fire the
    // handler either, but calling onCellClick directly checks the guard too.
    component.onCellClick(0, 0);

    expect(emitted).toEqual([]);
    expect(firstCell.disabled).toBeTrue();
  });

  it('does not emit when the board is disabled', () => {
    fixture.componentRef.setInput('disabled', true);
    fixture.detectChanges();

    const emitted: { row: number; col: number }[] = [];
    component.cellClicked.subscribe((cell) => emitted.push(cell));

    component.onCellClick(1, 1);

    expect(emitted).toEqual([]);
  });

  it('applies the winning class to winning cells', () => {
    fixture.componentRef.setInput('winningCells', [
      { row: 0, col: 0 },
      { row: 0, col: 1 },
      { row: 0, col: 2 },
    ]);
    fixture.detectChanges();

    const buttons: NodeListOf<HTMLButtonElement> = fixture.nativeElement.querySelectorAll('button.cell');
    expect(buttons[0].classList).toContain('winning');
    expect(buttons[3].classList).not.toContain('winning');
  });
});
