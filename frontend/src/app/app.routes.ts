import { Routes } from '@angular/router';
import {WelcomePage} from './views/welcome-page/welcome-page';
import {ReservationPage} from './views/reservation-page/reservation-page';


export const routes: Routes = [
  { path: '', component: WelcomePage },
  { path: 'reserver/:id', component: ReservationPage },
  {
    path: 'mes-reservations',
    loadComponent: () =>
      import('./views/my-reservation/my-reservation')
        .then(m => m.MyReservations)
  },
  {
    path: 'admin',
    loadComponent: () =>
      import('./views/admin/admin-dashboard/admin-dashboard')
        .then(m => m.AdminDashboard)
  },
  {
    path: 'admin/members',
    loadComponent: () =>
      import('./views/admin/admin-members/admin-members')
        .then(m => m.AdminMembers)
  },
  {
    path: 'admin/reservations',
    loadComponent: () =>
      import('./views/admin/admin-reservations/admin-reservations')
        .then(m => m.AdminReservations)
  },
  {
    path: 'admin/sites',
    loadComponent: () =>
      import('./views/admin/admin-sites/admin-sites')
        .then(m => m.AdminSites)
  },
  {
    path: 'admin/paiements',
    loadComponent: () =>
      import('./views/admin/admin-paiements/admin-paiements')
        .then(m => m.AdminPaiements)
  },
  {
    path: 'matches-publics',
    loadComponent: () =>
      import('./views/public-matches/public-matches')
        .then(m => m.PublicMatches)
  },
  {
    path: 'admin/courts',
    loadComponent: () =>
      import('./views/admin/admin-courts/admin-courts')
        .then(m => m.AdminCourts)
  }
];
