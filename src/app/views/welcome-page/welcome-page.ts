import { Component, inject} from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { PadelService } from '../../services/padel.service';
import {Router} from '@angular/router';
import {PadelCardComponent} from '../padel-card/padel-card';
import {AsyncPipe} from '@angular/common';
import {MatDialog} from '@angular/material/dialog';
import {AuthService} from '../../services/auth.service';
import {LoginDialogComponent} from '../login-dialog/login-dialog';


@Component({
  selector: 'app-welcome-page',
  standalone: true,
  imports: [
    MatCardModule,
    MatButtonModule,
    PadelCardComponent,
    AsyncPipe
  ],
  templateUrl: './welcome-page.html',
  styleUrl: './welcome-page.css',

})
export class WelcomePage {

  private padelService = inject(PadelService);
  private router = inject(Router);
  private dialog = inject(MatDialog);
  private authService = inject(AuthService);

  sites$ = this.padelService.getSites();

  goToReservation(siteId: string) {
    if (this.authService.isLoggedIn()) {
      this.router.navigate(['/reserver', siteId]);
      return;
    }

    const dialogRef = this.dialog.open(LoginDialogComponent);

    dialogRef.afterClosed().subscribe(member => {
      if (member) {
        this.router.navigate(['/reserver', siteId]);
      }
    });
  }
}
