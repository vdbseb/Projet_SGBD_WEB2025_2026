import { ApplicationConfig, provideBrowserGlobalErrorListeners, LOCALE_ID } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { registerLocaleData } from '@angular/common';
import localeFrBe from '@angular/common/locales/fr-BE';

import { routes } from './app.routes';
import { apiErrorInterceptor } from './interceptors/api-error.interceptor';
import { authTokenInterceptor } from './interceptors/auth-token.interceptor';

registerLocaleData(localeFrBe);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    { provide: LOCALE_ID, useValue: 'fr-BE' },
    provideHttpClient(withInterceptors([
      authTokenInterceptor,
      apiErrorInterceptor
    ]))
  ]
};
