import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { MatDialog } from '@angular/material/dialog';

import { AuthService } from '../services/auth.service';
import { LoginDialogComponent } from '../views/login-dialog/login-dialog';

export const adminGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const dialog = inject(MatDialog);

  if (authService.isAdminLoggedIn()) {
    return true;
  }

  dialog.open(LoginDialogComponent, {
    data: { mode: 'ADMIN' }
  });

  router.navigate(['/']);
  return false;
};
