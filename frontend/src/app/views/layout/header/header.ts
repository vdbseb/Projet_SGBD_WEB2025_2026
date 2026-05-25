import { Component, inject } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LoginDialogComponent } from '../../login-dialog/login-dialog';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-header',
  imports: [
    MatIcon,
    RouterLink,
    RouterLinkActive,
    MatDialogModule
  ],
  templateUrl: './header.html',
  styleUrl: './header.css',
})
export class Header {
  private dialog = inject(MatDialog);
  authService = inject(AuthService);

  openLogin() {
    if (this.authService.isLoggedIn()) {
      this.logout()
      return;
    }

    this.dialog.open(LoginDialogComponent);
  }

  logout() {
    this.authService.logout();
  }
  logoutAdmin(){
    this.authService.logoutAdmin ();
  }
}
