import { Component, inject, signal, OnInit } from '@angular/core';
import { DatePipe } from '@angular/common';
import { PadelService } from '../../services/padel.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-my-reservation',
  standalone: true,
  imports: [DatePipe],
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
      this.reservations.set(
        reservations.filter(reservation =>
          reservation.memberId === member.id
        )
      );
    });
  }
}
