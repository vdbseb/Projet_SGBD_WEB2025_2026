import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { PadelService } from '../../../services/padel.service';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import {AuthService} from '../../../services/auth.service';

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
  courts = signal<any[]>([]);
  sites = signal<any[]>([]);
  members = signal<any[]>([]);
  private dialog = inject(MatDialog);
  authService = inject(AuthService);

  ngOnInit() {
    this.padelService.getAllReservations().subscribe(reservations => {
      const admin = this.authService.currentAdmin();

      let filteredReservations = reservations;

      if (admin?.typeAdmin === 'SITE') {
        filteredReservations = reservations.filter(reservation => {
          const court = this.courts().find(c => c.id === reservation.courtId);
          return court?.siteId === admin.siteId;
        });
      }

      const sorted = filteredReservations.sort((a, b) => {
        const dateA = new Date(`${a.date}T${a.startTime}`).getTime();
        const dateB = new Date(`${b.date}T${b.startTime}`).getTime();
        return dateA - dateB;
      });

      this.reservations.set(sorted);
    });
    this.padelService.getCourts().subscribe(courts => {
      this.courts.set(courts);
    });

    this.padelService.getSites().subscribe(sites => {
      this.sites.set(sites);
    });

    this.padelService.getMembers().subscribe(members => {
      this.members.set(members);
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
  getCourtName(reservation: any): string {
    const court = this.courts().find(court => court.id === reservation.courtId);
    return reservation.courtName || court?.name || 'Terrain inconnu';
  }

  getSiteName(reservation: any): string {
    if (reservation.siteName) {
      return reservation.siteName;
    }

    const court = this.courts().find(court => court.id === reservation.courtId);
    const site = this.sites().find(site => site.id === court?.siteId);

    return site?.clubName || site?.name || 'Site inconnu';
  }

  getOrganizer(reservation: any): any | null {
    return this.members().find(member => member.id === reservation.memberId) || null;
  }

  getOrganizerLabel(reservation: any): string {
    const organizer = this.getOrganizer(reservation);

    if (!organizer) {
      return reservation.playerMatricule || 'Organisateur inconnu';
    }

    return `${organizer.firstName} ${organizer.lastName} (${organizer.matricule})`;
  }

  getParticipantsLabel(reservation: any): string {
    if (reservation.members?.length > 0) {
      return reservation.members
        .map((member: any) => `${member.firstName} ${member.lastName} (${member.matricule})`)
        .join(', ');
    }

    if (reservation.participantMatricules?.length > 0) {
      return reservation.participantMatricules.join(', ');
    }

    return 'Participants non disponibles';
  }

  getMatchTypeLabel(reservation: any): string {
    if (reservation.matchType === 'PUBLIC') {
      return 'Match public';
    }

    if (reservation.matchType === 'PRIVATE') {
      return 'Match privé';
    }

    return 'Type non défini';
  }
  deleteReservation(reservationId: number) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Supprimer la réservation',
        message: 'Voulez-vous vraiment supprimer cette réservation ?',
        confirmLabel: 'Oui, supprimer',
        cancelLabel: 'Retour'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.padelService.deleteReservation(reservationId).subscribe({
        next: () => {
          this.reservations.update(reservations =>
            reservations.filter(r => r.id !== reservationId)
          );
        },
        error: () => {
          alert('Impossible de supprimer la réservation.');
        }
      });
    });
  }
}
