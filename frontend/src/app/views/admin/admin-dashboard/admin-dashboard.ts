import { Component, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AdminCardComponent } from '../admin-card/admin-card';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [AdminCardComponent, RouterLink, MatIconModule],
  templateUrl: './admin-dashboard.html'
})
export class AdminDashboard {
  adminType = signal<'GLOBAL' | 'SITE'>('GLOBAL');

  isGlobalAdmin(): boolean {
    return this.adminType() === 'GLOBAL';
  }
}
