import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { PlayerWalletCard } from './player-wallet-card/player-wallet-card';

@Component({
  selector: 'app-my-reservation',
  standalone: true,
  imports: [DatePipe, RouterLink, MatIconModule, PlayerWalletCard],
  templateUrl: './my-reservation.html'
})
export class MyReservations implements OnInit {
  private padelService = inject(PadelService);
  authService = inject(AuthService);

  reservations = signal<any[]>([]);
  sites = signal<any[]>([]);
  selectedFilter = signal<'all' | 'upcoming' | 'past' | 'cancelled'>('all');
  courts = signal<any[]>([]);
  wallet = signal<any | null>(null);

  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  ngOnInit() {
    const member = this.authService.currentMember();

    if (!member) {
      this.reservations.set([]);
      this.wallet.set(null);
      return;
    }

    this.loadWallet(member.id);
    this.loadReservations(member);
    this.loadSites();
    this.loadCourts();
  }

  private loadWallet(memberId: number) {
    this.padelService.getMemberWallet(memberId).subscribe(wallet => {
      this.wallet.set(wallet);
    });
  }

  private loadSites() {
    this.padelService.getSites().subscribe(sites => {
      this.sites.set(sites);
    });
  }

  private loadCourts() {
    this.padelService.getCourts().subscribe(courts => {
      this.courts.set(courts);
    });
  }

  private loadReservations(member: any) {
    this.padelService.getAllReservations().subscribe(reservations => {
      const filteredReservations = reservations
        .filter(reservation =>
          reservation.memberId === member.id ||
          reservation.participantMatricules?.includes(member.matricule)
        )
        .sort((a, b) => {
          const dateA = new Date(`${a.date}T${a.startTime}`).getTime();
          const dateB = new Date(`${b.date}T${b.startTime}`).getTime();

          return dateA - dateB;
        });

      this.reservations.set(filteredReservations);
    });
  }

  getSiteName(reservation: any): string {
    if (reservation.siteName) {
      return reservation.siteName;
    }

    const court = this.courts().find(court => court.id === reservation.courtId);

    return court?.siteName || court?.site?.name || court?.site?.clubName || 'Club inconnu';
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
      return this.reservations().filter(reservation =>
          reservation.reservationStatus !== 'ANNULEE'
          && reservation.matchStatus !== 'ANNULE'
          && (
            this.getReservationStatus(reservation) === 'today'
            || this.getReservationStatus(reservation) === 'upcoming'
          )
      );
    }

    if (filter === 'cancelled') {
      return this.reservations().filter(reservation =>
        reservation.reservationStatus === 'ANNULEE'
        || reservation.matchStatus === 'ANNULE'
      );
    }

    return this.reservations().filter(reservation =>
      reservation.reservationStatus !== 'ANNULEE'
      && reservation.matchStatus !== 'ANNULE'
      && this.getReservationStatus(reservation) === 'past'
    );
  }

  countAllReservations(): number {
    return this.reservations().length;
  }

  countUpcomingReservations(): number {
    return this.reservations().filter(reservation =>
        reservation.reservationStatus !== 'ANNULEE'
        && reservation.matchStatus !== 'ANNULE'
        && (
          this.getReservationStatus(reservation) === 'today'
          || this.getReservationStatus(reservation) === 'upcoming'
        )
    ).length;
  }

  countPastReservations(): number {
    return this.reservations().filter(reservation =>
      reservation.reservationStatus !== 'ANNULEE'
      && reservation.matchStatus !== 'ANNULE'
      && this.getReservationStatus(reservation) === 'past'
    ).length;
  }

  countCancelledReservations(): number {
    return this.reservations().filter(reservation =>
      reservation.reservationStatus === 'ANNULEE'
      || reservation.matchStatus === 'ANNULE'
    ).length;
  }

  getParticipantsLabel(reservation: any): string {
    if (reservation.members?.length > 0) {
      return reservation.members
        .map((member: any) =>
          `${member.firstName} ${member.lastName} (${member.matricule})`
        )
        .join(', ');
    }

    if (reservation.participantMatricules?.length > 0) {
      return reservation.participantMatricules
        .filter(Boolean)
        .join(', ');
    }

    return reservation.playerMatricule || 'Participants non disponibles';
  }

  cancelReservation(reservationId: number) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Annuler la réservation',
        message: 'Voulez-vous vraiment annuler cette réservation ?',
        confirmLabel: 'Oui, annuler',
        cancelLabel: 'Retour'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      const member = this.authService.currentMember();

      this.padelService.deleteReservation(reservationId).subscribe({
        next: () => {
          this.reservations.update(reservations =>
            reservations.map(reservation =>
              reservation.id === reservationId
                ? {
                  ...reservation,
                  reservationStatus: 'ANNULEE',
                  matchStatus: 'ANNULE'
                }
                : reservation
            )
          );

          if (member) {
            this.loadWallet(member.id);
          }
        },
        error: () => {
          this.snackBar.open('Impossible d’annuler la réservation.', 'OK', {
            duration: 4000
          });
        }
      });
    });
  }

  isPaid(reservation: any): boolean {
    return reservation.paiementStatut === 'VALIDE';
  }

  payReservation(reservation: any) {
    const member = this.authService.currentMember();

    if (!member) {
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Paiement',
        message: 'Simuler le paiement de votre part de 15€ ?',
        confirmLabel: 'Payer 15€',
        cancelLabel: 'Retour'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.snackBar.open(`Paiement confirmé pour la réservation ${reservation.id} !`, 'OK', {
        duration: 3000
      });

      this.loadWallet(member.id);
    });
  }



  getPlayerBalance(): number {
    return (this.wallet()?.balanceCentimes ?? 0) / 100;
  }

  getPlayerCredit(): number {
    return (this.wallet()?.creditCentimes ?? 0) / 100;
  }

  getAmountDue(): number {
    return (this.wallet()?.amountDueCentimes ?? 0) / 100;
  }

  getAmountRefunded(): number {
    return (this.wallet()?.amountRefundedCentimes ?? 0) / 100;
  }
}
