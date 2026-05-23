import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-my-reservation',
  standalone: true,
  imports: [DatePipe, RouterLink, MatIconModule],
  templateUrl: './my-reservation.html'
})
export class MyReservations implements OnInit {
  private padelService = inject(PadelService);
  authService = inject(AuthService);

  reservations = signal<any[]>([]);
  sites = signal<any[]>([]);
  selectedFilter = signal<'all' | 'upcoming' | 'past'>('all');

  ngOnInit() {
    const member = this.authService.currentMember();

    if (!member) {
      this.reservations.set([]);
      return;
    }

    this.padelService.getSites().subscribe(sites => {
      this.sites.set(sites);
    });

    this.padelService.getAllReservations().subscribe(reservations => {
      const filteredReservations = reservations
        .filter(reservation => reservation.memberId === member.id)
        .sort((a, b) => {
          const dateA = new Date(`${a.date}T${a.startTime}`).getTime();
          const dateB = new Date(`${b.date}T${b.startTime}`).getTime();

          return dateA - dateB;
        });

      this.reservations.set(filteredReservations);
    });
  }
  getSiteName(reservation: any): string {
    const site = this.sites().find(site =>
      site.courts?.some((court: any) => court.id === reservation.courtId)
    );

    return site?.clubName || 'Club inconnu';
  }
  getReservationStatus(reservation: any): 'today' | 'upcoming' | 'past' {
    const reservationDate = new Date(`${reservation.date}T${reservation.startTime}`);
    const now = new Date();

    const sameDay =
      reservationDate.getFullYear() === now.getFullYear() &&
      reservationDate.getMonth() === now.getMonth() &&
      reservationDate.getDate() === now.getDate();

    if (sameDay) {
      return 'today';
    }

    return reservationDate > now ? 'upcoming' : 'past';
  }

  getStatusLabel(reservation: any): string {
    const status = this.getReservationStatus(reservation);

    switch (status) {
      case 'today':
        return 'Aujourd’hui';
      case 'upcoming':
        return 'À venir';
      case 'past':
        return 'Passée';
    }
  }

  getStatusClass(reservation: any): string {
    const status = this.getReservationStatus(reservation);

    switch (status) {
      case 'today':
        return 'bg-orange-100 text-orange-700';
      case 'upcoming':
        return 'bg-blue-100 text-blue-700';
      case 'past':
        return 'bg-slate-100 text-slate-500';
    }
  }
  getFilteredReservations() {
    const filter = this.selectedFilter();

    if (filter === 'all') {
      return this.reservations();
    }

    if (filter === 'upcoming') {
      return this.reservations().filter(reservation => {
        const status = this.getReservationStatus(reservation);
        return status === 'today' || status === 'upcoming';
      });
    }

    return this.reservations().filter(reservation =>
      this.getReservationStatus(reservation) === 'past'
    );

  }

  countAllReservations(): number {
    return this.reservations().length;
  }

  countUpcomingReservations(): number {
    return this.reservations().filter(reservation => {
      const status = this.getReservationStatus(reservation);
      return status === 'today' || status === 'upcoming';
    }).length;
  }

  countPastReservations(): number {
    return this.reservations().filter(reservation =>
      this.getReservationStatus(reservation) === 'past'
    ).length;
  }
}
