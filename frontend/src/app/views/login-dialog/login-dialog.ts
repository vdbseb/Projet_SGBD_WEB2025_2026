import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PadelService } from '../../services/padel.service';
import {AuthService} from '../../services/auth.service';

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

  matricule = signal('');

  login() {
    const value = this.matricule().trim();

    if (!value) {
      return;
    }

    this.padelService.getMemberByMatricule(value).subscribe({
      next: (member) => {
        this.authService.login(member);
        this.dialogRef.close(member);
      },
      error: () => {
        this.snackBar.open(
          'Aucun membre trouvé avec ce matricule.',
          'OK',
          { duration: 4000 }
        );
      }
    });
  }
}
