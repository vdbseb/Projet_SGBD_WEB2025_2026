import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';

import { AdminCardComponent } from '../admin-card/admin-card';
import { LoginDialogComponent } from '../../login-dialog/login-dialog';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [AdminCardComponent, MatIconModule, MatDialogModule],
  templateUrl: './admin-dashboard.html'
})
export class AdminDashboard implements OnInit {
  authService = inject(AuthService);

  private dialog = inject(MatDialog);
  private router = inject(Router);

  ngOnInit() {
    if (this.authService.isAdminLoggedIn()) {
      return;
    }

    const dialogRef = this.dialog.open(LoginDialogComponent, {
      disableClose: true,
      data: {
        mode: 'ADMIN'
      }
    });

    dialogRef.afterClosed().subscribe(admin => {
      if (!admin) {
        this.router.navigate(['/']);
      }
    });
  }

  isGlobalAdmin(): boolean {
    return this.authService.isGlobalAdmin();
  }
}
