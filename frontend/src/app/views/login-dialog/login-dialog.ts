import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';

import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';
import { AdminAuthApiService } from '../../services/admin-auth-api.service';
import { getHttpErrorUserMessage } from '../../shared/api-error.util';

@Component({
  selector: 'app-login-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './login-dialog.html'
})
export class LoginDialogComponent {
  private padelService = inject(PadelService);
  private adminAuthApiService = inject(AdminAuthApiService);
  private dialogRef = inject(MatDialogRef<LoginDialogComponent>);
  private snackBar = inject(MatSnackBar);
  private authService = inject(AuthService);

  data = inject(MAT_DIALOG_DATA, { optional: true });

  matricule = signal('');
  password = signal('');
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
    const password = this.password();

    if (!password) {
      this.snackBar.open('Le mot de passe administrateur est obligatoire.', 'OK', {
        duration: 5000
      });
      return;
    }

    this.loading.set(true);

    this.adminAuthApiService.loginAdmin(value, password).subscribe({
      next: response => {
        this.loading.set(false);
        this.authService.loginAdmin(response.admin, response.token);
        this.dialogRef.close(response.admin);
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
    if (type === 'administrateur' && error?.status === 401) {
      return 'Matricule ou mot de passe administrateur incorrect.';
    }

    if (error?.status === 404) {
      return type === 'membre'
        ? 'Aucun membre trouvé avec ce matricule.'
        : 'Aucun administrateur trouvé avec ce matricule.';
    }

    return getHttpErrorUserMessage(error);
  }
}
