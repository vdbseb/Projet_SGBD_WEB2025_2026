import { Routes } from '@angular/router';

import { WelcomePage } from './views/welcome-page/welcome-page';
import { ReservationPage } from './views/reservation-page/reservation-page';

import { adminGuard } from './guards/admin-guard';

export const routes: Routes = [

  {
    path: '',
    component: WelcomePage
  },

  {
    path: 'reserver/:id',
    component: ReservationPage
  },

  {
    path: 'mes-reservations',
    loadComponent: () =>
      import('./views/my-reservation/my-reservation')
        .then(m => m.MyReservations)
  },


  {
    path: 'mon-espace',
    loadComponent: () =>
      import('./views/member/member-dashboard/member-dashboard')
        .then(m => m.MemberDashboard)
  },

  {
    path: 'member/dashboard',
    redirectTo: 'mon-espace',
    pathMatch: 'full'
  },

  {
    path: 'matches-publics',
    loadComponent: () =>
      import('./views/public-matchs/public-matchs')
        .then(m => m.PublicMatchs)
  },

  // ADMIN

  {
    path: 'admin',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./views/admin/admin-dashboard/admin-dashboard')
        .then(m => m.AdminDashboard)
  },

  {
    path: 'admin/members',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./views/admin/admin-members/admin-members')
        .then(m => m.AdminMembers)
  },

  {
    path: 'admin/reservations',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./views/admin/admin-reservations/admin-reservations')
        .then(m => m.AdminReservations)
  },

  {
    path: 'admin/sites',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./views/admin/admin-sites/admin-sites')
        .then(m => m.AdminSites)
  },

  {
    path: 'admin/paiements',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./views/admin/admin-paiements/admin-paiements')
        .then(m => m.AdminPaiements)
  },

  {
    path: 'admin/courts',
    canActivate: [adminGuard],
    loadComponent: () =>
      import('./views/admin/admin-courts/admin-courts')
        .then(m => m.AdminCourts)
  },

  {
    path: 'admin/statistiques',
    canActivate: [adminGuard],
    loadComponent: () =>
     import('./views/admin/admin-statistiques/admin-statistiques')
       .then(m => m.AdminStatistiques)
  }

];
