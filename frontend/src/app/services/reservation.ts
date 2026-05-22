import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Reservation {
  id?: string;
  courtName: string;
  date: string;
  timeSlot: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReservationService {
  private http = inject(HttpClient);
  private readonly API_URL = 'http://localhost:8080/reservations';

  saveReservation(reservation: {
    courtName: string;
    date: Date;
    timeSlot: string;
    playerName: string
  }): Observable<Reservation> {
    return this.http.post<Reservation>(this.API_URL, reservation);
  }

  getReservations(): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(this.API_URL);
  }
}
