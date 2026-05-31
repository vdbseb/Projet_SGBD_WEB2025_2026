import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, throwError } from 'rxjs';

import { getHttpErrorUserMessage } from '../shared/api-error.util';

export const apiErrorInterceptor: HttpInterceptorFn = (request, next) => {
  const snackBar = inject(MatSnackBar);

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      const message = getHttpErrorUserMessage(error);

      if (shouldDisplayError(request.method, error.status)) {
        snackBar.open(message, 'OK', {
          duration: getDuration(error.status),
          panelClass: ['api-error-snackbar']
        });
      }

      return throwError(() => error);
    })
  );
};

function shouldDisplayError(method: string, status: number): boolean {
  if (status === 0 || status === 401 || status === 403 || status >= 500) {
    return true;
  }

  return method.toUpperCase() !== 'GET';
}

function getDuration(status: number): number {
  if (status === 0 || status >= 500) {
    return 7000;
  }

  return 5000;
}
