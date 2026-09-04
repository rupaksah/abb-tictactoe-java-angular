import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ScoreboardComponent } from './scoreboard.component';

describe('ScoreboardComponent', () => {
  let fixture: ComponentFixture<ScoreboardComponent>;
  let component: ScoreboardComponent;

  beforeEach(() => {
    TestBed.configureTestingModule({ imports: [ScoreboardComponent] });
    fixture = TestBed.createComponent(ScoreboardComponent);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('scoreboard', { xWins: 3, oWins: 1, draws: 2 });
    fixture.detectChanges();
  });

  it('renders the win/draw counts', () => {
    const text = (fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text).toContain('3');
    expect(text).toContain('1');
    expect(text).toContain('2');
  });

  it('emits resetScoreboard when the reset button is clicked', () => {
    let emitted = false;
    component.resetScoreboard.subscribe(() => (emitted = true));

    const button = fixture.nativeElement.querySelector('.reset-btn') as HTMLButtonElement;
    button.click();

    expect(emitted).toBeTrue();
  });
});
