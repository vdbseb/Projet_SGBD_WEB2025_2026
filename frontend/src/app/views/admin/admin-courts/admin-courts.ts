import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { PadelService } from '../../../services/padel.service';
import { AuthService } from '../../../services/auth.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';
import { FormsModule } from '@angular/forms';

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
  search = signal('');
  reservations = signal<any[]>([]);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);
  maintenanceCourts = signal<number[]>([]);

  selectedSiteId = signal<number | 'ALL'>('ALL');
  selectedType = signal<'ALL' | 'INDOOR' | 'OUTDOOR'>('ALL');
  selectedStatus = signal<'ALL' | 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'>('ALL');

  ngOnInit() {
    this.padelService.getSites().subscribe(sites => {
      this.sites.set(sites);
    });

    this.padelService.getCourts().subscribe(courts => {
      const admin = this.authService.currentAdmin();

      const visibleCourts = this.authService.isSiteAdmin()
        ? courts.filter(court => court.siteId === admin.siteId)
        : courts;

      this.courts.set(visibleCourts);
    });

    this.padelService.getAllReservations().subscribe(reservations => {
      this.reservations.set(reservations);
    });
  }

  filteredCourts() {
    const query = this.search().toLowerCase().trim();
    const selectedSiteId = this.selectedSiteId();
    const selectedType = this.selectedType();
    const selectedStatus = this.selectedStatus();

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

      const matchesStatus =
        selectedStatus === 'ALL' ||
        (selectedStatus === 'ACTIVE' && court.active && !this.isInMaintenance(court)) ||
        (selectedStatus === 'INACTIVE' && !court.active) ||
        (selectedStatus === 'MAINTENANCE' && this.isInMaintenance(court));

      return matchesSearch && matchesSite && matchesType && matchesStatus;
    });
  }

  resetFilters() {
    this.search.set('');
    this.selectedSiteId.set('ALL');
    this.selectedType.set('ALL');
    this.selectedStatus.set('ALL');
  }

  getCourtType(court: any): string {
    return court.type ?? (court.indoor ? 'Indoor' : 'Outdoor');
  }

  getSiteName(court: any): string {
    const site = this.sites().find(site => site.id === court.siteId);
    return site?.clubName || site?.name || 'Site inconnu';
  }

  getCourtTypeClass(court: any): string {
    return this.getCourtType(court).toLowerCase() === 'indoor'
      ? 'bg-blue-100 text-blue-700'
      : 'bg-emerald-100 text-emerald-700';
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
    const rate = this.getOccupationRate(court);

    if (rate >= 75) {
      return 'bg-red-500';
    }

    if (rate >= 40) {
      return 'bg-orange-500';
    }

    return 'bg-emerald-500';
  }

  isInMaintenance(court: any): boolean {
    return this.maintenanceCourts().includes(court.id);
  }

  toggleMaintenance(court: any) {
    const inMaintenance = this.isInMaintenance(court);

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: inMaintenance ? 'Remise en service' : 'Maintenance',
        message: inMaintenance
          ? 'Remettre ce terrain en service ?'
          : 'Mettre ce terrain en maintenance ?',
        confirmLabel: inMaintenance ? 'Remettre en service' : 'Confirmer',
        cancelLabel: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      if (inMaintenance) {
        this.maintenanceCourts.update(ids =>
          ids.filter(id => id !== court.id)
        );

        this.snackBar.open('Terrain remis en service.', 'OK', {
          duration: 3000
        });

        return;
      }

      this.maintenanceCourts.update(ids => [...ids, court.id]);

      this.snackBar.open('Terrain mis en maintenance.', 'OK', {
        duration: 3000
      });
    });
  }
}
