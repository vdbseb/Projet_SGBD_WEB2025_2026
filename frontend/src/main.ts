import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { App } from './app/app';
import { registerLocaleData } from '@angular/common';
import localeFrBe from '@angular/common/locales/fr-BE';

registerLocaleData(localeFrBe);

bootstrapApplication(App, appConfig)
  .catch((err) => console.error(err));
