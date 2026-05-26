import { Component, input, output, signal, OnInit } from '@angular/core';

@Component({
  selector: 'app-time-slots',
  standalone: true,
  templateUrl: './time-slot.html'
})
export class TimeSlotsComponent implements OnInit {
  slotSelected = output<string>();

  reservedTimes = input<string[]>([]);
  openingTime = input<string>('08:00:00');
  closingTime = input<string>('22:00:00');

  availableSlots = signal<string[]>([]);
  selectedSlot = signal<string | null>(null);

  private readonly matchDurationMinutes = 90;
  private readonly pauseMinutes = 15;

  ngOnInit() {
    this.generateSlots();
  }

  generateSlots() {
    const slots: string[] = [];

    const [openHour, openMinute] = this.openingTime().split(':').map(Number);
    const [closeHour, closeMinute] = this.closingTime().split(':').map(Number);

    const current = new Date();
    current.setHours(openHour, openMinute, 0, 0);

    const closing = new Date();
    closing.setHours(closeHour, closeMinute, 0, 0);

    while (true) {
      const end = new Date(current);
      end.setMinutes(end.getMinutes() + this.matchDurationMinutes);

      if (end > closing) {
        break;
      }

      slots.push(this.formatTime(current));

      current.setMinutes(
        current.getMinutes() +
        this.matchDurationMinutes +
        this.pauseMinutes
      );
    }

    this.availableSlots.set(slots);
  }

  selectSlot(slot: string) {
    if (this.isReserved(slot)) {
      return;
    }

    this.selectedSlot.set(slot);
    this.slotSelected.emit(slot);
  }

  isReserved(slot: string): boolean {
    return this.reservedTimes().includes(`${slot}:00`);
  }

  isSelected(slot: string): boolean {
    return this.selectedSlot() === slot;
  }

  private formatTime(date: Date): string {
    return date.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
