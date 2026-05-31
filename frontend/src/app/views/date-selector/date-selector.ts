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
  closedDays = input<any[]>([]);

  tomorrowDayNumber = new Date(new Date().setDate(new Date().getDate() + 1)).getDate();

  dateChange = output<Date>();

  private datePipe = inject(DatePipe);

  selectedDate = signal<Date | null>(this.resetTime(new Date()));
  daysList = signal<DayItem[]>([]);

  ngOnInit() {
    this.generateThreeWeeks();

    const initial = this.initialDate();

    if (initial && !this.isDateDisabled(initial)) {
      this.selectedDate.set(this.resetTime(initial));
      return;
    }

    if (this.selectedDate() && this.isDateDisabled(this.selectedDate() as Date)) {
      this.selectedDate.set(null);
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
    const selected = this.selectedDate();

    if (!selected) {
      return false;
    }

    return date.getTime() === selected.getTime();
  }

  isDateDisabled(date: Date): boolean {
    const cleanDate = this.resetTime(date);
    const today = this.resetTime(new Date());

    if (cleanDate < today) {
      return true;
    }

    if (this.isClosedDate(cleanDate)) {
      return true;
    }

    const max = this.maxReservationDate();

    if (!max) {
      return false;
    }

    return cleanDate > this.resetTime(max);
  }

  isClosedDate(date: Date): boolean {
    const formattedDate = this.formatLocalDate(date);

    return this.closedDays().some(day =>
      day.dateFermeture === formattedDate
    );
  }

  getDisabledReason(date: Date): string {
    const cleanDate = this.resetTime(date);
    const max = this.maxReservationDate();

    if (this.isClosedDate(cleanDate)) {
      const closingDay = this.getClosingDay(cleanDate);

      if (closingDay?.global) {
        return 'Fermeture globale';
      }

      return 'Fermé';
    }

    if (max && cleanDate > this.resetTime(max)) {
      return 'Hors délai';
    }

    if (cleanDate < this.resetTime(new Date())) {
      return 'Passé';
    }

    return 'Indisponible';
  }

  getDisabledTitle(date: Date): string {
    const cleanDate = this.resetTime(date);
    const closingDay = this.getClosingDay(cleanDate);

    if (closingDay) {
      const scope = closingDay.global
        ? 'Fermeture globale'
        : 'Fermeture du site';

      return closingDay.raison
        ? `${scope} : ${closingDay.raison}`
        : scope;
    }

    return this.getDisabledReason(date);
  }

  private getClosingDay(date: Date): any | null {
    const formattedDate = this.formatLocalDate(date);

    return this.closedDays().find(day =>
      day.dateFermeture === formattedDate
    ) ?? null;
  }

  private formatLocalDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
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
