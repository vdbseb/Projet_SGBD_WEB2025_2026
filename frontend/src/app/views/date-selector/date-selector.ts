import {Component, output, signal, OnInit, inject, input} from '@angular/core';
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
  private datePipe = inject(DatePipe);


  dateChange = output<Date>();

  selectedDate = signal<Date>(this.resetTime(new Date()));

  // Liste des 21 jours
  daysList = signal<DayItem[]>([]);

  ngOnInit() {
    this.generateThreeWeeks();
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

  /**
   * Action au clic sur un jour
   */
  selectDate(date: Date) {
    const cleanDate = this.resetTime(date);
    this.selectedDate.set(cleanDate);
    this.dateChange.emit(cleanDate);
  }

  /**
   * Vérifie si la date est celle sélectionnée (pour le style CSS)
   */
  isSelected(date: Date): boolean {
    return date.getTime() === this.selectedDate().getTime();
  }

  /**
   * Helper : Réinitialise l'heure à minuit pour comparer uniquement les jours
   */
  private resetTime(date: Date): Date {
    const d = new Date(date);
    d.setHours(0, 0, 0, 0);
    return d;
  }

  /**
   * Helper : Formatage rapide via DatePipe
   */
  private formatDate(date: Date, format: string): string {
    const res = this.datePipe.transform(date, format) || '';
    return res.charAt(0).toUpperCase() + res.slice(1).replace('.', '');
  }
}
