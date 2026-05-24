import { Component } from '@angular/core';
import { AdminCardComponent } from '../admin-card/admin-card';
import { MatIconModule } from '@angular/material/icon';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [AdminCardComponent, RouterLink, MatIconModule],
  templateUrl: './admin-dashboard.html'
})
export class AdminDashboard {}
