import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-admin-card',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  templateUrl: './admin-card.html'
})
export class AdminCardComponent {
  title = input.required<string>();
  subtitle = input.required<string>();
  icon = input.required<string>();
  route = input.required<string>();
}
