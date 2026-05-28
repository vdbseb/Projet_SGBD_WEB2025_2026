import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-public-matches',
  standalone: true,
  imports: [
    DatePipe,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './public-matchs.html'
})
export class PublicMatchs implements OnInit {
  private padelService = inject(PadelService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  authService = inject(AuthService);

  reservations = signal<any[]>([]);
  courts = signal<any[]>([]);
  sites = signal<any[]>([]);
  members = signal<any[]>([]);

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

    this.loadReservations();
  }

  loadReservations() {
    this.padelService.getAllReservations().subscribe(reservations => {
      this.reservations.set(reservations);
    });
  }

  getPublicMatches() {
    const now = new Date();

    return this.reservations().filter(reservation => {
      const reservationDate = new Date(`${reservation.date}T${reservation.startTime}`);
      const participants = 1 + (reservation.participantMatricules?.length || 0);

      return reservation.matchType === 'PUBLIC'
        && reservation.reservationStatus !== 'ANNULEE'
        && reservation.matchStatus !== 'ANNULE'
        && reservationDate > now;

    });
  }

  getParticipantsCount(reservation: any): number {
    return 1 + (reservation.participantMatricules?.length || 0);
  }

  getRemainingSpots(reservation: any): number {
    return 4 - this.getParticipantsCount(reservation);
  }

  getCourtName(reservation: any): string {
    const court = this.courts().find(c => c.id === reservation.courtId);
    return reservation.courtName || court?.name || 'Terrain inconnu';
  }

  getSiteName(reservation: any): string {
    const court = this.courts().find(c => c.id === reservation.courtId);
    const site = this.sites().find(s => s.id === court?.siteId);

    return reservation.siteName || site?.clubName || 'Site inconnu';
  }

  joinMatch(reservation: any) {
    const member = this.authService.currentMember();
    const matchId = reservation.matchId;

    if (!matchId) {
      this.snackBar.open('Match introuvable pour cette réservation.', 'OK', {
        duration: 4000
      });
      return;
    }
    if (!member) {
      return;
    }
    if (reservation.memberId === member.id) {
      this.snackBar.open('Vous êtes déjà organisateur de ce match.', 'OK', {
        duration: 3000
      });
      return;
    }

    if (this.getParticipantsCount(reservation) >= 4) {
      this.snackBar.open('Ce match est déjà complet.', 'OK', {
        duration: 3000
      });
      return;
    }

    if (reservation.participantMatricules?.includes(member.matricule)) {
      this.snackBar.open('Vous participez déjà à ce match.', 'OK', {
        duration: 3000
      });
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Rejoindre le match',
        message: 'Confirmer le paiement de 15€ pour rejoindre ce match ?',
        confirmLabel: 'Payer 15€',
        cancelLabel: 'Retour'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.padelService.joinPublicMatch(matchId, member.id).subscribe({
        next: () => {
          this.snackBar.open('Vous avez rejoint le match !', 'OK', {
            duration: 3000
          });

          this.loadReservations();
        },
        error: () => {
          this.snackBar.open('Impossible de rejoindre ce match.', 'OK', {
            duration: 4000
          });
        }
      });
    });
  }
  isParticipant(reservation: any): boolean {
    const member = this.authService.currentMember();

    if (!member) {
      return false;
    }

    return reservation.participantMatricules?.includes(member.matricule);
  }
  leaveMatch(reservation: any) {
    const member = this.authService.currentMember();

    if (!member) {
      return;
    }

    const matchId = reservation.matchId;

    if (!matchId) {
      this.snackBar.open('Match introuvable.', 'OK', {
        duration: 4000
      });
      return;
    }

    this.padelService.leavePublicMatch(matchId, member.id).subscribe({
      next: () => {
        this.snackBar.open('Vous avez quitté le match.', 'OK', {
          duration: 3000
        });

        this.loadReservations();
      },
      error: () => {
        this.snackBar.open('Impossible de quitter le match.', 'OK', {
          duration: 4000
        });
      }
    });
  }
}
