import { Component, output, model } from '@angular/core'; // Ajoute 'model' ici
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


  selectedTime = model<Date | null>(null);


  onTimeChange(event: any) {
    // Le Timepicker peut renvoyer soit l'objet event, soit event.value
    const date = event?.value !== undefined ? event.value : event;

    if (date instanceof Date) {
      const formattedTime = date.toLocaleTimeString('fr-FR', {
        hour: '2-digit',
        minute: '2-digit'
      });

      this.slotSelected.emit(formattedTime);
    }
  }
}
