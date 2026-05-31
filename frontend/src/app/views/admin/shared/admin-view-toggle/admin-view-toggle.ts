import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

export type AdminViewMode = 'cards' | 'table';

@Component({
  selector: 'app-admin-view-toggle',
  standalone: true,
  imports: [MatIconModule],
  template: `
    <button
      type="button"
      (click)="modeChange.emit('cards')"
      class="inline-flex items-center gap-2 px-4 py-3 rounded-2xl font-black uppercase text-xs tracking-widest transition-all"
      [class]="mode === 'cards'
        ? 'bg-slate-900 text-white'
        : 'bg-white border border-slate-200 text-slate-500 hover:bg-slate-100'"
    >
      <mat-icon class="text-base">dashboard</mat-icon>
      Cartes
    </button>

    <button
      type="button"
      (click)="modeChange.emit('table')"
      class="inline-flex items-center gap-2 px-4 py-3 rounded-2xl font-black uppercase text-xs tracking-widest transition-all"
      [class]="mode === 'table'
        ? 'bg-slate-900 text-white'
        : 'bg-white border border-slate-200 text-slate-500 hover:bg-slate-100'"
    >
      <mat-icon class="text-base">table_rows</mat-icon>
      Tableau
    </button>
  `
})
export class AdminViewToggleComponent {
  @Input({ required: true }) mode: AdminViewMode = 'cards';
  @Output() modeChange = new EventEmitter<AdminViewMode>();
}
