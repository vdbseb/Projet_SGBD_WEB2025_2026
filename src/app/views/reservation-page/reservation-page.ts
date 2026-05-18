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

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.padelService.getSiteById(id).subscribe(site => {
        this.site.set(site);
      });
    }
  }

  onTimeSelected(time: string) {
    console.log('Heure sélectionnée :', time);
    this.selectedTime.set(time);
  }

  onDateSelected(date: Date) {
    console.log('Nouvelle date sélectionnée :', date);
    this.selectedDate.set(date);
    this.selectedTime.set(null);
  }

  selectCourt(court: PadelCourt) {
    this.selectedCourt.set(court);
    this.selectedTime.set(null);
  }
  onConfirmBooking() {
    const court = this.selectedCourt();
    const date = this.selectedDate();
    const time = this.selectedTime();

    if (court && date && time) {
      const startTime = `${time}:00`;

      const [hour, minute] = time.split(':').map(Number);
      const endTime = `${(hour + 1).toString().padStart(2, '0')}:${minute
        .toString()
        .padStart(2, '0')}:00`;

      const newReservation = {
        id: null,
        courtId: court.id,
        courtName: null,
        memberId: '3e15764c-7a0f-47b8-a192-26dd77f917ba', //hardcodé !
        playerMatricule: null,
        date: date.toISOString().split('T')[0],
        startTime: startTime,
        endTime: endTime
      };

      this.padelService.createReservation(newReservation).subscribe({
        next: () => {
          console.log('Réservation enregistrée avec succès');
          alert('Réservation confirmée !');
          this.selectedTime.set(null);
        },
        error: (error) => {
          console.error('Erreur lors de l\'enregistrement de la réservation :', error);
          alert('Erreur lors de la réservation. Veuillez réessayer.');
        }
      });
    }
  }
}
