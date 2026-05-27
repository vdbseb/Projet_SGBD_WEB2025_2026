import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { PadelService } from '../../../services/padel.service';
import {AuthService} from '../../../services/auth.service';

@Component({
  selector: 'app-admin-sites',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  templateUrl: './admin-sites.html'
})
export class AdminSites implements OnInit {
  private padelService = inject(PadelService);

  sites = signal<any[]>([]);
  reservations = signal<any[]>([]);
  search = signal('');
  authService = inject(AuthService);

  ngOnInit() {
    this.padelService.getSites().subscribe(sites => {
      this.sites.set(sites);
    });

    this.padelService.getAllReservations().subscribe(reservations => {
      this.reservations.set(reservations);
    });
  }

  filteredSites() {
    const admin = this.authService.currentAdmin();

    let visibleSites = this.sites();

    if (this.authService.isSiteAdmin()) {
      visibleSites = visibleSites.filter(site => site.id === admin.siteId);
    }

    const query = this.search().toLowerCase().trim();

    if (!query) {
      return visibleSites;
    }

    return visibleSites.filter(site =>
      site.clubName?.toLowerCase().includes(query) ||
      site.name?.toLowerCase().includes(query) ||
      site.city?.toLowerCase().includes(query)
    );
  }

  getCourtCount(site: any): number {
    return site.courts?.length || 0;
  }

  getIndoorCount(site: any): number {
    return site.courts?.filter((court: any) =>
      court.type?.toLowerCase() === 'indoor'
    ).length || 0;
  }

  getOutdoorCount(site: any): number {
    return site.courts?.filter((court: any) =>
      court.type?.toLowerCase() === 'outdoor'
    ).length || 0;
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
}
