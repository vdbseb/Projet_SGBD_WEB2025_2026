import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login-dialog.html'
})
export class LoginDialogComponent {
  private padelService = inject(PadelService);
  private dialogRef = inject(MatDialogRef<LoginDialogComponent>);
  private snackBar = inject(MatSnackBar);
  private authService = inject(AuthService);
  data = inject(MAT_DIALOG_DATA, { optional: true });

  matricule = signal('');

  isAdminMode(): boolean {
    return this.data?.mode === 'ADMIN';
  }

  login() {
    const value = this.matricule().trim().toUpperCase();

    if (!value) {
      return;
    }

    if (this.isAdminMode()) {
      this.loginAdmin(value);
      return;
    }

    this.loginMember(value);
  }

  private loginMember(value: string) {
    this.padelService.getMemberByMatricule(value).subscribe({
      next: (member) => {
        this.authService.login(member);
        this.dialogRef.close(member);
      },
      error: () => {
        this.snackBar.open('Aucun membre trouvé avec ce matricule.', 'OK', {
          duration: 4000
        });
      }
    });
  }

  private loginAdmin(value: string) {
    this.padelService.getAdministrators().subscribe(admins => {
      const admin = admins.find(admin =>
        admin.matricule?.toUpperCase() === value
      );

      if (!admin) {
        this.snackBar.open('Aucun administrateur trouvé avec ce matricule.', 'OK', {
          duration: 4000
        });
        return;
      }

      this.authService.loginAdmin(admin);
      this.dialogRef.close(admin);
    });
  }
}
