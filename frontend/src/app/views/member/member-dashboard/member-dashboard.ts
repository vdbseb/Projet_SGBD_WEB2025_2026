import { DatePipe } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';

import { AuthService } from '../../../services/auth.service';
import { PadelService } from '../../../services/padel.service';
import { PlayerWalletCard } from '../../player-wallet-card/player-wallet-card';

@Component({
  selector: 'app-member-dashboard',
  standalone: true,
  imports: [
    DatePipe,
    ReactiveFormsModule,
    RouterLink,
    MatIconModule,
    PlayerWalletCard
  ],
  templateUrl: './member-dashboard.html',
  styleUrl: './member-dashboard.css'
})
export class MemberDashboard implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly padelService = inject(PadelService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly snackBar = inject(MatSnackBar);

  currentMember = this.authService.currentMember;

  wallet = signal<any | null>(null);
  reservations = signal<any[]>([]);
  payments = signal<any[]>([]);
  activePenalties = signal<any[]>([]);
  loading = signal(false);
  savingProfile = signal(false);
  payingDebts = signal(false);
  payingAllReservations = signal(false);
  profileEditMode = signal(false);

  profileForm = this.formBuilder.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName: ['', [Validators.required, Validators.minLength(2)]],
    email: ['', [Validators.required, Validators.email]]
  });

  upcomingReservations = computed(() =>
    this.reservations()
      .filter(reservation => this.isUpcomingReservation(reservation))
      .sort((a, b) => this.getReservationTime(a) - this.getReservationTime(b))
  );

  pastReservations = computed(() =>
    this.reservations()
      .filter(reservation => this.isPastReservation(reservation))
      .sort((a, b) => this.getReservationTime(b) - this.getReservationTime(a))
  );

  nextReservation = computed(() => this.upcomingReservations()[0] ?? null);

  openDebts = computed(() => this.wallet()?.openDebts ?? []);

  amountDue = computed(() => this.walletAmountToEuros('amountDue'));
  amountPending = computed(() => this.walletAmountToEuros('amountPending'));
  amountPaid = computed(() => this.walletAmountToEuros('amountPaid'));
  amountRefunded = computed(() => this.walletAmountToEuros('amountRefunded'));

  ngOnInit() {
    const member = this.currentMember();

    if (!member) {
      return;
    }

    this.resetProfileForm(member);
    this.refreshDashboard();
  }

  refreshDashboard() {
    const member = this.currentMember();

    if (!member) {
      return;
    }

    this.loading.set(true);

    this.padelService.getMemberWallet(member.id).subscribe({
      next: (wallet: any) => this.wallet.set(wallet),
      error: () => this.showError('Impossible de charger le portefeuille membre.')
    });

    this.padelService.getActiveMemberPenalties(member.id).subscribe({
      next: (penalties: any[]) => this.activePenalties.set(penalties ?? []),
      error: () => this.activePenalties.set([])
    });

    this.padelService.getAllReservations().subscribe({
      next: (reservations: any[]) => {
        this.reservations.set(
          (reservations ?? []).filter(reservation => this.isMemberReservation(reservation, member))
        );
        this.loading.set(false);
      },
      error: () => {
        this.loading.set(false);
        this.showError('Impossible de charger les réservations du membre.');
      }
    });

    this.padelService.getPayments().subscribe({
      next: (payments: any[]) => {
        this.payments.set(
          (payments ?? [])
            .filter(payment => payment.membreId === member.id || payment.memberId === member.id)
            .sort((a, b) => this.getPaymentTime(b) - this.getPaymentTime(a))
        );
      },
      error: () => this.payments.set([])
    });
  }

  startProfileEdit() {
    const member = this.currentMember();

    if (!member) {
      return;
    }

    this.resetProfileForm(member);
    this.profileEditMode.set(true);
  }

  cancelProfileEdit() {
    const member = this.currentMember();

    if (member) {
      this.resetProfileForm(member);
    }

    this.profileEditMode.set(false);
  }

  saveProfile() {
    const member = this.currentMember();

    if (!member) {
      return;
    }

    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.savingProfile.set(true);

    this.padelService.updateOwnMemberProfile(member.id, this.profileForm.getRawValue()).subscribe({
      next: (updatedMember: any) => {
        this.authService.login(updatedMember);
        this.resetProfileForm(updatedMember);
        this.profileEditMode.set(false);
        this.savingProfile.set(false);
        this.snackBar.open('Profil mis à jour.', 'OK', { duration: 3000 });
      },
      error: () => {
        this.savingProfile.set(false);
        this.showError('Impossible de mettre à jour le profil.');
      }
    });
  }

  payAllDebts() {
    const member = this.currentMember();

    if (this.payingDebts()) {
      return;
    }

    if (!member || this.amountDue() <= 0) {
      return;
    }

    this.payingDebts.set(true);

    this.padelService.initierMemberDebtsPayment(member.id).subscribe({
      next: (payment: any) => {
        const paymentId = payment?.id ?? payment?.paiementId;

        if (!paymentId) {
          this.payingDebts.set(false);
          this.showError('Le paiement des dettes a été créé, mais son identifiant est introuvable.');
          this.refreshDashboard();
          return;
        }

        this.padelService.confirmPayment(paymentId).subscribe({
          next: () => {
            this.snackBar.open('Toutes les dettes ouvertes ont été réglées.', 'OK', {
              duration: 3500
            });

            this.payingDebts.set(false);
            this.refreshDashboard();
          },
          error: (error: any) => {
            this.payingDebts.set(false);

            const message =
              error?.error?.error ??
              error?.error?.message ??
              error?.error?.detail ??
              'Le paiement a été créé, mais la confirmation a échoué.';

            this.showError(message);
            this.refreshDashboard();
          }
        });
      },
      error: (error: any) => {
        this.payingDebts.set(false);

        const message =
          error?.error?.error ??
          error?.error?.message ??
          error?.error?.detail ??
          'Impossible de créer le paiement des dettes.';

        this.showError(message);
        this.refreshDashboard();
      }
    });
  }

  getReservationsToPay(): any[] {
    const member = this.currentMember();

    if (!member) {
      return [];
    }

    return this.reservations().filter(reservation =>
      !this.isCancelledReservation(reservation)
      && this.isUpcomingReservation(reservation)
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
    const member = this.currentMember();

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

    const confirmed = window.confirm(
      `Régler les matchs\n\nTu vas régler ${reservationsToPay.length} participation(s), pour un total de ${total} €. Confirmer le paiement ?`
    );

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

      this.snackBar.open('Toutes les participations à venir ont été réglées.', 'OK', {
        duration: 3500
      });

      this.refreshDashboard();
    } catch (error: any) {
      const message =
        error?.error?.error
        ?? error?.error?.message
        ?? error?.error?.detail
        ?? error?.message
        ?? 'Impossible de régler toutes les participations.';

      this.showError(message);
      this.refreshDashboard();
    } finally {
      this.payingAllReservations.set(false);
    }
  }

  getCurrentMemberParticipation(reservation: any): any | null {
    const member = this.currentMember();

    if (!member) {
      return null;
    }

    return this.getParticipants(reservation).find((participant: any) =>
      participant.membreId === member.id
      || participant.memberId === member.id
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

  getParticipationAmountLabel(reservation: any): string {
    return `${this.toEuros(this.getParticipationAmountCentimes(reservation))} €`;
  }

  getDebtReasonLabel(debt: any): string {
    const reason = debt?.raison ?? debt?.reason ?? '';

    switch (reason) {
      case 'PARTICIPATION_IMPAYEE':
        return 'Participation impayée';
      case 'SOLDE_ORGANISATEUR':
        return 'Solde organisateur non réglé';
      case 'MATCH_PRIVE_INCOMPLET':
        return 'Match privé incomplet';
      case 'ORGANISATEUR_NON_PAYE':
        return 'Organisateur non payé dans les délais';
      default:
        return this.cleanDebtLabel(reason || 'Dette à régler');
    }
  }

  getDebtReservation(debt: any): any | null {
    const reservationId = debt?.reservationId ?? debt?.reservation?.id;

    if (!reservationId) {
      return null;
    }

    return this.reservations().find(reservation =>
      Number(reservation.id) === Number(reservationId)
    ) ?? null;
  }

  getDebtContextLabel(debt: any): string {
    const reservation = this.getDebtReservation(debt);

    if (!reservation) {
      return 'Réservation concernée';
    }

    return this.getReservationLabel(reservation);
  }

  getDebtDateLabel(debt: any): string {
    const reservation = this.getDebtReservation(debt);

    if (!reservation) {
      return '';
    }

    const date = reservation.date
      ? new Date(reservation.date).toLocaleDateString('fr-BE')
      : '';

    const start = reservation.startTime?.substring(0, 5) ?? '';
    const end = reservation.endTime?.substring(0, 5) ?? '';

    if (date && start && end) {
      return `${date} · ${start} - ${end}`;
    }

    return date;
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

  private findCurrentMemberParticipationId(reservation: any, member: any): number | null {
    const directParticipationId =
      reservation?.participationId
      ?? reservation?.participationMatchId
      ?? reservation?.idParticipation
      ?? reservation?.currentMemberParticipationId
      ?? reservation?.maParticipationId;

    if (directParticipationId) {
      return Number(directParticipationId);
    }

    const participants = this.getParticipants(reservation);

    const participant = participants.find((item: any) =>
      item.membreId === member.id
      || item.memberId === member.id
      || item.matricule === member.matricule
    );

    const participantParticipationId =
      participant?.participationId
      ?? participant?.participationMatchId
      ?? participant?.idParticipation
      ?? participant?.participation?.id;

    return participantParticipationId ? Number(participantParticipationId) : null;
  }

  hasActivePenalty(): boolean {
    return this.activePenalties().length > 0;
  }

  getMainPenalty(): any | null {
    return this.activePenalties()[0] ?? null;
  }

  getPenaltyReason(penalty: any): string {
    const rawReason = (penalty?.raison ?? penalty?.reason ?? '').trim();

    switch (rawReason) {
      case 'MATCH_PRIVE_INCOMPLET':
        return 'Match privé incomplet';
      case 'ORGANISATEUR_NON_PAYE':
        return 'Organisateur non payé dans les délais';
      case 'PARTICIPATION_IMPAYEE':
        return 'Participation non payée dans les délais';
      case 'SOLDE_ORGANISATEUR':
        return 'Solde organisateur non réglé';
      default:
        return this.cleanSentence(rawReason || 'Pénalité de réservation');
    }
  }

  getPenaltyEndDate(penalty: any): string {
    if (!penalty?.dateFin) {
      return 'date inconnue';
    }

    return new Date(penalty.dateFin).toLocaleDateString('fr-BE');
  }

  getPenaltyPeriod(penalty: any): string {
    const startDate = penalty?.dateDebut
      ? new Date(penalty.dateDebut).toLocaleDateString('fr-BE')
      : 'date inconnue';

    const endDate = this.getPenaltyEndDate(penalty);

    return `Du ${startDate} au ${endDate}`;
  }

  getPenaltySummary(penalty: any): string {
    if (!penalty) {
      return '';
    }

    return `Réservations bloquées jusqu’au ${this.getPenaltyEndDate(penalty)}.`;
  }

  getPenaltyRemainingLabel(penalty: any): string {
    const days = Number(penalty?.joursRestants ?? 0);

    if (days <= 0) {
      return 'Dernier jour de blocage';
    }

    if (days === 1) {
      return '1 jour restant';
    }

    return `${days} jours restants`;
  }

  getPenaltyMatchLabel(penalty: any): string {
    if (!penalty) {
      return '';
    }

    const court = penalty.courtName ?? '';
    const site = penalty.siteName ?? '';
    const date = penalty.matchDate
      ? new Date(penalty.matchDate).toLocaleDateString('fr-BE')
      : '';

    const start = penalty.matchStartTime?.substring(0, 5) ?? '';
    const end = penalty.matchEndTime?.substring(0, 5) ?? '';

    const place = [court, site].filter(Boolean).join(' — ');
    const time = date && start && end ? `${date} de ${start} à ${end}` : '';

    if (place && time) {
      return `${place} · ${time}`;
    }

    if (time) {
      return `Match du ${time}`;
    }

    if (place) {
      return place;
    }

    return penalty.matchId ? 'Match concerné' : '';
  }

  showMainPenaltyMessage() {
    const penalty = this.getMainPenalty();

    if (!penalty) {
      return;
    }

    const message = `${this.getPenaltySummary(penalty)} Raison : ${this.getPenaltyReason(penalty)}.`;

    this.snackBar.open(message, 'OK', { duration: 7000 });
  }

  getTypeLabel(member: any): string {
    const code = member?.type?.code ?? member?.typeCode ?? '';

    return switchTypeLabel(code);
  }

  getReservationLabel(reservation: any): string {
    const court = reservation.courtName ?? `Terrain ${reservation.courtId ?? ''}`.trim();
    const site = reservation.siteName ? ` — ${reservation.siteName}` : '';

    return `${court}${site}`;
  }

  getParticipants(reservation: any): any[] {
    if (Array.isArray(reservation?.participants)) {
      return reservation.participants;
    }

    if (Array.isArray(reservation?.members)) {
      return reservation.members;
    }

    if (Array.isArray(reservation?.participantMatricules)) {
      return reservation.participantMatricules.map((matricule: string) => ({
        matricule
      }));
    }

    return [];
  }

  getParticipantDisplayName(participant: any): string {
    const firstName = participant?.firstName ?? participant?.prenom ?? '';
    const lastName = participant?.lastName ?? participant?.nom ?? '';
    const fullName = `${firstName} ${lastName}`.trim();

    return fullName || participant?.matricule || 'Joueur';
  }

  isCurrentMember(participant: any): boolean {
    const member = this.currentMember();

    if (!member || !participant) {
      return false;
    }

    return participant.membreId === member.id
      || participant.memberId === member.id
      || participant.matricule === member.matricule;
  }

  getNombrePlacesLibres(reservation: any): number {
    return Math.max(0, 4 - this.getParticipants(reservation).length);
  }

  getPaymentStatusClass(status: string): string {
    switch (status) {
      case 'VALIDE':
        return 'bg-emerald-100 text-emerald-700';
      case 'EN_ATTENTE':
        return 'bg-amber-100 text-amber-700';
      case 'REMBOURSE':
        return 'bg-violet-100 text-violet-700';
      case 'REFUSE':
      case 'ANNULE':
        return 'bg-red-100 text-red-700';
      default:
        return 'bg-slate-100 text-slate-600';
    }
  }

  toEuros(value: number | null | undefined): number {
    return (value ?? 0) / 100;
  }

  walletAmountToEuros(fieldName: string): number {
    const wallet = this.wallet();

    if (!wallet) {
      return 0;
    }

    const centimesValue = wallet[`${fieldName}Centimes`];
    const rawValue = wallet[fieldName];

    return this.toEuros(centimesValue ?? rawValue ?? 0);
  }

  private resetProfileForm(member: any) {
    this.profileForm.reset({
      firstName: member.firstName ?? '',
      lastName: member.lastName ?? '',
      email: member.email ?? ''
    });
  }

  private isMemberReservation(reservation: any, member: any): boolean {
    return reservation.memberId === member.id
      || reservation.membreId === member.id
      || reservation.organisateurId === member.id
      || reservation.playerMatricule === member.matricule
      || reservation.participantMatricules?.includes(member.matricule)
      || reservation.members?.some((participant: any) =>
        participant.id === member.id
        || participant.memberId === member.id
        || participant.membreId === member.id
        || participant.matricule === member.matricule
      )
      || reservation.participants?.some((participant: any) =>
        participant.id === member.id
        || participant.memberId === member.id
        || participant.membreId === member.id
        || participant.matricule === member.matricule
      );
  }

  private isUpcomingReservation(reservation: any): boolean {
    return !this.isCancelledReservation(reservation)
      && this.getReservationTime(reservation) >= Date.now();
  }

  private isPastReservation(reservation: any): boolean {
    return !this.isCancelledReservation(reservation)
      && this.getReservationTime(reservation) < Date.now();
  }

  private isCancelledReservation(reservation: any): boolean {
    return reservation.reservationStatus === 'ANNULEE'
      || reservation.matchStatus === 'ANNULE';
  }

  private getReservationTime(reservation: any): number {
    return new Date(`${reservation.date}T${reservation.startTime ?? '00:00'}`).getTime();
  }

  private getPaymentTime(payment: any): number {
    return new Date(payment.datePaiement ?? payment.dateCreation ?? 0).getTime();
  }

  private showError(message: string) {
    this.snackBar.open(message, 'OK', { duration: 4500 });
  }

  private cleanSentence(value: string): string {
    return value.trim().replace(/[.。]+$/g, '');
  }

  private cleanDebtLabel(value: string): string {
    return value
      .toString()
      .split('_')
      .join(' ')
      .toLowerCase()
      .replace(/^\w/, letter => letter.toUpperCase());
  }
}

function switchTypeLabel(code: string): string {
  switch (code?.toUpperCase()) {
    case 'GLOBAL':
      return 'Membre global';
    case 'SITE':
      return 'Membre du site';
    case 'LIBRE':
      return 'Membre libre';
    default:
      return 'Type inconnu';
  }
}
