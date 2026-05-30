import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';

import { PadelService } from '../../../services/padel.service';
import { AuthService } from '../../../services/auth.service';

type ReportMode = 'YEAR' | 'MONTH';

type ChartItem = {
  label: string;
  value: number;
  subLabel?: string;
};

@Component({
  selector: 'app-admin-statistiques',
  standalone: true,
  imports: [
    RouterLink,
    DatePipe,
    MatIconModule,
    FormsModule
  ],
  templateUrl: './admin-statistiques.html'
})
export class AdminStatistiques implements OnInit {
  private padelService = inject(PadelService);
  authService = inject(AuthService);

  sites = signal<any[]>([]);
  courts = signal<any[]>([]);
  reservations = signal<any[]>([]);
  members = signal<any[]>([]);
  payments = signal<any[]>([]);

  selectedYear = signal<number>(new Date().getFullYear());
  selectedMonth = signal<number | 'ALL'>('ALL');
  selectedSiteId = signal<number | 'ALL'>('ALL');
  reportMode = signal<ReportMode>('YEAR');

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    forkJoin({
      sites: this.padelService.getSites(),
      courts: this.padelService.getCourts(),
      reservations: this.padelService.getAllReservations(),
      members: this.padelService.getMembers(),
      payments: this.padelService.getPayments()
    }).subscribe(({ sites, courts, reservations, members, payments }) => {
      const admin = this.authService.currentAdmin();

      let visibleSites = sites;
      let visibleCourts = courts;
      let visibleReservations = reservations;
      let visiblePayments = payments;

      if (this.authService.isSiteAdmin()) {
        visibleSites = sites.filter(site => site.id === admin?.siteId);
        visibleCourts = courts.filter(court => court.siteId === admin?.siteId);

        visibleReservations = reservations.filter(reservation => {
          const court = visibleCourts.find(c => c.id === reservation.courtId);
          return !!court;
        });

        const visibleReservationIds = visibleReservations.map(r => r.id);

        visiblePayments = payments.filter(payment =>
          visibleReservationIds.includes(payment.reservationId)
        );

        this.selectedSiteId.set(admin?.siteId);
      }

      this.sites.set(visibleSites);
      this.courts.set(visibleCourts);
      this.reservations.set(visibleReservations);
      this.members.set(members);
      this.payments.set(visiblePayments);
    });
  }

  setReportMode(mode: ReportMode) {
    this.reportMode.set(mode);

    if (mode === 'YEAR') {
      this.selectedMonth.set('ALL');
    }

    if (mode === 'MONTH' && this.selectedMonth() === 'ALL') {
      this.selectedMonth.set(new Date().getMonth() + 1);
    }
  }

  resetFilters() {
    this.selectedYear.set(new Date().getFullYear());
    this.selectedMonth.set('ALL');
    this.selectedSiteId.set(this.authService.isSiteAdmin()
      ? this.authService.currentAdmin()?.siteId
      : 'ALL'
    );
    this.reportMode.set('YEAR');
  }

  availableYears(): number[] {
    const years = new Set<number>();

    this.reservations().forEach(reservation => {
      const year = this.getReservationDate(reservation).getFullYear();
      if (!Number.isNaN(year)) {
        years.add(year);
      }
    });

    this.payments().forEach(payment => {
      const date = this.getPaymentDateObject(payment);
      const year = date.getFullYear();

      if (!Number.isNaN(year)) {
        years.add(year);
      }
    });

    years.add(new Date().getFullYear());

    return [...years].sort((a, b) => b - a);
  }

  months() {
    return [
      { value: 1, label: 'Janvier' },
      { value: 2, label: 'Février' },
      { value: 3, label: 'Mars' },
      { value: 4, label: 'Avril' },
      { value: 5, label: 'Mai' },
      { value: 6, label: 'Juin' },
      { value: 7, label: 'Juillet' },
      { value: 8, label: 'Août' },
      { value: 9, label: 'Septembre' },
      { value: 10, label: 'Octobre' },
      { value: 11, label: 'Novembre' },
      { value: 12, label: 'Décembre' }
    ];
  }

  selectedPeriodLabel(): string {
    const year = this.selectedYear();

    if (this.selectedMonth() === 'ALL') {
      return `Année ${year}`;
    }

    const month = this.months().find(m => m.value === this.selectedMonth());
    return `${month?.label || 'Mois'} ${year}`;
  }

  selectedSiteLabel(): string {
    if (this.selectedSiteId() === 'ALL') {
      return 'Tous les sites';
    }

    const site = this.sites().find(site => site.id === this.selectedSiteId());
    return site?.clubName || site?.name || site?.city || 'Site inconnu';
  }

  visibleCourtsForSelectedSite() {
    if (this.selectedSiteId() === 'ALL') {
      return this.courts();
    }

    return this.courts().filter(court =>
      court.siteId === this.selectedSiteId()
    );
  }

  visibleReservationsForSelectedSite() {
    const selectedSiteId = this.selectedSiteId();

    if (selectedSiteId === 'ALL') {
      return this.reservations();
    }

    return this.reservations().filter(reservation => {
      const court = this.courts().find(c => c.id === reservation.courtId);
      return court?.siteId === selectedSiteId;
    });
  }

  filteredReservations() {
    const selectedYear = this.selectedYear();
    const selectedMonth = this.selectedMonth();

    return this.visibleReservationsForSelectedSite().filter(reservation => {
      const date = this.getReservationDate(reservation);

      const matchesYear = date.getFullYear() === selectedYear;
      const matchesMonth =
        selectedMonth === 'ALL' ||
        date.getMonth() + 1 === selectedMonth;

      return matchesYear && matchesMonth;
    });
  }

  filteredPayments() {
    const selectedYear = this.selectedYear();
    const selectedMonth = this.selectedMonth();
    const selectedSiteId = this.selectedSiteId();

    const selectedReservationIds = this.filteredReservations().map(r => r.id);

    return this.payments().filter(payment => {
      const date = this.getPaymentDateObject(payment);

      const matchesYear = date.getFullYear() === selectedYear;
      const matchesMonth =
        selectedMonth === 'ALL' ||
        date.getMonth() + 1 === selectedMonth;

      const matchesSite =
        selectedSiteId === 'ALL' ||
        selectedReservationIds.includes(payment.reservationId);

      return matchesYear && matchesMonth && matchesSite;
    });
  }

  getReservationDate(reservation: any): Date {
    return new Date(`${reservation.date}T${reservation.startTime || '00:00'}`);
  }

  getPaymentDateObject(payment: any): Date {
    return new Date(payment.datePaiement || payment.dateCreation || new Date());
  }

  totalReservations() {
    return this.filteredReservations().length;
  }

  upcomingReservations() {
    const now = new Date();

    return this.filteredReservations().filter(reservation =>
      this.getReservationDate(reservation) > now
    ).length;
  }

  todayReservations() {
    const now = new Date();

    return this.filteredReservations().filter(reservation => {
      const reservationDate = this.getReservationDate(reservation);

      return reservationDate.getFullYear() === now.getFullYear()
        && reservationDate.getMonth() === now.getMonth()
        && reservationDate.getDate() === now.getDate();
    }).length;
  }

  activeMembers() {
    if (this.selectedSiteId() === 'ALL') {
      return this.members().filter(member => member.active !== false).length;
    }

    return this.members().filter(member =>
      member.active !== false &&
      member.siteId === this.selectedSiteId()
    ).length;
  }

  suspendedMembers() {
    if (this.selectedSiteId() === 'ALL') {
      return this.members().filter(member => member.active === false).length;
    }

    return this.members().filter(member =>
      member.active === false &&
      member.siteId === this.selectedSiteId()
    ).length;
  }

  activeSites() {
    return this.sites().filter(site => site.active !== false).length;
  }

  inactiveSites() {
    return this.sites().filter(site => site.active === false).length;
  }

  activeCourts() {
    return this.visibleCourtsForSelectedSite()
      .filter(court => court.active !== false && court.maintenance !== true)
      .length;
  }

  inactiveCourts() {
    return this.visibleCourtsForSelectedSite()
      .filter(court => court.active === false)
      .length;
  }

  maintenanceCourts() {
    return this.visibleCourtsForSelectedSite()
      .filter(court => court.maintenance === true)
      .length;
  }

  courtStatusItems(): ChartItem[] {
    return [
      {
        label: 'Disponibles',
        value: this.activeCourts()
      },
      {
        label: 'Maintenance',
        value: this.maintenanceCourts()
      },
      {
        label: 'Inactifs',
        value: this.inactiveCourts()
      }
    ];
  }

  /**
   * CA encaissé brut :
   * - VALIDE = paiement encore encaissé
   * - REMBOURSE = paiement qui a bien été encaissé puis remboursé
   *
   * Important : un paiement REMBOURSE ne doit pas disparaître du brut.
   * Sinon, netRevenue ferait VALIDE - REMBOURSE et compterait le remboursement deux fois.
   */
  grossRevenue() {
    return this.filteredPayments()
      .filter(payment =>
        payment.statut === 'VALIDE' ||
        payment.statut === 'REMBOURSE'
      )
      .reduce((sum, payment) => sum + this.getPaymentAmount(payment), 0);
  }

  totalPaid() {
    return this.grossRevenue();
  }

  totalValidatedStillPaid() {
    return this.filteredPayments()
      .filter(payment => payment.statut === 'VALIDE')
      .reduce((sum, payment) => sum + this.getPaymentAmount(payment), 0);
  }

  totalRefunded() {
    return this.filteredPayments()
      .filter(payment => payment.statut === 'REMBOURSE')
      .reduce((sum, payment) => sum + this.getPaymentAmount(payment), 0);
  }

  netRevenue() {
    return this.grossRevenue() - this.totalRefunded();
  }

  pendingPayments() {
    return this.filteredPayments()
      .filter(payment => (payment.statut || 'EN_ATTENTE') === 'EN_ATTENTE')
      .length;
  }

  refusedPayments() {
    return this.filteredPayments()
      .filter(payment => payment.statut === 'REFUSE')
      .length;
  }

  getPaymentAmount(payment: any): number {
    return (payment.montantCentimes ?? 0) / 100;
  }

  estimatedOccupationRate() {
    const activeCourtCount = this.activeCourts();

    if (activeCourtCount === 0) {
      return 0;
    }

    const days = this.daysInSelectedPeriod();
    const slotsPerDay = 8;
    const theoreticalCapacity = activeCourtCount * days * slotsPerDay;

    if (theoreticalCapacity === 0) {
      return 0;
    }

    return Math.min(
      100,
      Math.round((this.totalReservations() / theoreticalCapacity) * 100)
    );
  }

  daysInSelectedPeriod() {
    const year = this.selectedYear();
    const month = this.selectedMonth();

    if (month === 'ALL') {
      return this.isLeapYear(year) ? 366 : 365;
    }

    return new Date(year, month, 0).getDate();
  }

  isLeapYear(year: number) {
    return new Date(year, 1, 29).getMonth() === 1;
  }

  monthlyRevenue(): ChartItem[] {
    return this.months().map(month => {
      const payments = this.filteredPaymentsForMonth(month.value);

      const gross = payments
        .filter(payment =>
          payment.statut === 'VALIDE' ||
          payment.statut === 'REMBOURSE'
        )
        .reduce((sum, payment) => sum + this.getPaymentAmount(payment), 0);

      const refunded = payments
        .filter(payment => payment.statut === 'REMBOURSE')
        .reduce((sum, payment) => sum + this.getPaymentAmount(payment), 0);

      return {
        label: month.label.substring(0, 3),
        value: gross - refunded
      };
    });
  }

  monthlyReservations(): ChartItem[] {
    return this.months().map(month => {
      const value = this.visibleReservationsForSelectedSite()
        .filter(reservation => {
          const date = this.getReservationDate(reservation);
          return date.getFullYear() === this.selectedYear()
            && date.getMonth() + 1 === month.value;
        })
        .length;

      return {
        label: month.label.substring(0, 3),
        value
      };
    });
  }

  filteredPaymentsForMonth(month: number) {
    const selectedReservationIds = this.visibleReservationsForSelectedSite()
      .filter(reservation => {
        const date = this.getReservationDate(reservation);
        return date.getFullYear() === this.selectedYear()
          && date.getMonth() + 1 === month;
      })
      .map(reservation => reservation.id);

    return this.payments().filter(payment => {
      const date = this.getPaymentDateObject(payment);

      const matchesDate =
        date.getFullYear() === this.selectedYear()
        && date.getMonth() + 1 === month;

      const matchesSite =
        this.selectedSiteId() === 'ALL' ||
        selectedReservationIds.includes(payment.reservationId);

      return matchesDate && matchesSite;
    });
  }

  reservationsBySite(): ChartItem[] {
    return this.sites()
      .filter(site =>
        this.selectedSiteId() === 'ALL' ||
        site.id === this.selectedSiteId()
      )
      .map(site => {
        const courtIds = this.getCourtIdsForSite(site.id);

        const count = this.filteredReservations().filter(reservation =>
          courtIds.includes(reservation.courtId)
        ).length;

        return {
          label: site.clubName || site.name || site.city,
          value: count
        };
      })
      .sort((a, b) => b.value - a.value);
  }

  paymentsByStatus(): ChartItem[] {
    const statuses = ['EN_ATTENTE', 'VALIDE', 'REFUSE', 'REMBOURSE'];

    return statuses.map(status => ({
      label: this.getPaymentStatusLabel(status),
      value: this.filteredPayments().filter(payment =>
        (payment.statut || 'EN_ATTENTE') === status
      ).length
    }));
  }

  membersByType(): ChartItem[] {
    const types = ['GLOBAL', 'SITE', 'LIBRE'];

    return types.map(type => ({
      label: type,
      value: this.members().filter(member => {
        const matchesType = (member.type?.code || 'LIBRE') === type;

        const matchesSite =
          this.selectedSiteId() === 'ALL' ||
          member.siteId === this.selectedSiteId();

        return matchesType && matchesSite;
      }).length
    }));
  }

  topCourts(): ChartItem[] {
    return this.visibleCourtsForSelectedSite()
      .map(court => {
        const count = this.filteredReservations().filter(reservation =>
          reservation.courtId === court.id
        ).length;

        return {
          label: court.name,
          subLabel: this.getSiteNameByCourt(court),
          value: count
        };
      })
      .sort((a, b) => b.value - a.value)
      .slice(0, 5);
  }

  mostActiveSiteLabel() {
    const sites = this.reservationsBySite();

    if (sites.length === 0 || sites[0].value === 0) {
      return 'Aucun site';
    }

    return sites[0].label;
  }

  topCourtLabel() {
    const courts = this.topCourts();

    if (courts.length === 0 || courts[0].value === 0) {
      return 'Aucun terrain';
    }

    return courts[0].label;
  }

  nextReservations() {
    const now = new Date();

    return this.visibleReservationsForSelectedSite()
      .filter(reservation => this.getReservationDate(reservation) > now)
      .sort((a, b) =>
        this.getReservationDate(a).getTime() - this.getReservationDate(b).getTime()
      )
      .slice(0, 5);
  }

  pendingPaymentItems() {
    return this.filteredPayments()
      .filter(payment => (payment.statut || 'EN_ATTENTE') === 'EN_ATTENTE')
      .slice(0, 5);
  }

  inactiveSiteItems() {
    return this.sites()
      .filter(site => site.active === false)
      .slice(0, 5);
  }

  inactiveCourtItems() {
    return this.visibleCourtsForSelectedSite()
      .filter(court => court.active === false)
      .slice(0, 5);
  }

  maintenanceCourtItems() {
    return this.visibleCourtsForSelectedSite()
      .filter(court => court.maintenance === true)
      .slice(0, 5);
  }

  getCourtIdsForSite(siteId: number): number[] {
    return this.courts()
      .filter(court => court.siteId === siteId)
      .map(court => court.id);
  }

  getSiteNameByCourt(court: any): string {
    const site = this.sites().find(site => site.id === court.siteId);
    return site?.clubName || site?.name || site?.city || 'Site inconnu';
  }

  getCourtName(reservation: any): string {
    const court = this.courts().find(court => court.id === reservation.courtId);
    return reservation.courtName || court?.name || 'Terrain inconnu';
  }

  getSiteName(reservation: any): string {
    if (reservation.siteName) {
      return reservation.siteName;
    }

    const court = this.courts().find(court => court.id === reservation.courtId);
    const site = this.sites().find(site => site.id === court?.siteId);

    return site?.clubName || site?.name || site?.city || 'Site inconnu';
  }

  getMemberNameFromPayment(payment: any): string {
    const member = this.members().find(member =>
      member.id === payment.membreId ||
      member.id === payment.memberId
    );

    if (!member) {
      return 'Membre inconnu';
    }

    const firstName = member.firstName || member.prenom || '';
    const lastName = member.lastName || member.nom || '';

    return `${firstName} ${lastName}`.trim() || member.matricule || 'Membre inconnu';
  }

  getPaymentStatusLabel(status: string): string {
    switch (status) {
      case 'EN_ATTENTE':
        return 'En attente';
      case 'VALIDE':
        return 'Validés';
      case 'REFUSE':
        return 'Refusés';
      case 'REMBOURSE':
        return 'Remboursés';
      default:
        return status;
    }
  }

  maxValue(items: { value: number }[]) {
    const values = items.map(item => item.value);
    return Math.max(...values, 1);
  }

  barWidth(value: number, items: { value: number }[]) {
    return Math.round((value / this.maxValue(items)) * 100);
  }
}
