import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-admin-page-shell',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  template: `
    <div class="min-h-screen bg-slate-50 p-8 md:p-12">

      <a
        [routerLink]="backLink"
        class="inline-flex items-center gap-2 mb-8 text-slate-500 hover:text-slate-900 font-bold uppercase text-xs tracking-widest no-underline"
      >
        <mat-icon>arrow_back</mat-icon>
        {{ backLabel }}
      </a>

      <header class="mb-10">
        <div class="flex flex-col xl:flex-row xl:items-end xl:justify-between gap-5">
          <div>
            <h1 class="text-4xl font-black uppercase tracking-tighter text-slate-900">
              {{ title }}
            </h1>

            @if (description) {
              <p class="text-slate-500 mt-2">
                {{ description }}
              </p>
            }
          </div>

          <div class="flex flex-wrap gap-3">
            <ng-content select="[admin-actions]" />
          </div>
        </div>
      </header>

      <ng-content />

    </div>
  `
})
export class AdminPageShellComponent {
  @Input({ required: true }) title = '';
  @Input() description = '';
  @Input() backLink = '/admin';
  @Input() backLabel = 'Retour admin';
}
