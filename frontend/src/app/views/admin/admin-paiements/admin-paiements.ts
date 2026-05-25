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

  matchPrice = 60;
  playerShare = 15;

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
    return this.getRemainingAmount(reservation) === 0 ? 'Payé' : 'En attente';
  }

  getPaymentStatusClass(reservation: any): string {
    return this.getRemainingAmount(reservation) === 0
      ? 'bg-emerald-100 text-emerald-700'
      : 'bg-orange-100 text-orange-700';
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
      return reservation.playerMatricule || 'Organisateur inconnu';
    }

    return `${organizer.firstName} ${organizer.lastName} (${organizer.matricule})`;
  }

  getVisibleReservations() {
    const admin = this.authService.currentAdmin();

    if (admin?.typeAdmin !== 'SITE') {
      return this.reservations();
    }

    return this.reservations().filter(reservation => {
      const court = this.courts().find(c => c.id === reservation.courtId);
      return court?.siteId === admin.siteId;
    });
  }
}
