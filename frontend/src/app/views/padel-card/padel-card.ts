import { Component, input, output } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-padel-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule],
  templateUrl: './padel-card.html'
})
export class PadelCardComponent {

  title = input.required<string>();
  subtitle = input<string>('');
  initial = input<string>('');
  image = input<string>('');
  buttonLabel = input<string>('Réserver');
  isSelected = input<boolean>(false);

  cardClick = output<void>();

  onButtonClick() {
    this.cardClick.emit();
  }
}
