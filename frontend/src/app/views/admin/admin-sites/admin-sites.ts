import { Component, inject, OnInit, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';

import { PadelService } from '../../../services/padel.service';
import { AuthService } from '../../../services/auth.service';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import { getHttpErrorUserMessage } from '../../../shared/api-error.util';
import { AdminPageShellComponent } from '../shared/admin-page-shell/admin-page-shell';

@Component({
  selector: 'app-admin-sites',
  standalone: true,
  imports: [
    CommonModule,
    MatIconModule,
    FormsModule,
    MatSnackBarModule,
    AdminPageShellComponent
  ],
  templateUrl: './admin-sites.html'
})
export class AdminSites implements OnInit {
  private padelService = inject(PadelService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  authService = inject(AuthService);

  today = new Date().toISOString().split('T')[0];

  sites = signal<any[]>([]);
  reservations = signal<any[]>([]);

  search = signal('');
  selectedStatus = signal<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  selectedCity = signal<string>('ALL');

  addingClosure = signal<any | null>(null);
  editingSchedule = signal<any | null>(null);
  originalSchedule = signal<any | null>(null);

  closureForm = signal({
    siteId: null as number | null,
    dateFermeture: '',
    raison: '',
    global: false,
    recurrence: 'ONCE',
    repeatUntil: ''
  });

  scheduleForm = signal({
    id: null as number | null,
    siteId: null as number | null,
    annee: new Date().getFullYear(),
    heure_debut: '',
    heure_fin: '',
    duree_match_minutes: 90,
    pause_minutes: 15
  });

  ngOnInit() {
    this.loadSites();
    this.loadReservations();
  }

  loadSites() {
    this.padelService.getSites().subscribe({
      next: sites => {
        this.sites.set(sites);
      },
      error: error => {
        this.sites.set([]);

        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  loadReservations() {
    this.padelService.getAllReservations().subscribe({
      next: reservations => {
        this.reservations.set(reservations);
      },
      error: error => {
        this.reservations.set([]);

        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  visibleSites() {
    const admin = this.authService.currentAdmin();

    if (this.authService.isSiteAdmin()) {
      return this.sites().filter(site => site.id === admin.siteId);
    }

    return this.sites();
  }

  filteredSites() {
    const query = this.search().toLowerCase().trim();
    const selectedStatus = this.selectedStatus();
    const selectedCity = this.selectedCity();

    return this.visibleSites().filter(site => {
      const matchesSearch =
        !query ||
        site.clubName?.toLowerCase().includes(query) ||
        site.name?.toLowerCase().includes(query) ||
        site.city?.toLowerCase().includes(query) ||
        site.adresse?.toLowerCase().includes(query);

      const matchesStatus =
        selectedStatus === 'ALL' ||
        (selectedStatus === 'ACTIVE' && site.active !== false) ||
        (selectedStatus === 'INACTIVE' && site.active === false);

      const matchesCity =
        selectedCity === 'ALL' ||
        site.city === selectedCity;

      return matchesSearch && matchesStatus && matchesCity;
    });
  }

  cities() {
    return [...new Set(
      this.visibleSites()
        .map(site => site.city)
        .filter(Boolean)
    )];
  }

  resetFilters() {
    this.search.set('');
    this.selectedStatus.set('ALL');
    this.selectedCity.set('ALL');
  }

  toggleSiteActive(site: any) {
    const isActive = site.active !== false;
    const nextActive = !isActive;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: isActive ? 'Désactiver le site' : 'Réactiver le site',
        message: isActive
          ? `Désactiver ${site.clubName || site.name} ? Les réservations futures ne devraient plus être possibles sur ce site.`
          : `Réactiver ${site.clubName || site.name} ? Le site pourra à nouveau être utilisé.`,
        confirmLabel: isActive ? 'Désactiver' : 'Réactiver',
        cancelLabel: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      if (nextActive) {
        this.reactivateSite(site.id);
        return;
      }

      this.deactivateSite(site.id);
    });
  }

  private buildSiteUpdatePayload(site: any, active: boolean) {
    return {
      ...site,
      active,
      name: site.name || site.clubName,
      clubName: site.clubName || site.name,
      city: site.city,
      adresse: site.adresse,
      codePostal: site.codePostal,
      openingTime: site.openingTime || site.heureOuverture,
      closingTime: site.closingTime || site.heureFermeture,
      heureOuverture: site.heureOuverture || site.openingTime,
      heureFermeture: site.heureFermeture || site.closingTime
    };
  }

  private reactivateSite(siteId: number) {
    this.padelService.reactivateSite(siteId).subscribe({
      next: () => {
        this.snackBar.open('Site réactivé avec succès.', 'OK', {
          duration: 3000
        });

        this.loadSites();
      },
      error: error => {
        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  private deactivateSite(siteId: number) {
    this.padelService.deactivateSite(siteId).subscribe({
      next: () => {
        this.snackBar.open('Site désactivé avec succès.', 'OK', {
          duration: 3000
        });

        this.loadSites();
      },
      error: error => {
        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  totalSites() {
    return this.visibleSites().length;
  }

  activeSites() {
    return this.visibleSites().filter(site => site.active !== false).length;
  }

  inactiveSites() {
    return this.visibleSites().filter(site => site.active === false).length;
  }

  totalCourts() {
    return this.visibleSites().reduce(
      (total, site) => total + this.getCourtCount(site),
      0
    );
  }

  totalReservations() {
    return this.visibleSites().reduce(
      (total, site) => total + this.getSiteReservationCount(site),
      0
    );
  }

  totalRevenue() {
    return this.visibleSites().reduce(
      (total, site) => total + this.getSiteRevenue(site),
      0
    );
  }

  getCourtCount(site: any): number {
    return site.courts?.length || 0;
  }

  getIndoorCount(site: any): number {
    return site.courts?.filter((court: any) =>
      this.getCourtType(court).toLowerCase() === 'indoor'
    ).length || 0;
  }

  getOutdoorCount(site: any): number {
    return site.courts?.filter((court: any) =>
      this.getCourtType(court).toLowerCase() === 'outdoor'
    ).length || 0;
  }

  getCourtType(court: any): string {
    return court.type ?? (court.indoor ? 'Indoor' : 'Outdoor');
  }

  getSiteReservationCount(site: any): number {
    const courtIds = site.courts?.map((court: any) => court.id) || [];

    return this.reservations().filter(reservation =>
      courtIds.includes(reservation.courtId)
    ).length;
  }

  getSiteRevenue(site: any): number {
    return this.getSiteReservationCount(site) * 60;
  }

  getSiteStatusLabel(site: any): string {
    return site.active !== false ? 'Actif' : 'Inactif';
  }

  getSiteStatusClass(site: any): string {
    return site.active !== false
      ? 'bg-emerald-100 text-emerald-700'
      : 'bg-red-100 text-red-700';
  }

  openScheduleEditor(site: any) {
    const year = new Date().getFullYear();

    this.padelService.getSiteSchedule(site.id, year).subscribe({
      next: schedule => {
        this.editingSchedule.set(site);

        this.originalSchedule.set({
          heure_debut: schedule.heure_debut,
          heure_fin: schedule.heure_fin
        });

        this.scheduleForm.set({
          id: schedule.id,
          siteId: schedule.siteId,
          annee: schedule.annee,
          heure_debut: schedule.heure_debut,
          heure_fin: schedule.heure_fin,
          duree_match_minutes: schedule.duree_match_minutes,
          pause_minutes: schedule.pause_minutes
        });
      },
      error: error => {
        this.snackBar.open(
          getHttpErrorUserMessage(error),
          'OK',
          { duration: 5000 }
        );
      }
    });
  }

  hasScheduleChanged(): boolean {
    const original = this.originalSchedule();
    const current = this.scheduleForm();

    if (!original) {
      return false;
    }

    const originalStart = original.heure_debut?.substring(0, 5);
    const originalEnd = original.heure_fin?.substring(0, 5);

    const currentStart = current.heure_debut?.substring(0, 5);
    const currentEnd = current.heure_fin?.substring(0, 5);

    return (
      originalStart !== currentStart ||
      originalEnd !== currentEnd
    );
  }

  closeScheduleEditor() {
    this.editingSchedule.set(null);
    this.originalSchedule.set(null);
  }

  saveSchedule() {
    const form = this.scheduleForm();

    if (!form.id) {
      return;
    }

    this.padelService.updateSiteSchedule(form.id, form).subscribe({
      next: () => {
        this.snackBar.open('Horaires modifiés avec succès.', 'OK', {
          duration: 3000
        });

        this.closeScheduleEditor();
        this.loadSites();
      },
      error: error => {
        this.snackBar.open(
          getHttpErrorUserMessage(error),
          'OK',
          { duration: 5000 }
        );
      }
    });
  }

  openClosureEditor(site: any) {
    this.addingClosure.set(site);

    this.closureForm.set({
      siteId: site.id,
      dateFermeture: '',
      raison: '',
      global: false,
      recurrence: 'ONCE',
      repeatUntil: ''
    });
  }

  closeClosureEditor() {
    this.addingClosure.set(null);

    this.closureForm.set({
      siteId: null,
      dateFermeture: '',
      raison: '',
      global: false,
      recurrence: 'ONCE',
      repeatUntil: ''
    });
  }

  onClosureGlobalChange(isGlobal: boolean) {
    if (isGlobal && !this.authService.isGlobalAdmin()) {
      this.snackBar.open(
        'Seul un administrateur global peut créer une fermeture globale.',
        'OK',
        { duration: 4000 }
      );

      this.closureForm.set({
        ...this.closureForm(),
        global: false
      });

      return;
    }

    this.closureForm.set({
      ...this.closureForm(),
      global: isGlobal
    });
  }

  isClosureFormValid(): boolean {
    const form = this.closureForm();

    if (!form.dateFermeture || !form.raison.trim()) {
      return false;
    }

    if (form.recurrence !== 'ONCE' && !form.repeatUntil) {
      return false;
    }

    if (form.recurrence !== 'ONCE' && form.repeatUntil < form.dateFermeture) {
      return false;
    }

    if (form.global) {
      return this.authService.isGlobalAdmin();
    }

    return !!form.siteId;
  }

  generateClosureDates(
    startDate: string,
    recurrence: string,
    repeatUntil: string
  ): string[] {
    const dates: string[] = [];
    const current = this.parseLocalDate(startDate);
    const end = repeatUntil
      ? this.parseLocalDate(repeatUntil)
      : this.parseLocalDate(startDate);

    while (current <= end) {
      dates.push(this.formatLocalDate(current));

      if (recurrence === 'ONCE') {
        break;
      }

      if (recurrence === 'WEEKLY') {
        current.setDate(current.getDate() + 7);
      } else if (recurrence === 'MONTHLY') {
        current.setMonth(current.getMonth() + 1);
      } else if (recurrence === 'YEARLY') {
        current.setFullYear(current.getFullYear() + 1);
      } else {
        break;
      }
    }

    return dates;
  }

  saveClosure() {
    const form = this.closureForm();

    if (!this.isClosureFormValid()) {
      this.snackBar.open(
        'Complète la date, la raison et la portée de la fermeture.',
        'OK',
        { duration: 3000 }
      );

      return;
    }

    const dates = this.generateClosureDates(
      form.dateFermeture,
      form.recurrence,
      form.repeatUntil
    );

    if (dates.length === 0) {
      this.snackBar.open('Aucune date de fermeture à créer.', 'OK', {
        duration: 3000
      });

      return;
    }

    const requests = dates.map(date =>
      this.padelService.createSiteClosingDay({
        siteId: form.global ? null : form.siteId,
        dateFermeture: date,
        raison: form.raison.trim(),
        global: form.global
      })
    );

    forkJoin(requests).subscribe({
      next: () => {
        this.snackBar.open(
          form.global
            ? 'Fermeture globale ajoutée avec succès.'
            : 'Jour(s) de fermeture ajouté(s) pour le site.',
          'OK',
          { duration: 3000 }
        );

        this.closeClosureEditor();
        this.loadSites();
      },
      error: error => {
        this.snackBar.open(
          getHttpErrorUserMessage(error),
          'OK',
          { duration: 5000 }
        );
      }
    });
  }

  private parseLocalDate(value: string): Date {
    const [year, month, day] = value.split('-').map(Number);

    return new Date(year, month - 1, day);
  }

  private formatLocalDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
  }
}
