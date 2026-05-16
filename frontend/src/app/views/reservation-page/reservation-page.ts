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
import {ReservationService} from '../../services/reservation';

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
  private reservationService = inject(ReservationService);

  site = signal<PadelSite | undefined>(undefined);
  selectedCourt = signal<PadelCourt | undefined>(undefined);
  selectedDate = signal<Date | null>(new Date());
  selectedTime = signal<string | null>(null);

  ngOnInit() {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.site.set(this.padelService.getSiteById(id));
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
  onConfirmBooking(){
    const court = this.selectedCourt();
    const date = this.selectedDate();
    const time= this.selectedTime();

    if (court && date && time){
      const newReservation = {
        courtName: court.name,
        date: date,
        timeSlot: time,
        playerName: 'TEST'
      };
      this.reservationService.saveReservation(newReservation).subscribe({
        next: (reservation) => {
          console.log('Réservation enregistrée avec succès :', reservation);
          alert('Réservation confirmée !');
        },
        error: (error) => {
          console.error('Erreur lors de l\'enregistrement de la réservation :', error);
          alert('Erreur lors de la réservation. Veuillez réessayer.');
        }
      });
    }
  }
}
