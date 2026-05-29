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

  ngOnInit() {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = idParam ? Number(idParam) : NaN;

    if (Number.isFinite(id)) {
      this.padelService.getSiteById(id).subscribe(site => {
        this.site.set(site);

        const year = new Date().getFullYear();

        this.padelService.getSiteSchedule(site.id, year).subscribe(schedule => {
          this.matchDurationMinutes.set(schedule.duree_match_minutes ?? 90);
          this.pauseMinutes.set(schedule.pause_minutes ?? 15);
        });

        this.loadClosingDays(site.id);
      });
    }
  }

  private loadClosingDays(siteId: number) {
    this.padelService.getSiteClosingDays(siteId).subscribe(siteDays => {
      this.padelService.getGlobalClosingDays().subscribe(globalDays => {
        this.closedDays.set([
          ...siteDays,
          ...globalDays
        ]);
      });
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
    if (!court.active) {
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

    this.padelService.getReservations(court.id, formattedDate).subscribe(reservations => {
      this.reservedTimes.set(
        reservations
          .filter(reservation => reservation.reservationStatus !== 'ANNULEE')
          .filter(reservation => reservation.matchStatus !== 'ANNULE')
          .map(reservation => reservation.startTime)
      );
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
      error: error => {
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
