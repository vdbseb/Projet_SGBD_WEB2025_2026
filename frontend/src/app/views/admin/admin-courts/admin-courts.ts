import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { PadelService } from '../../../services/padel.service';
import { AuthService } from '../../../services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import { FormsModule } from '@angular/forms';
import { getHttpErrorUserMessage } from '../../../shared/api-error.util';

type CourtViewMode = 'CARDS' | 'TABLE';
type CourtTypeFilter = 'ALL' | 'INDOOR' | 'OUTDOOR';
type CourtStatusFilter = 'ALL' | 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE';

@Component({
  selector: 'app-admin-courts',
  standalone: true,
  imports: [RouterLink, MatIconModule, FormsModule],
  templateUrl: './admin-courts.html'
})
export class AdminCourts implements OnInit {
  private padelService = inject(PadelService);
  authService = inject(AuthService);

  courts = signal<any[]>([]);
  sites = signal<any[]>([]);
  reservations = signal<any[]>([]);
  search = signal('');

  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  selectedSiteId = signal<number | 'ALL'>('ALL');
  selectedType = signal<CourtTypeFilter>('ALL');
  selectedStatus = signal<CourtStatusFilter>('ALL');
  viewMode = signal<CourtViewMode>('CARDS');

  ngOnInit() {
    this.loadSites();
    this.loadCourts();
    this.loadReservations();
  }

  filteredCourts() {
    const selectedStatus = this.selectedStatus();

    return this.courtsForCurrentScope().filter(court =>
      selectedStatus === 'ALL' ||
      (selectedStatus === 'ACTIVE' && court.active === true && !this.isInMaintenance(court)) ||
      (selectedStatus === 'INACTIVE' && court.active === false) ||
      (selectedStatus === 'MAINTENANCE' && this.isInMaintenance(court))
    );
  }

  courtsForCurrentScope() {
    const query = this.search().toLowerCase().trim();
    const selectedSiteId = this.selectedSiteId();
    const selectedType = this.selectedType();

    return this.courts().filter(court => {
      const type = this.getCourtType(court);

      const matchesSearch =
        !query ||
        court.name?.toLowerCase().includes(query) ||
        type.toLowerCase().includes(query) ||
        this.getSiteName(court).toLowerCase().includes(query);

      const matchesSite =
        selectedSiteId === 'ALL' ||
        court.siteId === selectedSiteId;

      const matchesType =
        selectedType === 'ALL' ||
        (selectedType === 'INDOOR' && type.toLowerCase() === 'indoor') ||
        (selectedType === 'OUTDOOR' && type.toLowerCase() === 'outdoor');

      return matchesSearch && matchesSite && matchesType;
    });
  }

  resetFilters() {
    this.search.set('');
    this.selectedSiteId.set(this.authService.isSiteAdmin()
      ? this.authService.currentAdmin()?.siteId
      : 'ALL'
    );
    this.selectedType.set('ALL');
    this.selectedStatus.set('ALL');
  }

  setStatusFilter(status: CourtStatusFilter) {
    this.selectedStatus.set(status);
  }

  setViewMode(mode: CourtViewMode) {
    this.viewMode.set(mode);
  }

  totalCourtCount(): number {
    return this.courtsForCurrentScope().length;
  }

  activeCourtCount(): number {
    return this.courtsForCurrentScope()
      .filter(court => court.active === true && court.maintenance !== true)
      .length;
  }

  maintenanceCourtCount(): number {
    return this.courtsForCurrentScope()
      .filter(court => court.maintenance === true)
      .length;
  }

  inactiveCourtCount(): number {
    return this.courtsForCurrentScope()
      .filter(court => court.active === false)
      .length;
  }

  hasActiveFilters(): boolean {
    const siteFilterActive = this.authService.isSiteAdmin()
      ? false
      : this.selectedSiteId() !== 'ALL';

    return this.search().trim().length > 0 ||
      siteFilterActive ||
      this.selectedType() !== 'ALL' ||
      this.selectedStatus() !== 'ALL';
  }

  getStatusPillClass(status: CourtStatusFilter): string {
    const selected = this.selectedStatus() === status;

    if (status === 'ACTIVE') {
      return selected
        ? 'bg-emerald-600 text-white shadow-sm'
        : 'bg-emerald-50 text-emerald-700 hover:bg-emerald-100';
    }

    if (status === 'MAINTENANCE') {
      return selected
        ? 'bg-red-600 text-white shadow-sm'
        : 'bg-red-50 text-red-700 hover:bg-red-100';
    }

    if (status === 'INACTIVE') {
      return selected
        ? 'bg-slate-700 text-white shadow-sm'
        : 'bg-slate-100 text-slate-600 hover:bg-slate-200';
    }

    return selected
      ? 'bg-slate-900 text-white shadow-sm'
      : 'bg-slate-100 text-slate-600 hover:bg-slate-200';
  }

  getCourtType(court: any): string {
    return court.type ?? (court.indoor ? 'Indoor' : 'Outdoor');
  }

  getSiteName(court: any): string {
    const site = this.sites().find(site => site.id === court.siteId);
    return site?.clubName || site?.name || site?.city || 'Site inconnu';
  }

  getCourtTypeClass(court: any): string {
    return this.getCourtType(court).toLowerCase() === 'indoor'
      ? 'bg-blue-100 text-blue-700'
      : 'bg-emerald-100 text-emerald-700';
  }

  getCourtStatusLabel(court: any): string {
    if (court.maintenance === true) {
      return 'Maintenance';
    }

    if (court.active === false) {
      return 'Inactif';
    }

    return 'Actif';
  }

  getCourtStatusClass(court: any): string {
    if (court.maintenance === true) {
      return 'bg-red-100 text-red-700';
    }

    if (court.active === false) {
      return 'bg-slate-100 text-slate-600';
    }

    return 'bg-emerald-100 text-emerald-700';
  }

  getCourtStatusIcon(court: any): string {
    if (court.maintenance === true) {
      return 'construction';
    }

    if (court.active === false) {
      return 'block';
    }

    return 'check_circle';
  }

  getCourtReservationCount(court: any): number {
    return this.reservations().filter(reservation =>
      reservation.courtId === court.id
    ).length;
  }

  getOccupationRate(court: any): number {
    const reservationCount = this.getCourtReservationCount(court);
    const maxSlots = 8;

    return Math.min(100, Math.round((reservationCount / maxSlots) * 100));
  }

  getOccupationClass(court: any): string {
    if (court.maintenance === true) {
      return 'bg-red-500';
    }

    if (court.active === false) {
      return 'bg-slate-400';
    }

    const rate = this.getOccupationRate(court);

    if (rate >= 75) {
      return 'bg-orange-500';
    }

    if (rate >= 40) {
      return 'bg-blue-500';
    }

    return 'bg-emerald-500';
  }

  isInMaintenance(court: any): boolean {
    return court?.maintenance === true;
  }

  canToggleMaintenance(court: any): boolean {
    return court.active !== false || this.isInMaintenance(court);
  }

  getMaintenanceActionLabel(court: any): string {
    if (this.isInMaintenance(court)) {
      return 'Remettre en service';
    }

    if (court.active === false) {
      return 'Terrain inactif';
    }

    return 'Mettre en maintenance';
  }

  toggleMaintenance(court: any) {
    if (!this.canToggleMaintenance(court)) {
      this.snackBar.open(
        'Ce terrain est inactif. Réactive-le avant de gérer sa maintenance.',
        'OK',
        { duration: 4000 }
      );
      return;
    }

    const inMaintenance = this.isInMaintenance(court);
    const nextMaintenanceState = !inMaintenance;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: inMaintenance ? 'Remise en service' : 'Maintenance terrain',
        message: inMaintenance
          ? `Remettre ${court.name} en service ?`
          : `Mettre ${court.name} en maintenance ? Les réservations futures de ce terrain seront annulées et les paiements validés seront remboursés.`,
        confirmLabel: inMaintenance ? 'Remettre en service' : 'Confirmer la maintenance',
        cancelLabel: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.padelService.setCourtMaintenance(court.id, nextMaintenanceState)
        .subscribe({
          next: updatedCourt => {
            this.courts.update(courts =>
              courts.map(existingCourt =>
                existingCourt.id === court.id
                  ? {
                      ...existingCourt,
                      ...updatedCourt,
                      maintenance: updatedCourt.maintenance ?? nextMaintenanceState
                    }
                  : existingCourt
              )
            );

            this.loadReservations();

            this.snackBar.open(
              nextMaintenanceState
                ? 'Terrain mis en maintenance. Les réservations futures ont été annulées/remboursées.'
                : 'Terrain remis en service.',
              'OK',
              { duration: 4500 }
            );
          },
          error: error => {
            this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
              duration: 5000
            });
          }
        });
    });
  }

  private loadSites() {
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

  private loadCourts() {
    this.padelService.getCourts().subscribe({
      next: courts => {
        const admin = this.authService.currentAdmin();

        const visibleCourts = this.authService.isSiteAdmin()
          ? courts.filter(court => court.siteId === admin?.siteId)
          : courts;

        this.courts.set(visibleCourts);
      },
      error: error => {
        this.courts.set([]);

        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
      }
    });
  }

  private loadReservations() {
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
}
