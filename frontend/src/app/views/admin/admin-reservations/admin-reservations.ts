import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { PadelService } from '../../../services/padel.service';

@Component({
  selector: 'app-admin-reservations',
  standalone: true,
  imports: [RouterLink, DatePipe, MatIconModule],
  templateUrl: './admin-reservations.html'
})
export class AdminReservations implements OnInit {
  private padelService = inject(PadelService);

  reservations = signal<any[]>([]);
  search = signal('');
  selectedFilter = signal<'all' | 'today' | 'upcoming' | 'past'>('all');

  ngOnInit() {
    this.padelService.getAllReservations().subscribe(reservations => {
      const sorted = reservations.sort((a, b) => {
        const dateA = new Date(`${a.date}T${a.startTime}`).getTime();
        const dateB = new Date(`${b.date}T${b.startTime}`).getTime();
        return dateA - dateB;
      });

      this.reservations.set(sorted);
    });
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

  filteredReservations() {
    let data = this.reservations();

    const query = this.search().toLowerCase().trim();

    if (query) {
      data = data.filter(reservation =>
        reservation.courtName?.toLowerCase().includes(query) ||
        reservation.playerMatricule?.toLowerCase().includes(query)
      );
    }

    const filter = this.selectedFilter();

    if (filter === 'all') {
      return data;
    }

    return data.filter(reservation =>
      this.getReservationStatus(reservation) === filter
    );
  }
}
