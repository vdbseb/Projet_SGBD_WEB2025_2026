import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

import { PadelService } from '../../../services/padel.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-admin-paiements',
  standalone: true,
  imports: [RouterLink, DatePipe, MatIconModule],
  templateUrl: './admin-paiements.html'
})
export class AdminPaiements implements OnInit {
  private padelService = inject(PadelService);
  authService = inject(AuthService);

  reservations = signal<any[]>([]);
  courts = signal<any[]>([]);
  sites = signal<any[]>([]);
  members = signal<any[]>([]);

  search = signal('');
  selectedFilter = signal<'all' | 'paid' | 'partial' | 'organizer'>('all');

  readonly matchPrice = 60;
  readonly playerShare = 15;

  ngOnInit() {
    this.padelService.getCourts().subscribe(courts => {
      this.courts.set(courts);
    });

    this.padelService.getSites().subscribe(sites => {
      this.sites.set(sites);
    });

    this.padelService.getMembers().subscribe(members => {
      this.members.set(members);
    });

    this.padelService.getAllReservations().subscribe(reservations => {
      this.reservations.set(reservations);
    });
  }

  getVisibleReservations() {
    const admin = this.authService.currentAdmin();

    let reservations = this.reservations();

    if (this.authService.isSiteAdmin()) {
      reservations = reservations.filter(reservation => {
        const court = this.courts().find(c => c.id === reservation.courtId);
        return court?.siteId === admin.siteId;
      });
    }

    const query = this.search().toLowerCase().trim();

    if (query) {
      reservations = reservations.filter(reservation => {
        const organizer = this.getOrganizerLabel(reservation).toLowerCase();
        const site = this.getSiteName(reservation).toLowerCase();
        const court = this.getCourtName(reservation).toLowerCase();

        return organizer.includes(query) ||
          site.includes(query) ||
          court.includes(query);
      });
    }

    const filter = this.selectedFilter();

    if (filter === 'paid') {
      return reservations.filter(r => this.getPaymentStatus(r) === 'PAYÉ');
    }

    if (filter === 'partial') {
      return reservations.filter(r => this.getPaymentStatus(r) === 'PARTIEL');
    }

    if (filter === 'organizer') {
      return reservations.filter(r => this.getPaymentStatus(r) === 'SOLDE ORGANISATEUR');
    }

    return reservations;
  }

  getParticipantsCount(reservation: any): number {
    return 1 + (reservation.participantMatricules?.length || 0);
  }

  getPaidAmount(reservation: any): number {
    return this.getParticipantsCount(reservation) * this.playerShare;
  }

  getRemainingAmount(reservation: any): number {
    return Math.max(0, this.matchPrice - this.getPaidAmount(reservation));
  }

  getPaymentStatus(reservation: any): string {
    const count = this.getParticipantsCount(reservation);

    if (count >= 4) {
      return 'PAYÉ';
    }

    if (reservation.matchType === 'PUBLIC') {
      return 'SOLDE ORGANISATEUR';
    }

    return 'PARTIEL';
  }

  getPaymentStatusClass(reservation: any): string {
    const status = this.getPaymentStatus(reservation);

    if (status === 'PAYÉ') {
      return 'bg-emerald-100 text-emerald-700';
    }

    if (status === 'SOLDE ORGANISATEUR') {
      return 'bg-red-100 text-red-700';
    }

    return 'bg-orange-100 text-orange-700';
  }

  getCourtName(reservation: any): string {
    const court = this.courts().find(c => c.id === reservation.courtId);
    return reservation.courtName || court?.name || 'Terrain inconnu';
  }

  getSiteName(reservation: any): string {
    const court = this.courts().find(c => c.id === reservation.courtId);
    const site = this.sites().find(s => s.id === court?.siteId);

    return site?.clubName || site?.name || 'Site inconnu';
  }

  getOrganizerLabel(reservation: any): string {
    const organizer = this.members().find(m => m.id === reservation.memberId);

    if (!organizer) {
      return 'Organisateur inconnu';
    }

    return `${organizer.firstName} ${organizer.lastName} (${organizer.matricule})`;
  }

  getParticipantRows(reservation: any): string[] {
    const rows = [];

    rows.push(this.getOrganizerLabel(reservation));

    if (reservation.participantMatricules?.length) {
      rows.push(...reservation.participantMatricules);
    }

    while (rows.length < 4) {
      rows.push('Place libre');
    }

    return rows;
  }

  getRevenue(): number {
    return this.getVisibleReservations()
      .reduce((sum, r) => sum + this.getPaidAmount(r), 0);
  }

  getRemainingRevenue(): number {
    return this.getVisibleReservations()
      .reduce((sum, r) => sum + this.getRemainingAmount(r), 0);
  }

  getIncompleteMatchesCount(): number {
    return this.getVisibleReservations()
      .filter(r => this.getParticipantsCount(r) < 4)
      .length;
  }
}
