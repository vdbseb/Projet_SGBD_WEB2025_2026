import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { DatePipe } from '@angular/common';

import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';
import { PadelSite, PadelCourt } from '../../shared/site.model';

import { PadelCardComponent } from '../padel-card/padel-card';
import { DateSelectorComponent } from '../date-selector/date-selector';
import { TimeSlotsComponent } from '../time-slot/time-slot';
import { MatchSelectorComponent, MatchType } from '../match-selector/match-selector';

@Component({
  selector: 'app-reservation-page',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule,
    PadelCardComponent,
    DateSelectorComponent,
    TimeSlotsComponent,
    MatchSelectorComponent,
    DatePipe
  ],
  templateUrl: './reservation-page.html'
})
export class ReservationPage implements OnInit {
  private route = inject(ActivatedRoute);
  private padelService = inject(PadelService);
  authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);

  site = signal<PadelSite | undefined>(undefined);
  selectedCourt = signal<PadelCourt | undefined>(undefined);
  selectedDate = signal<Date | null>(new Date());
  selectedTime = signal<string | null>(null);
  reservedTimes = signal<string[]>([]);

  selectedMatchType = signal<MatchType>('PRIVATE');
  participantMatricules = signal<string[]>([]);
  isMatchSelectionValid = signal(false);

  matchDurationMinutes = signal(90);
  pauseMinutes = signal(15);

  closedDays = signal<any[]>([]);
  activePenalties = signal<any[]>([]);
  loadingPenalties = signal(false);

  ngOnInit() {
    this.loadActivePenalties();

    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : NaN;

    if (!Number.isFinite(id)) {
      this.snackBar.open('Site introuvable.', 'OK', {
        duration: 4000
      });
      return;
    }

    this.padelService.getSiteById(id).subscribe({
      next: site => {
        this.site.set(site);

        const year = new Date().getFullYear();

        this.padelService.getSiteSchedule(site.id, year).subscribe({
          next: schedule => {
            this.matchDurationMinutes.set(schedule.duree_match_minutes ?? 90);
            this.pauseMinutes.set(schedule.pause_minutes ?? 15);
          },
          error: () => {
            this.matchDurationMinutes.set(90);
            this.pauseMinutes.set(15);
          }
        });

        this.loadClosingDays(site.id);
      },
      error: () => {
        this.snackBar.open('Impossible de charger le site.', 'OK', {
          duration: 4000
        });
      }
    });
  }

  private loadActivePenalties() {
    const currentMember = this.authService.currentMember();

    if (!currentMember?.id) {
      this.activePenalties.set([]);
      return;
    }

    this.loadingPenalties.set(true);

    this.padelService.getActiveMemberPenalties(currentMember.id).subscribe({
      next: penalties => {
        this.activePenalties.set(penalties ?? []);
        this.loadingPenalties.set(false);
      },
      error: () => {
        this.activePenalties.set([]);
        this.loadingPenalties.set(false);
      }
    });
  }

  private loadClosingDays(siteId: number) {
    this.padelService.getSiteClosingDays(siteId).subscribe({
      next: siteDays => {
        this.padelService.getGlobalClosingDays().subscribe({
          next: globalDays => {
            this.closedDays.set([
              ...siteDays,
              ...globalDays
            ]);
          },
          error: () => {
            this.closedDays.set(siteDays);
          }
        });
      },
      error: () => {
        this.closedDays.set([]);
      }
    });
  }

  onTimeSelected(time: string) {
    this.selectedTime.set(time);
  }

  onDateSelected(date: Date) {
    const formattedDate = this.formatLocalDate(date);

    const closingDay = this.closedDays().find(day =>
      day.dateFermeture === formattedDate
    );

    if (closingDay) {
      const reason = closingDay.raison
        ? ` : ${closingDay.raison}`
        : '.';

      this.snackBar.open(`Le centre est fermé à cette date${reason}`, 'OK', {
        duration: 5000
      });

      this.selectedDate.set(null);
      this.selectedTime.set(null);
      this.reservedTimes.set([]);
      return;
    }

    this.selectedDate.set(date);
    this.selectedTime.set(null);
    this.loadReservedTimes();
  }

  selectCourt(court: PadelCourt) {
    if (this.hasActivePenalty()) {
      this.snackBar.open(this.getPenaltyMessage(), 'OK', {
        duration: 7000
      });
      return;
    }

    if (!court.active) {
      this.snackBar.open('Ce terrain est actuellement indisponible.', 'OK', {
        duration: 4000
      });
      return;
    }

    if (court.maintenance) {
      this.snackBar.open('Ce terrain est actuellement en maintenance.', 'OK', {
        duration: 4000
      });
      return;
    }

    this.selectedCourt.set(court);
    this.selectedTime.set(null);
    this.loadReservedTimes();
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

  getPenaltyMessage(): string {
    const penalty = this.getMainPenalty();

    if (!penalty) {
      return '';
    }

    return `Réservation impossible jusqu’au ${this.getPenaltyEndDate(penalty)}. Raison : ${this.getPenaltyReason(penalty)}.`;
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

  private cleanSentence(value: string): string {
    return value.trim().replace(/[.。]+$/g, '');
  }

  private formatLocalDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }

  loadReservedTimes() {
    const court = this.selectedCourt();
    const date = this.selectedDate();

    if (!court || !date) {
      this.reservedTimes.set([]);
      return;
    }

    const formattedDate = this.formatLocalDate(date);

    this.padelService.getReservations(court.id, formattedDate).subscribe({
      next: reservations => {
        this.reservedTimes.set(
          reservations
            .filter((reservation: any) => reservation.reservationStatus !== 'ANNULEE')
            .filter((reservation: any) => reservation.matchStatus !== 'ANNULE')
            .map((reservation: any) => reservation.startTime)
        );
      },
      error: () => {
        this.reservedTimes.set([]);
      }
    });
  }

  onMatchTypeChanged(type: MatchType) {
    this.selectedMatchType.set(type);
  }

  onParticipantsChanged(participants: string[]) {
    this.participantMatricules.set(participants);
  }

  onMatchValidityChanged(isValid: boolean) {
    this.isMatchSelectionValid.set(isValid);
  }

  onConfirmBooking() {
    const court = this.selectedCourt();
    const date = this.selectedDate();
    const time = this.selectedTime();
    const currentMember = this.authService.currentMember();

    if (this.hasActivePenalty()) {
      this.snackBar.open(this.getPenaltyMessage(), 'OK', {
        duration: 7000
      });
      return;
    }

    if (!court || !date || !time || !currentMember || !this.isMatchSelectionValid()) {
      this.snackBar.open('Veuillez compléter toutes les informations de réservation.', 'OK', {
        duration: 4000
      });
      return;
    }

    if (!court.active || court.maintenance) {
      this.snackBar.open('Ce terrain n’est pas disponible à la réservation.', 'OK', {
        duration: 4000
      });
      return;
    }

    const startTime = `${time}:00`;

    const [hour, minute] = time.split(':').map(Number);

    const startDateTime = new Date();
    startDateTime.setHours(hour, minute, 0, 0);

    const endDateTime = new Date(startDateTime);
    endDateTime.setMinutes(endDateTime.getMinutes() + this.matchDurationMinutes());

    const endTime = `${endDateTime.getHours().toString().padStart(2, '0')}:${endDateTime
      .getMinutes()
      .toString()
      .padStart(2, '0')}:00`;

    const newReservation = {
      id: null,
      courtId: court.id,
      courtName: null,
      memberId: currentMember.id,
      playerMatricule: null,
      date: this.formatLocalDate(date),
      startTime,
      endTime,
      matchType: this.selectedMatchType(),
      participantMatricules: this.participantMatricules()
    };

    this.padelService.createReservation(newReservation).subscribe({
      next: () => {
        this.snackBar.open('Réservation confirmée !', 'OK', {
          duration: 3000
        });

        this.selectedTime.set(null);
        this.loadReservedTimes();
      },
      error: (error: any) => {
        const message =
          error?.error?.detail ??
          error?.error?.message ??
          'Erreur lors de la réservation. Veuillez réessayer.';

        this.snackBar.open(message, 'OK', {
          duration: 5000
        });
      }
    });
  }
}
