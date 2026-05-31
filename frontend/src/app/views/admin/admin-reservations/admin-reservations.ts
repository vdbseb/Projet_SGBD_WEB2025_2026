import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';

import { PadelService } from '../../../services/padel.service';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import { AuthService } from '../../../services/auth.service';
import { getHttpErrorUserMessage } from '../../../shared/api-error.util';

@Component({
  selector: 'app-admin-reservations',
  standalone: true,
  imports: [RouterLink, DatePipe, MatIconModule],
  templateUrl: './admin-reservations.html'
})
export class AdminReservations implements OnInit {
  private padelService = inject(PadelService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  authService = inject(AuthService);

  reservations = signal<any[]>([]);
  courts = signal<any[]>([]);
  sites = signal<any[]>([]);
  members = signal<any[]>([]);

  search = signal('');
  selectedFilter = signal<'all' | 'today' | 'upcoming' | 'past'>('all');
  viewMode = signal<'cards' | 'table'>('cards');

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    forkJoin({
      reservations: this.padelService.getAllReservations(),
      courts: this.padelService.getCourts(),
      sites: this.padelService.getSites(),
      members: this.padelService.getMembers()
    }).subscribe({
      next: ({ reservations, courts, sites, members }) => {
        this.courts.set(courts);
        this.sites.set(sites);
        this.members.set(members);

        const admin = this.authService.currentAdmin();

        let visibleReservations = reservations;

        if (this.authService.isSiteAdmin()) {
          visibleReservations = reservations.filter(reservation => {
            const court = courts.find(c => c.id === reservation.courtId);
            return court?.siteId === admin.siteId;
          });
        }

        const sorted = visibleReservations.sort((a, b) => {
          const dateA = new Date(`${a.date}T${a.startTime}`).getTime();
          const dateB = new Date(`${b.date}T${b.startTime}`).getTime();

          return dateA - dateB;
        });

        this.reservations.set(sorted);
      },
      error: error => {
        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  showCardsView() {
    this.viewMode.set('cards');
  }

  showTableView() {
    this.viewMode.set('table');
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
        reservation.playerMatricule?.toLowerCase().includes(query) ||
        this.getCourtName(reservation).toLowerCase().includes(query) ||
        this.getSiteName(reservation).toLowerCase().includes(query) ||
        this.getOrganizerLabel(reservation).toLowerCase().includes(query)
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

    if (reservation.matchType === 'PRIVATE' || reservation.matchType === 'PRIVE') {
      return 'Match privé';
    }

    return 'Type non défini';
  }

  getMatchTypeClass(reservation: any): string {
    return reservation.matchType === 'PRIVATE' || reservation.matchType === 'PRIVE'
      ? 'bg-violet-100 text-violet-700'
      : 'bg-blue-100 text-blue-700';
  }

  getParticipantsCount(reservation: any): number {
    const organizerCount = reservation.memberId ? 1 : 0;
    const extraCount = reservation.participantMatricules?.length || 0;

    return organizerCount + extraCount;
  }

  getMatchCapacityLabel(reservation: any): string {
    return `${this.getParticipantsCount(reservation)}/4 joueurs`;
  }

  getMatchStatusLabel(reservation: any): string {
    if (reservation.matchStatus === 'ANNULE') {
      return 'Annulé';
    }

    if (reservation.matchStatus === 'COMPLET') {
      return 'Complet';
    }

    if (reservation.matchStatus === 'OUVERT') {
      return 'Ouvert';
    }

    if (reservation.matchStatus === 'TERMINE') {
      return 'Terminé';
    }

    return reservation.matchStatus || 'Inconnu';
  }

  getMatchStatusClass(reservation: any): string {
    switch (reservation.matchStatus) {
      case 'COMPLET':
        return 'bg-emerald-100 text-emerald-700';

      case 'OUVERT':
        return 'bg-orange-100 text-orange-700';

      case 'ANNULE':
        return 'bg-red-100 text-red-700';

      case 'TERMINE':
        return 'bg-slate-100 text-slate-600';

      default:
        return 'bg-slate-100 text-slate-600';
    }
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
            reservations.filter(reservation => reservation.id !== reservationId)
          );

          this.snackBar.open('Réservation supprimée.', 'OK', {
            duration: 3000
          });
        },
        error: error => {
          this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
            duration: 5000
          });
        }
      });
    });
  }
}
