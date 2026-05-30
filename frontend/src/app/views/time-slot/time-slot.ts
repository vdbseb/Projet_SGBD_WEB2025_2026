import { Component, input, output, signal, OnInit, OnChanges, SimpleChanges } from '@angular/core';

@Component({
  selector: 'app-time-slots',
  standalone: true,
  templateUrl: './time-slot.html'
})
export class TimeSlotsComponent implements OnInit, OnChanges {
  slotSelected = output<string>();

  reservedTimes = input<string[]>([]);
  openingTime = input<string>('08:00:00');
  closingTime = input<string>('22:00:00');
  selectedDate = input<Date | null>(null);

  matchDurationMinutes = input<number>(90);
  pauseMinutes = input<number>(15);

  availableSlots = signal<string[]>([]);
  selectedSlot = signal<string | null>(null);

  ngOnInit() {
    this.generateSlots();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (
      changes['openingTime']
      || changes['closingTime']
      || changes['matchDurationMinutes']
      || changes['pauseMinutes']
    ) {
      this.generateSlots();
    }

    if (changes['selectedDate']) {
      this.selectedSlot.set(null);
    }
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
      end.setMinutes(end.getMinutes() + this.matchDurationMinutes());

      if (end > closing) {
        break;
      }

      slots.push(this.formatTime(current));

      current.setMinutes(
        current.getMinutes()
        + this.matchDurationMinutes()
        + this.pauseMinutes()
      );
    }

    this.availableSlots.set(slots);
  }

  selectSlot(slot: string) {
    if (this.isSlotDisabled(slot)) {
      return;
    }

    this.selectedSlot.set(slot);
    this.slotSelected.emit(slot);
  }

  isReserved(slot: string): boolean {
    return this.reservedTimes().includes(`${slot}:00`);
  }

  isPastSlot(slot: string): boolean {
    const selectedDate = this.selectedDate();

    if (!selectedDate) {
      return false;
    }

    const slotDateTime = this.buildSlotDateTime(selectedDate, slot);
    const now = new Date();

    return slotDateTime <= now;
  }

  isSlotDisabled(slot: string): boolean {
    return this.isReserved(slot) || this.isPastSlot(slot);
  }

  isSelected(slot: string): boolean {
    return this.selectedSlot() === slot;
  }

  getSlotLabel(slot: string): string {
    if (this.isReserved(slot)) {
      return 'Réservé';
    }

    if (this.isPastSlot(slot)) {
      return 'Passé';
    }

    return '';
  }

  private buildSlotDateTime(date: Date, slot: string): Date {
    const [hour, minute] = slot.split(':').map(Number);
    const slotDate = new Date(date);

    slotDate.setHours(hour, minute, 0, 0);

    return slotDate;
  }

  private formatTime(date: Date): string {
    return date.toLocaleTimeString('fr-FR', {
      hour: '2-digit',
      minute: '2-digit'
    });
  }
}
