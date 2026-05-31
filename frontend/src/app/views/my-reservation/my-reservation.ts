import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';

import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';
import { PlayerWalletCard } from '../player-wallet-card/player-wallet-card';
import { getHttpErrorUserMessage } from '../../shared/api-error.util';

@Component({
  selector: 'app-my-reservation',
  standalone: true,
  imports: [
    DatePipe,
    RouterLink,
    MatIconModule,
    PlayerWalletCard
  ],
  templateUrl: './my-reservation.html'
})
export class MyReservations implements OnInit {
  private readonly padelService = inject(PadelService);
  readonly authService = inject(AuthService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  reservations = signal<any[]>([]);
  sites = signal<any[]>([]);
  selectedFilter = signal<'all' | 'upcoming' | 'past' | 'cancelled'>('all');
  courts = signal<any[]>([]);
  wallet = signal<any | null>(null);
  payingReservationId = signal<number | null>(null);
  payingAllReservations = signal(false);

  ngOnInit() {
    const member = this.authService.currentMember();

    if (!member) {
      this.reservations.set([]);
      this.wallet.set(null);
      return;
    }

    this.refreshMemberData(member);
  }

  private refreshMemberData(member: any) {
    this.loadWallet(member.id);
    this.loadReservations(member);
    this.loadSites();
    this.loadCourts();
  }

  private loadWallet(memberId: number) {
    this.padelService.getMemberWallet(memberId).subscribe({
      next: (wallet: any) => {
        this.wallet.set(wallet);
      },
      error: error => {
        this.wallet.set(null);

        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  private loadSites() {
    this.padelService.getSites().subscribe({
      next: (sites: any[]) => {
        this.sites.set(sites ?? []);
      },
      error: error => {
        this.sites.set([]);

        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  private loadCourts() {
    this.padelService.getCourts().subscribe({
      next: (courts: any[]) => {
        this.courts.set(courts ?? []);
      },
      error: error => {
        this.courts.set([]);

        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  private loadReservations(member: any) {
    this.padelService.getAllReservations().subscribe({
      next: (reservations: any[]) => {
        const filteredReservations = (reservations ?? [])
          .filter(reservation => this.isReservationLinkedToMember(reservation, member))
          .sort((a, b) => {
            const dateA = new Date(`${a.date}T${a.startTime ?? '00:00'}`).getTime();
            const dateB = new Date(`${b.date}T${b.startTime ?? '00:00'}`).getTime();

            return dateA - dateB;
          });

        this.reservations.set(filteredReservations);
      },
      error: error => {
        this.reservations.set([]);

        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  getSiteName(reservation: any): string {
    if (reservation.siteName) {
      return reservation.siteName;
    }

    const court = this.courts().find(court => court.id === reservation.courtId);

    return court?.siteName
      || court?.site?.name
      || court?.site?.clubName
      || 'Club inconnu';
  }

  getReservationStatus(reservation: any): 'today' | 'upcoming' | 'past' {
    const reservationDate = new Date(`${reservation.date}T${reservation.startTime ?? '00:00'}`);
    const now = new Date();

    const sameDay =
      reservationDate.getFullYear() === now.getFullYear()
      && reservationDate.getMonth() === now.getMonth()
      && reservationDate.getDate() === now.getDate();

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

  getFilteredReservations(): any[] {
    const filter = this.selectedFilter();

    if (filter === 'all') {
      return this.reservations();
    }

    if (filter === 'upcoming') {
      return this.reservations().filter(reservation =>
        !this.isCancelledReservation(reservation)
        && (
          this.getReservationStatus(reservation) === 'today'
          || this.getReservationStatus(reservation) === 'upcoming'
        )
      );
    }

    if (filter === 'cancelled') {
      return this.reservations().filter(reservation =>
        this.isCancelledReservation(reservation)
      );
    }

    return this.reservations().filter(reservation =>
      !this.isCancelledReservation(reservation)
      && this.getReservationStatus(reservation) === 'past'
    );
  }

  countAllReservations(): number {
    return this.reservations().length;
  }

  countUpcomingReservations(): number {
    return this.reservations().filter(reservation =>
      !this.isCancelledReservation(reservation)
      && (
        this.getReservationStatus(reservation) === 'today'
        || this.getReservationStatus(reservation) === 'upcoming'
      )
    ).length;
  }

  countPastReservations(): number {
    return this.reservations().filter(reservation =>
      !this.isCancelledReservation(reservation)
      && this.getReservationStatus(reservation) === 'past'
    ).length;
  }

  countCancelledReservations(): number {
    return this.reservations().filter(reservation =>
      this.isCancelledReservation(reservation)
    ).length;
  }

  getParticipantsLabel(reservation: any): string {
    const participants = this.getActiveParticipants(reservation);

    if (participants.length > 0) {
      return participants
        .map(participant => this.getParticipantDisplayName(participant))
        .join(', ');
    }

    if (this.isOrganizer(reservation)) {
      const member = this.authService.currentMember();
      return member?.matricule || 'Organisateur';
    }

    return 'Participants non disponibles';
  }

  getReservationsToPay(): any[] {
    const member = this.authService.currentMember();

    if (!member) {
      return [];
    }

    return this.reservations().filter(reservation =>
      !this.isCancelledReservation(reservation)
      && this.getReservationStatus(reservation) !== 'past'
      && !this.isPaid(reservation)
      && !!this.findCurrentMemberParticipationId(reservation, member)
    );
  }

  getTotalReservationsToPayCentimes(): number {
    return this.getReservationsToPay()
      .reduce((total, reservation) => total + this.getParticipationAmountCentimes(reservation), 0);
  }

  getTotalReservationsToPayEuros(): number {
    return this.toEuros(this.getTotalReservationsToPayCentimes());
  }

  async payAllReservationParts() {
    const member = this.authService.currentMember();

    if (!member || this.payingAllReservations()) {
      return;
    }

    const reservationsToPay = this.getReservationsToPay();

    if (reservationsToPay.length === 0) {
      this.snackBar.open('Aucune participation à régler.', 'OK', {
        duration: 3000
      });
      return;
    }

    const total = this.getTotalReservationsToPayEuros();

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Tout régler',
        message: `Vous allez régler ${reservationsToPay.length} participation(s), pour un total de ${total} €. Confirmez-vous le paiement ?`,
        confirmLabel: `Payer ${total} €`,
        cancelLabel: 'Retour'
      }
    });

    const confirmed = await firstValueFrom(dialogRef.afterClosed());

    if (!confirmed) {
      return;
    }

    this.payingAllReservations.set(true);

    try {
      for (const reservation of reservationsToPay) {
        const participationId = this.findCurrentMemberParticipationId(reservation, member);

        if (!participationId) {
          continue;
        }

        const payment: any = await firstValueFrom(
          this.padelService.initierPaiementPourParticipation(participationId)
        );

        const paymentId = payment?.id ?? payment?.paiementId;

        if (!paymentId) {
          throw new Error('Identifiant de paiement introuvable.');
        }

        await firstValueFrom(
          this.padelService.confirmPayment(paymentId)
        );
      }

      this.snackBar.open('Toutes les participations ont été réglées.', 'OK', {
        duration: 3500
      });

      this.refreshMemberData(member);
    } catch (error: any) {
      this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
        duration: 5000
      });

      this.refreshMemberData(member);
    } finally {
      this.payingAllReservations.set(false);
    }
  }

  cancelReservation(reservation: any) {
    const member = this.authService.currentMember();

    if (!member) {
      return;
    }

    if (this.canLeavePublicMatch(reservation)) {
      this.leavePublicMatch(reservation, member);
      return;
    }

    if (this.canCancelWholeReservation(reservation)) {
      this.cancelWholeReservation(reservation);
      return;
    }

    this.snackBar.open('Action impossible pour cette réservation.', 'OK', {
      duration: 4000
    });
  }

  private leavePublicMatch(reservation: any, member: any) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Quitter le match',
        message: 'Tu vas quitter ce match public. Si ta participation était payée, le remboursement sera pris en compte. Continuer ?',
        confirmLabel: 'Quitter le match',
        cancelLabel: 'Retour'
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }

      this.padelService.leavePublicMatch(reservation.matchId, member.id).subscribe({
        next: () => {
          this.snackBar.open('Tu as quitté le match.', 'OK', {
            duration: 3500
          });

          this.refreshMemberData(member);
        },
        error: error => {
          this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
            duration: 5000
          });

          this.refreshMemberData(member);
        }
      });
    });
  }

  private cancelWholeReservation(reservation: any) {
    const member = this.authService.currentMember();

    if (!member) {
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: this.isPublicMatch(reservation)
          ? 'Annuler le match'
          : 'Annuler la réservation',
        message: this.isPublicMatch(reservation)
          ? 'Tu es organisateur de ce match public. Cette action annule le match pour tout le monde. Continuer ?'
          : 'Voulez-vous vraiment annuler cette réservation ?',
        confirmLabel: 'Oui, annuler',
        cancelLabel: 'Retour'
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }

      this.padelService.deleteReservation(reservation.id, member.id).subscribe({
        next: () => {
          this.reservations.update(reservations =>
            reservations.map(item =>
              item.id === reservation.id
                ? {
                    ...item,
                    reservationStatus: 'ANNULEE',
                    matchStatus: 'ANNULE'
                  }
                : item
            )
          );

          this.loadWallet(member.id);

          this.snackBar.open('Réservation annulée.', 'OK', {
            duration: 3000
          });
        },
        error: error => {
          this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
            duration: 5000
          });

          this.refreshMemberData(member);
        }
      });
    });
  }

  getCurrentMemberParticipation(reservation: any): any | null {
    const member = this.authService.currentMember();

    if (!member) {
      return null;
    }

    return this.getActiveParticipants(reservation).find((participant: any) =>
      participant.membreId === member.id
      || participant.memberId === member.id
      || participant.id === member.id
      || participant.matricule === member.matricule
    ) ?? null;
  }

  isPaid(reservation: any): boolean {
    const participation = this.getCurrentMemberParticipation(reservation);

    return participation?.statut === 'PAYEE'
      || participation?.status === 'PAYEE'
      || participation?.participationStatus === 'PAYEE'
      || participation?.statutParticipation === 'PAYEE'
      || reservation.paiementStatut === 'VALIDE'
      || reservation.paymentStatus === 'VALIDE'
      || reservation.participationStatus === 'PAYEE'
      || reservation.statutParticipation === 'PAYEE';
  }

  canPayReservation(reservation: any): boolean {
    const member = this.authService.currentMember();

    return !!member
      && !this.isPaid(reservation)
      && !this.isCancelledReservation(reservation)
      && !!this.findCurrentMemberParticipationId(reservation, member);
  }

  getReservationPaymentLabel(reservation: any): string {
    if (this.isPaid(reservation)) {
      return 'Part déjà payée';
    }

    if (this.canPayReservation(reservation)) {
      return 'Part à régler';
    }

    return 'Paiement non disponible';
  }

  isPayingReservation(reservation: any): boolean {
    return this.payingReservationId() === reservation?.id;
  }

  payReservation(reservation: any) {
    const member = this.authService.currentMember();

    if (!member) {
      return;
    }

    if (this.payingReservationId() || this.payingAllReservations()) {
      return;
    }

    if (this.isPaid(reservation)) {
      this.snackBar.open('Cette participation est déjà payée.', 'OK', {
        duration: 3500
      });
      return;
    }

    const participationId = this.findCurrentMemberParticipationId(reservation, member);

    if (!participationId) {
      this.snackBar.open(
        'Impossible de payer cette réservation : participation introuvable.',
        'OK',
        { duration: 5000 }
      );
      return;
    }

    const amount = this.getParticipationAmountLabel(reservation);

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Paiement',
        message: `Confirmez-vous le paiement de votre participation de ${amount} ?`,
        confirmLabel: `Payer ${amount}`,
        cancelLabel: 'Retour'
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }

      this.payingReservationId.set(reservation.id);

      this.padelService.initierPaiementPourParticipation(participationId).subscribe({
        next: (payment: any) => {
          const paymentId = payment?.id ?? payment?.paiementId;

          if (!paymentId) {
            this.payingReservationId.set(null);

            this.snackBar.open(
              'Le paiement a été créé, mais son identifiant est introuvable.',
              'OK',
              { duration: 5000 }
            );

            this.refreshMemberData(member);
            return;
          }

          this.padelService.confirmPayment(paymentId).subscribe({
            next: () => {
              this.payingReservationId.set(null);

              this.snackBar.open('Paiement confirmé.', 'OK', {
                duration: 3000
              });

              this.refreshMemberData(member);
            },
            error: error => {
              this.payingReservationId.set(null);

              this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
                duration: 5000
              });

              this.refreshMemberData(member);
            }
          });
        },
        error: error => {
          this.payingReservationId.set(null);

          this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
            duration: 5000
          });

          this.refreshMemberData(member);
        }
      });
    });
  }

  getAmountDue(): number {
    return this.getWalletAmountInEuros('amountDue');
  }

  getAmountPending(): number {
    return this.getWalletAmountInEuros('amountPending');
  }

  getAmountPaid(): number {
    return this.getWalletAmountInEuros('amountPaid');
  }

  getAmountRefunded(): number {
    return this.getWalletAmountInEuros('amountRefunded');
  }

  getParticipationAmountLabel(reservation: any): string {
    return `${this.toEuros(this.getParticipationAmountCentimes(reservation))} €`;
  }

  isPublicMatch(reservation: any): boolean {
    return reservation.matchType === 'PUBLIC';
  }

  isOrganizer(reservation: any): boolean {
    const member = this.authService.currentMember();

    if (!member) {
      return false;
    }

    return reservation.memberId === member.id
      || reservation.membreId === member.id
      || reservation.organisateurId === member.id
      || reservation.playerMatricule === member.matricule;
  }

  isCurrentMemberActiveParticipant(reservation: any): boolean {
    const member = this.authService.currentMember();

    if (!member) {
      return false;
    }

    return this.getActiveParticipants(reservation).some((participant: any) =>
      participant.membreId === member.id
      || participant.memberId === member.id
      || participant.id === member.id
      || participant.matricule === member.matricule
    );
  }

  canLeavePublicMatch(reservation: any): boolean {
    return this.isPublicMatch(reservation)
      && !this.isOrganizer(reservation)
      && this.isCurrentMemberActiveParticipant(reservation)
      && !!reservation.matchId
      && !this.isCancelledReservation(reservation)
      && this.getReservationStatus(reservation) !== 'past';
  }

  canCancelWholeReservation(reservation: any): boolean {
    return this.isOrganizer(reservation)
      && !this.isCancelledReservation(reservation)
      && this.getReservationStatus(reservation) !== 'past';
  }

  getCancelActionLabel(reservation: any): string {
    if (this.canLeavePublicMatch(reservation)) {
      return 'Quitter le match';
    }

    if (this.canCancelWholeReservation(reservation)) {
      return this.isPublicMatch(reservation)
        ? 'Annuler le match'
        : 'Annuler la réservation';
    }

    return '';
  }

  private getParticipationAmountCentimes(reservation: any): number {
    const participation = this.getCurrentMemberParticipation(reservation);

    return participation?.montantDuCentimes
      ?? participation?.montantParticipationCentimes
      ?? reservation?.montantDuCentimes
      ?? reservation?.montantParticipationCentimes
      ?? reservation?.participationAmountCentimes
      ?? reservation?.amountDueCentimes
      ?? 1500;
  }

  private getWalletAmountInEuros(fieldName: string): number {
    const wallet = this.wallet();

    if (!wallet) {
      return 0;
    }

    const centimesValue = wallet[`${fieldName}Centimes`];
    const rawValue = wallet[fieldName];

    return this.toEuros(centimesValue ?? rawValue ?? 0);
  }

  private toEuros(amountCentimes: number): number {
    return Math.round((amountCentimes ?? 0) / 100);
  }

  private isCancelledReservation(reservation: any): boolean {
    return reservation.reservationStatus === 'ANNULEE'
      || reservation.matchStatus === 'ANNULE';
  }

  private isReservationLinkedToMember(reservation: any, member: any): boolean {
    return reservation.memberId === member.id
      || reservation.membreId === member.id
      || reservation.organisateurId === member.id
      || reservation.playerMatricule === member.matricule
      || this.getActiveParticipants(reservation).some((participant: any) =>
        participant.id === member.id
        || participant.memberId === member.id
        || participant.membreId === member.id
        || participant.matricule === member.matricule
      );
  }

  private findCurrentMemberParticipationId(reservation: any, member: any): number | null {
    const participant = this.getActiveParticipants(reservation).find((item: any) =>
      item.membreId === member.id
      || item.memberId === member.id
      || item.id === member.id
      || item.matricule === member.matricule
    );

    const participantParticipationId =
      participant?.participationId
      ?? participant?.participationMatchId
      ?? participant?.idParticipation
      ?? participant?.participation?.id;

    return participantParticipationId ? Number(participantParticipationId) : null;
  }

  private getParticipants(reservation: any): any[] {
    if (Array.isArray(reservation?.participants)) {
      return reservation.participants;
    }

    if (Array.isArray(reservation?.members)) {
      return reservation.members;
    }

    return [];
  }

  private getActiveParticipants(reservation: any): any[] {
    return this.getParticipants(reservation)
      .filter((participant: any) => this.isActiveParticipation(participant));
  }

  private isActiveParticipation(participant: any): boolean {
    return !this.isInactiveParticipation(participant);
  }

  private isInactiveParticipation(participant: any): boolean {
    const status = this.getParticipationStatus(participant);

    return [
      'ANNULEE',
      'ANNULE',
      'CANCELLED',
      'DESINSCRIT',
      'DESINSCRITE',
      'REMBOURSEE',
      'REMBOURSE',
      'REFUSEE',
      'REFUSE',
      'SUPPRIMEE',
      'SUPPRIME',
      'LIBEREE',
      'LIBERE'
    ].includes(status);
  }

  private getParticipationStatus(participant: any): string {
    return (
      participant?.statut
      ?? participant?.status
      ?? participant?.participationStatus
      ?? participant?.statutParticipation
      ?? participant?.etat
      ?? ''
    )
      .toString()
      .trim()
      .toUpperCase();
  }

  private getParticipantDisplayName(participant: any): string {
    const firstName = participant?.firstName ?? participant?.prenom ?? '';
    const lastName = participant?.lastName ?? participant?.nom ?? '';
    const fullName = `${firstName} ${lastName}`.trim();

    return fullName || participant?.matricule || 'Joueur';
  }
}
