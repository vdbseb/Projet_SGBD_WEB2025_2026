import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';
import { ConfirmDialogComponent } from '../confirm-dialog/confirm-dialog';

type PublicMatchFilter =
  | 'ALL'
  | 'AVAILABLE'
  | 'MINE'
  | 'FULL'
  | 'TODAY'
  | 'WEEK';

type PublicMatchViewMode = 'CARDS' | 'LIST';

type PublicMatchSort =
  | 'DATE_ASC'
  | 'DATE_DESC'
  | 'SPOTS_DESC'
  | 'SITE_ASC';

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
  private readonly padelService = inject(PadelService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly authService = inject(AuthService);

  reservations = signal<any[]>([]);
  courts = signal<any[]>([]);
  sites = signal<any[]>([]);

  searchTerm = signal('');
  selectedSiteFilter = signal<string>('ALL');
  selectedFilter = signal<PublicMatchFilter>('ALL');
  viewMode = signal<PublicMatchViewMode>('CARDS');
  sortMode = signal<PublicMatchSort>('DATE_ASC');
  loadingMatchId = signal<number | null>(null);

  publicMatches = computed(() => this.getPublicMatches());
  filteredMatches = computed(() => this.buildFilteredMatches());

  ngOnInit() {
    this.padelService.getCourts().subscribe({
      next: (courts: any[]) => this.courts.set(courts ?? []),
      error: () => this.courts.set([])
    });

    this.padelService.getSites().subscribe({
      next: (sites: any[]) => this.sites.set(sites ?? []),
      error: () => this.sites.set([])
    });

    this.loadReservations();
  }

  loadReservations() {
    this.padelService.getAllReservations().subscribe({
      next: (reservations: any[]) => this.reservations.set(reservations ?? []),
      error: () => {
        this.reservations.set([]);

        this.snackBar.open('Impossible de charger les matchs publics.', 'OK', {
          duration: 4000
        });
      }
    });
  }

  setFilter(filter: PublicMatchFilter) {
    this.selectedFilter.set(filter);
  }

  setViewMode(mode: PublicMatchViewMode) {
    this.viewMode.set(mode);
  }

  setSearchTerm(event: Event) {
    const input = event.target as HTMLInputElement;
    this.searchTerm.set(input.value ?? '');
  }

  setSiteFilter(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.selectedSiteFilter.set(select.value);
  }

  setSortMode(event: Event) {
    const select = event.target as HTMLSelectElement;
    this.sortMode.set(select.value as PublicMatchSort);
  }

  getPublicMatches(): any[] {
    const now = new Date();

    return this.reservations().filter(reservation => {
      const reservationDate = this.getReservationDateTime(reservation);

      return reservation.matchType === 'PUBLIC'
        && reservation.reservationStatus !== 'ANNULEE'
        && reservation.matchStatus !== 'ANNULE'
        && reservationDate > now;
    });
  }

  getAvailableSiteNames(): string[] {
    const names = this.publicMatches()
      .map(reservation => this.getSiteName(reservation))
      .filter(siteName => siteName && siteName !== 'Site inconnu');

    return [...new Set(names)].sort((a, b) => a.localeCompare(b));
  }

  private buildFilteredMatches(): any[] {
    const search = this.normalizeText(this.searchTerm());
    const filter = this.selectedFilter();
    const selectedSite = this.selectedSiteFilter();

    let matches = this.publicMatches().filter(reservation => {
      if (selectedSite !== 'ALL' && this.getSiteName(reservation) !== selectedSite) {
        return false;
      }

      if (filter === 'AVAILABLE' && !this.canJoinMatch(reservation)) {
        return false;
      }

      if (filter === 'MINE' && !this.isParticipant(reservation) && !this.isOrganizer(reservation)) {
        return false;
      }

      if (filter === 'FULL' && !this.isFull(reservation)) {
        return false;
      }

      if (filter === 'TODAY' && !this.isToday(reservation)) {
        return false;
      }

      if (filter === 'WEEK' && !this.isThisWeek(reservation)) {
        return false;
      }

      if (!search) {
        return true;
      }

      const searchable = this.normalizeText([
        this.getCourtName(reservation),
        this.getSiteName(reservation),
        reservation.date,
        reservation.startTime,
        reservation.endTime
      ].join(' '));

      return searchable.includes(search);
    });

    matches = [...matches].sort((a, b) => {
      switch (this.sortMode()) {
        case 'DATE_DESC':
          return this.getReservationDateTime(b).getTime() - this.getReservationDateTime(a).getTime();

        case 'SPOTS_DESC':
          return this.getRemainingSpots(b) - this.getRemainingSpots(a);

        case 'SITE_ASC': {
          const siteCompare = this.getSiteName(a).localeCompare(this.getSiteName(b));

          return siteCompare !== 0
            ? siteCompare
            : this.getReservationDateTime(a).getTime() - this.getReservationDateTime(b).getTime();
        }

        case 'DATE_ASC':
        default:
          return this.getReservationDateTime(a).getTime() - this.getReservationDateTime(b).getTime();
      }
    });

    return matches;
  }

  countAvailableMatches(): number {
    return this.publicMatches().filter(reservation => this.canJoinMatch(reservation)).length;
  }

  countMyMatches(): number {
    return this.publicMatches().filter(reservation =>
      this.isParticipant(reservation) || this.isOrganizer(reservation)
    ).length;
  }

  countFullMatches(): number {
    return this.publicMatches().filter(reservation => this.isFull(reservation)).length;
  }

  countTodayMatches(): number {
    return this.publicMatches().filter(reservation => this.isToday(reservation)).length;
  }

  countWeekMatches(): number {
    return this.publicMatches().filter(reservation => this.isThisWeek(reservation)).length;
  }

  getParticipantsCount(reservation: any): number {
    return Math.min(4, this.getActiveParticipants(reservation).length);
  }

  getRemainingSpots(reservation: any): number {
    return Math.max(0, 4 - this.getParticipantsCount(reservation));
  }

  isFull(reservation: any): boolean {
    return this.getParticipantsCount(reservation) >= 4;
  }

  isOrganizer(reservation: any): boolean {
    const member = this.authService.currentMember();

    if (!member) {
      return false;
    }

    return reservation.memberId === member.id
      || reservation.membreId === member.id
      || reservation.organisateurId === member.id;
  }

  isParticipant(reservation: any): boolean {
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

  canJoinMatch(reservation: any): boolean {
    return !this.isFull(reservation)
      && !this.isParticipant(reservation)
      && !this.isOrganizer(reservation)
      && !!this.authService.currentMember();
  }

  getActionLabel(reservation: any): string {
    if (this.loadingMatchId() === reservation.id) {
      return 'Paiement...';
    }

    if (this.isParticipant(reservation)) {
      return 'Quitter le match';
    }

    if (this.isOrganizer(reservation)) {
      return 'Organisateur';
    }

    if (this.isFull(reservation)) {
      return 'Match complet';
    }

    return 'Payer 15€ et rejoindre';
  }

  getCourtName(reservation: any): string {
    const court = this.courts().find(c => c.id === reservation.courtId);

    return reservation.courtName
      || court?.name
      || court?.nom
      || 'Terrain inconnu';
  }

  getSiteName(reservation: any): string {
    if (reservation.siteName) {
      return reservation.siteName;
    }

    const court = this.courts().find(c => c.id === reservation.courtId);

    const courtSiteId =
      court?.siteId
      ?? court?.site?.id
      ?? reservation.siteId;

    const site = this.sites().find(s =>
      Number(s.id) === Number(courtSiteId)
    );

    return site?.clubName
      || site?.nom
      || site?.name
      || 'Site inconnu';
  }

  getMatchDateLabel(reservation: any): string {
    return this.getReservationDateTime(reservation).toLocaleDateString('fr-BE');
  }

  getMatchTimeLabel(reservation: any): string {
    const start = reservation.startTime?.substring(0, 5) ?? '';
    const end = reservation.endTime?.substring(0, 5) ?? '';

    return `${start} - ${end}`;
  }

  getMatchStatusClass(reservation: any): string {
    if (this.isParticipant(reservation) || this.isOrganizer(reservation)) {
      return 'bg-violet-100 text-violet-700';
    }

    if (this.isFull(reservation)) {
      return 'bg-red-100 text-red-700';
    }

    return 'bg-emerald-50 text-emerald-700';
  }

  getMatchStatusLabel(reservation: any): string {
    if (this.isOrganizer(reservation)) {
      return 'Organisé par toi';
    }

    if (this.isParticipant(reservation)) {
      return 'Déjà rejoint';
    }

    if (this.isFull(reservation)) {
      return 'Complet';
    }

    return `${this.getRemainingSpots(reservation)} place(s) libre(s)`;
  }

  joinMatch(reservation: any) {
    const member = this.authService.currentMember();
    const matchId = reservation.matchId;

    if (!member) {
      this.snackBar.open('Connecte-toi avec ton matricule pour rejoindre un match.', 'OK', {
        duration: 4000
      });
      return;
    }

    if (!matchId) {
      this.snackBar.open('Match introuvable pour cette réservation.', 'OK', {
        duration: 4000
      });
      return;
    }

    if (this.isOrganizer(reservation)) {
      this.snackBar.open('Tu es déjà organisateur de ce match.', 'OK', {
        duration: 3000
      });
      return;
    }

    if (this.isFull(reservation)) {
      this.snackBar.open('Ce match est déjà complet.', 'OK', {
        duration: 3000
      });
      return;
    }

    if (this.isParticipant(reservation)) {
      this.snackBar.open('Tu participes déjà à ce match.', 'OK', {
        duration: 3000
      });
      return;
    }

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Payer et rejoindre le match',
        message: `Confirmer le paiement de 15€ pour rejoindre ${this.getCourtName(reservation)} le ${this.getMatchDateLabel(reservation)} à ${reservation.startTime?.substring(0, 5) ?? ''} ?`,
        confirmLabel: 'Payer 15€',
        cancelLabel: 'Retour'
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }

      this.loadingMatchId.set(reservation.id);

      this.padelService.joinPublicMatch(matchId, member.id).subscribe({
        next: (joinResponse: any) => {
          const participationId = Number(joinResponse?.participationId);

          if (!participationId) {
            this.loadingMatchId.set(null);

            this.snackBar.open(
              'Tu as rejoint le match, mais la participation à payer est introuvable.',
              'OK',
              { duration: 6000 }
            );

            this.loadReservations();
            return;
          }

          this.payJoinedParticipation(participationId);
        },
        error: (error: any) => {
          this.loadingMatchId.set(null);

          const message =
            error?.error?.error
            ?? error?.error?.message
            ?? error?.error?.detail
            ?? 'Impossible de rejoindre ce match.';

          this.snackBar.open(message, 'OK', {
            duration: 5000
          });

          this.loadReservations();
        }
      });
    });
  }

  private payJoinedParticipation(participationId: number) {
    this.padelService.initierPaiementPourParticipation(participationId).subscribe({
      next: (payment: any) => {
        const paymentId = payment?.id ?? payment?.paiementId;

        if (!paymentId) {
          this.loadingMatchId.set(null);

          this.snackBar.open(
            'Le paiement a été créé, mais son identifiant est introuvable.',
            'OK',
            { duration: 5000 }
          );

          this.loadReservations();
          return;
        }

        this.padelService.confirmPayment(paymentId).subscribe({
          next: () => {
            this.loadingMatchId.set(null);

            this.snackBar.open('Paiement confirmé. Tu as rejoint le match.', 'OK', {
              duration: 3500
            });

            this.loadReservations();
          },
          error: (error: any) => {
            this.loadingMatchId.set(null);

            const message =
              error?.error?.error
              ?? error?.error?.message
              ?? error?.error?.detail
              ?? 'Tu as rejoint le match, mais la confirmation du paiement a échoué.';

            this.snackBar.open(message, 'OK', {
              duration: 6000
            });

            this.loadReservations();
          }
        });
      },
      error: (error: any) => {
        this.loadingMatchId.set(null);

        const message =
          error?.error?.error
          ?? error?.error?.message
          ?? error?.error?.detail
          ?? 'Tu as rejoint le match, mais le paiement n’a pas pu être lancé.';

        this.snackBar.open(message, 'OK', {
          duration: 6000
        });

        this.loadReservations();
      }
    });
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

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Quitter le match',
        message: `Tu vas quitter ${this.getCourtName(reservation)} le ${this.getMatchDateLabel(reservation)}. Continuer ?`,
        confirmLabel: 'Quitter',
        cancelLabel: 'Retour'
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (!confirmed) {
        return;
      }

      this.loadingMatchId.set(reservation.id);

      this.padelService.leavePublicMatch(matchId, member.id).subscribe({
        next: () => {
          this.loadingMatchId.set(null);

          this.snackBar.open(
            'Tu as quitté le match. Si ta participation était payée, le remboursement est pris en compte.',
            'OK',
            { duration: 4500 }
          );

          this.loadReservations();
        },
        error: (error: any) => {
          this.loadingMatchId.set(null);

          const message =
            error?.error?.error
            ?? error?.error?.message
            ?? error?.error?.detail
            ?? 'Impossible de quitter le match.';

          this.snackBar.open(message, 'OK', {
            duration: 5000
          });

          this.loadReservations();
        }
      });
    });
  }

  handlePrimaryAction(reservation: any) {
    if (this.loadingMatchId() === reservation.id) {
      return;
    }

    if (this.isParticipant(reservation)) {
      this.leaveMatch(reservation);
      return;
    }

    if (this.canJoinMatch(reservation)) {
      this.joinMatch(reservation);
    }
  }

  private getReservationDateTime(reservation: any): Date {
    return new Date(`${reservation.date}T${reservation.startTime ?? '00:00:00'}`);
  }

  private isToday(reservation: any): boolean {
    const date = this.getReservationDateTime(reservation);
    const today = new Date();

    return date.getFullYear() === today.getFullYear()
      && date.getMonth() === today.getMonth()
      && date.getDate() === today.getDate();
  }

  private isThisWeek(reservation: any): boolean {
    const date = this.getReservationDateTime(reservation);
    const now = new Date();

    const end = new Date(now);
    end.setDate(now.getDate() + 7);
    end.setHours(23, 59, 59, 999);

    return date >= now && date <= end;
  }

  private getParticipants(reservation: any): any[] {
    if (Array.isArray(reservation?.participants)) {
      return reservation.participants;
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

  private normalizeText(value: string): string {
    return (value ?? '')
      .toString()
      .normalize('NFD')
      .replace(/[\u0300-\u036f]/g, '')
      .toLowerCase()
      .trim();
  }
}
