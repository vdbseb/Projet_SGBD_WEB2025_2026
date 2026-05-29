import { Component, input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-player-wallet-card',
  standalone: true,
  imports: [MatIconModule],
  templateUrl: './player-wallet-card.html'
})
export class PlayerWalletCard {

  balance = input<number>(0);

  credit = input<number>(0);

  amountDue = input<number>(0);

  amountRefunded = input<number>(0);

}
