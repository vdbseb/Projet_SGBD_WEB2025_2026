import { ApplicationConfig, provideBrowserGlobalErrorListeners, LOCALE_ID } from '@angular/core'; // Ajoute LOCALE_ID ici
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import {provideHttpClient} from '@angular/common/http';
import { registerLocaleData } from '@angular/common';
import localeFrBe from '@angular/common/locales/fr-BE'
registerLocaleData(localeFrBe);

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    { provide: LOCALE_ID, useValue: 'fr-BE' },
    provideHttpClient()
  ]
};
