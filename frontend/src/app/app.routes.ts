import { Routes } from '@angular/router';
import {WelcomePage} from './views/welcome-page/welcome-page';
import {ReservationPage} from './views/reservation-page/reservation-page';


export const routes: Routes = [
  { path: '', component: WelcomePage },
  { path: 'reserver/:id', component: ReservationPage },
];
