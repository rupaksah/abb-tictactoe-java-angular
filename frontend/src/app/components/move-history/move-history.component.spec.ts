import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MoveHistoryComponent } from './move-history.component';

describe('MoveHistoryComponent', () => {
  let fixture: ComponentFixture<MoveHistoryComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [MoveHistoryComponent] });
    fixture = TestBed.createComponent(MoveHistoryComponent);
  });

  it('shows an empty message when there are no moves', () => {
    fixture.componentRef.setInput('moves', []);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('No moves yet');
  });

  it('lists each move with number, player and position', () => {
    fixture.componentRef.setInput('moves', [
      { moveNumber: 1, player: 'X', row: 0, col: 0 },
      { moveNumber: 2, player: 'O', row: 1, col: 1 },
    ]);
    fixture.detectChanges();

    const items = fixture.nativeElement.querySelectorAll('li');
    expect(items.length).toBe(2);
    expect(items[0].textContent).toContain('Row 1, Column 1');
    expect(items[1].textContent).toContain('Row 2, Column 2');
  });
});
