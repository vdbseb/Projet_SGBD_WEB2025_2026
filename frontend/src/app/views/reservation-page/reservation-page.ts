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
import { getHttpErrorUserMessage } from '../../shared/api-error.util';

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

  ngOnInit() {
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
        this.loadSiteSchedule(site.id);
        this.loadClosingDays(site.id);
      },
      error: error => {
        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  private loadSiteSchedule(siteId: number) {
    const year = new Date().getFullYear();

    this.padelService.getSiteSchedule(siteId, year).subscribe({
      next: schedule => {
        this.matchDurationMinutes.set(schedule.duree_match_minutes ?? 90);
        this.pauseMinutes.set(schedule.pause_minutes ?? 15);
      },
      error: error => {
        this.matchDurationMinutes.set(90);
        this.pauseMinutes.set(15);

        this.snackBar.open(
          `${getHttpErrorUserMessage(error)} Les durées par défaut seront utilisées.`,
          'OK',
          { duration: 5000 }
        );
      }
    });
  }

  private loadClosingDays(siteId: number) {
    this.padelService.getSiteClosingDays(siteId).subscribe({
      next: siteDays => {
        this.loadGlobalClosingDays(siteDays);
      },
      error: error => {
        this.closedDays.set([]);

        this.snackBar.open(
          `${getHttpErrorUserMessage(error)} Les jours de fermeture du site n’ont pas pu être chargés.`,
          'OK',
          { duration: 5000 }
        );
      }
    });
  }

  private loadGlobalClosingDays(siteDays: any[]) {
    this.padelService.getGlobalClosingDays().subscribe({
      next: globalDays => {
        this.closedDays.set([
          ...siteDays,
          ...globalDays
        ]);
      },
      error: error => {
        this.closedDays.set(siteDays);

        this.snackBar.open(
          `${getHttpErrorUserMessage(error)} Les fermetures globales n’ont pas pu être chargées.`,
          'OK',
          { duration: 5000 }
        );
      }
    });
  }

  getMaxReservationDate(): Date {
    const today = this.resetDate(new Date());
    const member = this.authService.currentMember();
    const typeCode = this.getMemberTypeCode(member);

    const maxDate = new Date(today);

    if (typeCode === 'GLOBAL' || typeCode === 'G') {
      maxDate.setDate(maxDate.getDate() + 21);
      return maxDate;
    }

    if (typeCode === 'SITE' || typeCode === 'S') {
      maxDate.setDate(maxDate.getDate() + 14);
      return maxDate;
    }

    maxDate.setDate(maxDate.getDate() + 5);
    return maxDate;
  }

  getReservationRuleLabel(): string {
    const member = this.authService.currentMember();
    const typeCode = this.getMemberTypeCode(member);

    if (typeCode === 'GLOBAL' || typeCode === 'G') {
      return 'Ton abonnement permet de réserver jusqu’à 3 semaines à l’avance.';
    }

    if (typeCode === 'SITE' || typeCode === 'S') {
      return 'Ton abonnement permet de réserver jusqu’à 2 semaines à l’avance, uniquement sur ton site.';
    }

    return 'Ton abonnement permet de réserver jusqu’à 5 jours à l’avance.';
  }

  getSiteMemberRestrictionMessage(): string | null {
    const member = this.authService.currentMember();
    const typeCode = this.getMemberTypeCode(member);
    const site = this.site();

    if ((typeCode !== 'SITE' && typeCode !== 'S') || !site) {
      return null;
    }

    const memberSiteId = member?.siteId ?? member?.site?.id;

    if (!memberSiteId || Number(memberSiteId) !== Number(site.id)) {
      return 'Ton abonnement est rattaché à un autre centre. Tu ne peux réserver que sur ton site.';
    }

    return null;
  }

  canSelectCourt(court: PadelCourt): boolean {
    if (!court.active || court.maintenance) {
      return false;
    }

    return !this.getSiteMemberRestrictionMessage();
  }

  onTimeSelected(time: string) {
    this.selectedTime.set(time);
  }

  onDateSelected(date: Date) {
    if (date > this.getMaxReservationDate()) {
      this.snackBar.open(this.getReservationRuleLabel(), 'OK', {
        duration: 5000
      });
      this.selectedDate.set(null);
      this.selectedTime.set(null);
      this.reservedTimes.set([]);
      return;
    }

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
    if (this.getSiteMemberRestrictionMessage()) {
      this.snackBar.open(this.getSiteMemberRestrictionMessage() ?? '', 'OK', {
        duration: 5000
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
      error: error => {
        this.reservedTimes.set([]);

        this.snackBar.open(
          `${getHttpErrorUserMessage(error)} Les créneaux réservés n’ont pas pu être chargés.`,
          'OK',
          { duration: 5000 }
        );
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

    if (!court || !date || !time || !currentMember || !this.isMatchSelectionValid()) {
      this.snackBar.open('Veuillez compléter toutes les informations de réservation.', 'OK', {
        duration: 4000
      });
      return;
    }

    if (date > this.getMaxReservationDate()) {
      this.snackBar.open(this.getReservationRuleLabel(), 'OK', {
        duration: 5000
      });
      return;
    }

    if (this.getSiteMemberRestrictionMessage()) {
      this.snackBar.open(this.getSiteMemberRestrictionMessage() ?? '', 'OK', {
        duration: 5000
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

    const slotDateTime = this.buildSlotDateTime(date, time);

    if (slotDateTime <= new Date()) {
      this.snackBar.open('Ce créneau est déjà passé.', 'OK', {
        duration: 4000
      });
      return;
    }

    const endDateTime = new Date(slotDateTime);
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
      error: error => {
        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  private getMemberTypeCode(member: any): string {
    return (
      member?.type?.code
      ?? member?.typeCode
      ?? member?.type
      ?? member?.matricule?.substring(0, 1)
      ?? ''
    ).toString().toUpperCase();
  }

  private resetDate(date: Date): Date {
    const d = new Date(date);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  private buildSlotDateTime(date: Date, time: string): Date {
    const [hour, minute] = time.split(':').map(Number);
    const slotDateTime = new Date(date);

    slotDateTime.setHours(hour, minute, 0, 0);

    return slotDateTime;
  }
}
