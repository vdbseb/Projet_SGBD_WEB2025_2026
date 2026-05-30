import { Component, output, signal, OnInit, inject, input } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';

interface DayItem {
  date: Date;
  dayName: string;
  dayNumber: string;
  monthName: string;
}

@Component({
  selector: 'app-date-selector',
  standalone: true,
  imports: [CommonModule],
  providers: [DatePipe],
  templateUrl: './date-selector.html'
})
export class DateSelectorComponent implements OnInit {
  courtName = input<string>('');
  initialDate = input<Date | null>(null);
  maxReservationDate = input<Date | null>(null);
  tomorrowDayNumber = new Date(new Date().setDate(new Date().getDate() + 1)).getDate();

  dateChange = output<Date>();

  private datePipe = inject(DatePipe);

  selectedDate = signal<Date>(this.resetTime(new Date()));
  daysList = signal<DayItem[]>([]);

  ngOnInit() {
    this.generateThreeWeeks();

    const initial = this.initialDate();

    if (initial && !this.isDateDisabled(initial)) {
      this.selectedDate.set(this.resetTime(initial));
    }
  }

  generateThreeWeeks() {
    const days: DayItem[] = [];
    const today = this.resetTime(new Date());

    for (let i = 0; i < 22; i++) {
      const current = new Date(today);
      current.setDate(today.getDate() + i);

      days.push({
        date: current,
        dayName: this.formatDate(current, 'EEE'),
        dayNumber: this.formatDate(current, 'd'),
        monthName: this.formatDate(current, 'MMM').toUpperCase()
      });
    }

    this.daysList.set(days);
  }

  selectDate(date: Date) {
    if (this.isDateDisabled(date)) {
      return;
    }

    const cleanDate = this.resetTime(date);
    this.selectedDate.set(cleanDate);
    this.dateChange.emit(cleanDate);
  }

  isSelected(date: Date): boolean {
    return date.getTime() === this.selectedDate().getTime();
  }

  isDateDisabled(date: Date): boolean {
    const cleanDate = this.resetTime(date);
    const today = this.resetTime(new Date());

    if (cleanDate < today) {
      return true;
    }

    const max = this.maxReservationDate();

    if (!max) {
      return false;
    }

    return cleanDate > this.resetTime(max);
  }

  getDisabledReason(date: Date): string {
    if (!this.isDateDisabled(date)) {
      return '';
    }

    const max = this.maxReservationDate();

    if (max && this.resetTime(date) > this.resetTime(max)) {
      return 'Hors délai';
    }

    return 'Indisponible';
  }

  private resetTime(date: Date): Date {
    const d = new Date(date);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  private formatDate(date: Date, format: string): string {
    const res = this.datePipe.transform(date, format) || '';
    return res.charAt(0).toUpperCase() + res.slice(1).replace('.', '');
  }
}
