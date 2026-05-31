import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PadelService } from '../../../services/padel.service';
import { AuthService } from '../../../services/auth.service';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import { getHttpErrorUserMessage } from '../../../shared/api-error.util';

@Component({
  selector: 'app-admin-sites',
  standalone: true,
  imports: [
    RouterLink,
    MatIconModule,
    FormsModule,
    MatSnackBarModule
  ],
  templateUrl: './admin-sites.html'
})
export class AdminSites implements OnInit {
  private padelService = inject(PadelService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  authService = inject(AuthService);

  sites = signal<any[]>([]);
  reservations = signal<any[]>([]);

  search = signal('');
  selectedStatus = signal<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  selectedCity = signal<string>('ALL');
  addingClosure = signal<any | null>(null);

  closureForm = signal({
    siteId: null as number | null,
    dateFermeture: '',
    raison: '',
    global: false
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
        (selectedStatus === 'ACTIVE' && site.active) ||
        (selectedStatus === 'INACTIVE' && !site.active);

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

  private reactivateSite(siteId: number) {
    this.padelService.updateSite(siteId, { active: true }).subscribe({
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
    return this.visibleSites().filter(site => site.active).length;
  }

  inactiveSites() {
    return this.visibleSites().filter(site => !site.active).length;
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
    return site.active ? 'Actif' : 'Inactif';
  }

  getSiteStatusClass(site: any): string {
    return site.active
      ? 'bg-emerald-100 text-emerald-700'
      : 'bg-red-100 text-red-700';
  }

  openClosureEditor(site: any) {
    this.addingClosure.set(site);

    this.closureForm.set({
      siteId: site.id,
      dateFermeture: '',
      raison: '',
      global: false
    });
  }

  closeClosureEditor() {
    this.addingClosure.set(null);
  }

  saveClosure() {
    const form = this.closureForm();

    if (!form.siteId || !form.dateFermeture) {
      this.snackBar.open('Choisis une date de fermeture.', 'OK', {
        duration: 3000
      });
      return;
    }

    this.padelService.createSiteClosingDay(form).subscribe({
      next: () => {
        this.snackBar.open('Jour de fermeture ajouté.', 'OK', {
          duration: 3000
        });

        this.closeClosureEditor();
        this.loadSites();
      },
      error: () => {
        this.snackBar.open('Impossible d’ajouter ce jour de fermeture.', 'OK', {
          duration: 4000
        });
      }
    });
  }
}
