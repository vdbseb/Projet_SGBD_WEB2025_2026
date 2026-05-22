import {Component, output, model, input} from '@angular/core';
import { MatTimepickerModule } from '@angular/material/timepicker';
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { FormsModule } from '@angular/forms';
import { provideNativeDateAdapter } from '@angular/material/core';

@Component({
  selector: 'app-time-slots',
  standalone: true,
  providers: [provideNativeDateAdapter()],
  imports: [
    MatFormFieldModule,
    MatInputModule,
    MatTimepickerModule,
    FormsModule
  ],
  templateUrl: './time-slot.html'
})
export class TimeSlotsComponent {
  slotSelected = output<string>();
  reservedTimes = input<string[]>([]);
  errorMessage = '';


  selectedTime = model<Date | null>(null);


  onTimeChange(event: any) {
    const date = event?.value !== undefined ? event.value : event;

    if (date instanceof Date) {
      const formattedTime = date.toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      });

      const formattedWithSeconds = `${formattedTime}:00`;

      if (this.reservedTimes().includes(formattedWithSeconds)) {
        this.errorMessage = 'Ce créneau est déjà réservé.';
        this.selectedTime.set(null);
        return;
      }

      this.errorMessage = '';
      this.slotSelected.emit(formattedTime);
    }
  }
}
