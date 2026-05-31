import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';
import { getHttpErrorUserMessage } from '../../shared/api-error.util';

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
  loading = signal(false);

  isAdminMode(): boolean {
    return this.data?.mode === 'ADMIN';
  }

  login() {
    const value = this.matricule().trim().toUpperCase();

    if (!value || this.loading()) {
      return;
    }

    if (this.isAdminMode()) {
      this.loginAdmin(value);
      return;
    }

    this.loginMember(value);
  }

  private loginMember(value: string) {
    this.loading.set(true);

    this.padelService.getMemberByMatricule(value).subscribe({
      next: member => {
        this.loading.set(false);
        this.authService.login(member);
        this.dialogRef.close(member);
      },
      error: error => {
        this.loading.set(false);

        this.snackBar.open(this.getLoginErrorMessage(error, 'membre'), 'OK', {
          duration: 5000
        });
      }
    });
  }

  private loginAdmin(value: string) {
    this.loading.set(true);

    this.padelService.getAdministratorByMatricule(value).subscribe({
      next: admin => {
        this.loading.set(false);
        this.authService.loginAdmin(admin);
        this.dialogRef.close(admin);
      },
      error: error => {
        this.loading.set(false);

        this.snackBar.open(this.getLoginErrorMessage(error, 'administrateur'), 'OK', {
          duration: 5000
        });
      }
    });
  }

  private getLoginErrorMessage(error: any, type: 'membre' | 'administrateur'): string {
    if (error?.status === 404) {
      return type === 'membre'
        ? 'Aucun membre trouvé avec ce matricule.'
        : 'Aucun administrateur trouvé avec ce matricule.';
    }

    return getHttpErrorUserMessage(error);
  }
}
