import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-player-wallet-card',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './player-wallet-card.html'
})
export class PlayerWalletCard {

  amountDue = input<number>(0);

  amountPending = input<number>(0);

  amountPaid = input<number>(0);

  amountRefunded = input<number>(0);

}
