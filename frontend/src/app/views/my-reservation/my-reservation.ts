import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-my-reservation',
  standalone: true,
  imports: [DatePipe, RouterLink],
  templateUrl: './my-reservation.html'
})
export class MyReservations implements OnInit {
  private padelService = inject(PadelService);
  authService = inject(AuthService);

  reservations = signal<any[]>([]);

  ngOnInit() {
    const member = this.authService.currentMember();

    if (!member) {
      this.reservations.set([]);
      return;
    }

    this.padelService.getAllReservations().subscribe(reservations => {
      const filteredReservations = reservations
        .filter(reservation => reservation.memberId === member.id)
        .sort((a, b) => {
          const dateA = new Date(`${a.date}T${a.startTime}`).getTime();
          const dateB = new Date(`${b.date}T${b.startTime}`).getTime();

          return dateA - dateB;
        });

      this.reservations.set(filteredReservations);
    });
  }
}
