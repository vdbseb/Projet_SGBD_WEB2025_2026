import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { PadelService } from '../../services/padel.service';
import { PadelSite, PadelCourt } from '../../shared/site.model';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import {PadelCardComponent} from '../padel-card/padel-card';
import {DateSelectorComponent} from '../date-selector/date-selector';
import {TimeSlotsComponent} from '../time-slot/time-slot';
import {DatePipe} from '@angular/common';
import { MatSnackBar} from '@angular/material/snack-bar';

@Component({
  selector: 'app-reservation-page',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    MatDatepickerModule,
    MatNativeDateModule,
    PadelCardComponent,
    DateSelectorComponent,
    TimeSlotsComponent,
    DatePipe
  ],
  templateUrl: './reservation-page.html'
})
export class ReservationPage implements OnInit {
  private route = inject(ActivatedRoute);
  private padelService = inject(PadelService);

  site = signal<PadelSite | undefined>(undefined);
  selectedCourt = signal<PadelCourt | undefined>(undefined);
  selectedDate = signal<Date | null>(new Date());
  selectedTime = signal<string | null>(null);
  members = signal<any[]>([]);
  selectedMemberId = signal<string | null>(null);
  reservedTimes = signal<string[]>([]);
  private snackBar = inject(MatSnackBar);


  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.padelService.getSiteById(id).subscribe(site => {
        this.site.set(site);
      });
    }
    this.padelService.getMembers().subscribe(members => {
      this.members.set(members);
    });
  }
  onMemberSelected(memberId: string) {
    this.selectedMemberId.set(memberId);
  }

  onTimeSelected(time: string) {
    console.log('Heure sélectionnée :', time);
    this.selectedTime.set(time);
  }

  onDateSelected(date: Date) {
    console.log('Nouvelle date sélectionnée :', date);
    this.selectedDate.set(date);
    this.selectedTime.set(null);
    this.loadReservedTimes();
  }

  selectCourt(court: PadelCourt) {
    this.selectedCourt.set(court);
    this.selectedTime.set(null);
    this.loadReservedTimes();
  }
  loadReservedTimes() {
    const court = this.selectedCourt();
    const date = this.selectedDate();

    if (!court || !date) {
      this.reservedTimes.set([]);
      return;
    }

    const formattedDate = date.toISOString().split('T')[0];

    this.padelService.getReservations(court.id, formattedDate).subscribe(reservations => {
      this.reservedTimes.set(reservations.map(r => r.startTime));
    });
  }
  onConfirmBooking() {
    const court = this.selectedCourt();
    const date = this.selectedDate();
    const time = this.selectedTime();

    if (court && date && time && this.selectedMemberId()) {
      const startTime = `${time}:00`;

      const [hour, minute] = time.split(':').map(Number);
      const endTime = `${(hour + 1).toString().padStart(2, '0')}:${minute
        .toString()
        .padStart(2, '0')}:00`;

      const newReservation = {
        id: null,
        courtId: court.id,
        courtName: null,
        memberId: this.selectedMemberId(),
        playerMatricule: null,
        date: date.toISOString().split('T')[0],
        startTime: startTime,
        endTime: endTime
      };

      this.padelService.createReservation(newReservation).subscribe({
        next: () => {
          this.snackBar.open('Réservation confirmée !', 'OK', {
            duration: 3000
          });

          this.selectedTime.set(null);
        },
        error: (error) => {
          console.error(error);

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
}
