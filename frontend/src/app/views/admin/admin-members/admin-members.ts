import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

import { PadelService } from '../../../services/padel.service';
import { AuthService } from '../../../services/auth.service';
import { CreateMemberDialog } from './create-member-dialog/create-member-dialog';

@Component({
  selector: 'app-admin-members',
  standalone: true,
  imports: [
    RouterLink,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    FormsModule
  ],
  templateUrl: './admin-members.html'
})
export class AdminMembers implements OnInit {
  private padelService = inject(PadelService);
  private authService = inject(AuthService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  members = signal<any[]>([]);
  sites = signal<any[]>([]);

  search = signal('');
  selectedType = signal<'ALL' | 'GLOBAL' | 'SITE' | 'LIBRE'>('ALL');
  selectedSiteId = signal<number | 'ALL'>('ALL');
  selectedStatus = signal<'ALL' | 'ACTIVE' | 'INACTIVE'>('ALL');
  viewMode = signal<'cards' | 'table'>('cards');

  ngOnInit() {
    this.loadSites();
    this.loadMembers();
  }

  loadMembers() {
    const admin = this.authService.currentAdmin();

    if (!admin?.matricule) {
      this.snackBar.open('Administrateur non connecté.', 'OK', {
        duration: 4000
      });
      return;
    }

    this.padelService.getMembersForAdmin(admin.matricule).subscribe({
      next: members => {
        this.members.set(members);
      },
      error: () => {
        this.snackBar.open('Impossible de charger les membres.', 'OK', {
          duration: 4000
        });
      }
    });
  }

  loadSites() {
    const admin = this.authService.currentAdmin();
    const adminSiteId = this.getAdminSiteId(admin);

    this.padelService.getSites().subscribe({
      next: sites => {
        if (this.isSiteAdmin(admin)) {
          this.sites.set(
            sites.filter(site => Number(site.id) === Number(adminSiteId))
          );

          if (adminSiteId !== null) {
            this.selectedSiteId.set(Number(adminSiteId));
          }

          this.selectedType.set('SITE');
          return;
        }

        this.sites.set(sites);
      },
      error: () => {
        this.snackBar.open('Impossible de charger les sites.', 'OK', {
          duration: 4000
        });
      }
    });
  }

  filteredMembers() {
    const admin = this.authService.currentAdmin();
    const adminSiteId = this.getAdminSiteId(admin);

    const query = this.search().toLowerCase().trim();
    const selectedType = this.selectedType();
    const selectedSiteId = this.selectedSiteId();
    const selectedStatus = this.selectedStatus();

    return this.members().filter(member => {
      const matchesAdminScope =
        !this.isSiteAdmin(admin) ||
        (
          member.type?.code === 'SITE' &&
          Number(member.siteId) === Number(adminSiteId)
        );

      const matchesSearch =
        !query ||
        member.firstName?.toLowerCase().includes(query) ||
        member.lastName?.toLowerCase().includes(query) ||
        member.email?.toLowerCase().includes(query) ||
        member.matricule?.toLowerCase().includes(query);

      const matchesType =
        selectedType === 'ALL' ||
        member.type?.code === selectedType;

      const matchesSite =
        selectedSiteId === 'ALL' ||
        Number(member.siteId) === Number(selectedSiteId);

      const matchesStatus =
        selectedStatus === 'ALL' ||
        (selectedStatus === 'ACTIVE' && member.active) ||
        (selectedStatus === 'INACTIVE' && !member.active);

      return matchesAdminScope && matchesSearch && matchesType && matchesSite && matchesStatus;
    });
  }

  canCreateMember(): boolean {
    const admin = this.authService.currentAdmin();

    if (admin?.typeAdmin === 'GLOBAL') {
      return true;
    }

    if (!this.isSiteAdmin(admin)) {
      return false;
    }

    const adminSite = this.getCurrentAdminSite();

    return adminSite?.active === true;
  }

  getCreateMemberDisabledReason(): string {
    const admin = this.authService.currentAdmin();

    if (!admin) {
      return 'Administrateur non connecté.';
    }

    if (admin.typeAdmin === 'GLOBAL') {
      return '';
    }

    if (!this.isSiteAdmin(admin)) {
      return 'Type administrateur non autorisé.';
    }

    const adminSite = this.getCurrentAdminSite();

    if (!adminSite) {
      return 'Site administrateur introuvable.';
    }

    if (!adminSite.active) {
      return 'Impossible d’ajouter un membre : votre site est inactif.';
    }

    return '';
  }

  showCardsView() {
    this.viewMode.set('cards');
  }

  showTableView() {
    this.viewMode.set('table');
  }

  resetFilters() {
    const admin = this.authService.currentAdmin();
    const adminSiteId = this.getAdminSiteId(admin);

    this.search.set('');
    this.selectedStatus.set('ALL');

    if (this.isSiteAdmin(admin)) {
      this.selectedType.set('SITE');

      if (adminSiteId !== null) {
        this.selectedSiteId.set(Number(adminSiteId));
      }

      return;
    }

    this.selectedType.set('ALL');
    this.selectedSiteId.set('ALL');
  }

  getMemberTypeLabel(member: any): string {
    const code = member.type?.code || 'LIBRE';
    const days = member.type?.delai_reservation_jours || 5;

    return `${code} · ${days} jours`;
  }

  getMemberTypeClass(member: any): string {
    if (member.type?.code === 'GLOBAL') {
      return 'bg-violet-100 text-violet-700';
    }

    if (member.type?.code === 'SITE') {
      return 'bg-emerald-100 text-emerald-700';
    }

    return 'bg-slate-100 text-slate-600';
  }

  getMemberStatusLabel(member: any): string {
    return member.active ? 'Actif' : 'Suspendu';
  }

  getMemberStatusClass(member: any): string {
    return member.active
      ? 'bg-emerald-100 text-emerald-700'
      : 'bg-red-100 text-red-700';
  }

  getMemberSiteLabel(member: any): string {
    return member.siteName || 'Global';
  }

  toggleMemberActive(member: any) {
    const isActive = member.active !== false;
    const nextActive = !isActive;

    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: isActive ? 'Suspendre le membre' : 'Réactiver le membre',
        message: isActive
          ? `Suspendre ${member.firstName} ${member.lastName} ? Le membre ne pourra plus effectuer de réservation.`
          : `Réactiver ${member.firstName} ${member.lastName} ? Le membre pourra à nouveau réserver.`,
        confirmLabel: isActive ? 'Suspendre' : 'Réactiver',
        cancelLabel: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.padelService.updateMemberActiveStatus(member.id, nextActive).subscribe({
        next: () => {
          this.snackBar.open(
            nextActive ? 'Membre réactivé avec succès.' : 'Membre suspendu avec succès.',
            'OK',
            { duration: 3000 }
          );

          this.loadMembers();
        },
        error: () => {
          this.snackBar.open(
            nextActive ? 'Impossible de réactiver le membre.' : 'Impossible de suspendre le membre.',
            'OK',
            { duration: 4000 }
          );
        }
      });
    });
  }

  openCreateMemberDialog() {
    if (!this.canCreateMember()) {
      this.snackBar.open(this.getCreateMemberDisabledReason(), 'OK', {
        duration: 4000
      });
      return;
    }

    const admin = this.authService.currentAdmin();

    if (!admin?.matricule) {
      this.snackBar.open('Administrateur non connecté.', 'OK', {
        duration: 4000
      });
      return;
    }

    const dialogRef = this.dialog.open(CreateMemberDialog, {
      data: {
        admin,
        sites: this.sites()
      }
    });

    dialogRef.afterClosed().subscribe(member => {
      if (!member) {
        return;
      }

      this.padelService.createMemberAsAdmin(admin.matricule, member).subscribe({
        next: () => {
          this.snackBar.open('Membre créé avec succès.', 'OK', {
            duration: 3000
          });

          this.loadMembers();
        },
        error: error => {
          const message =
            error?.error?.message ||
            error?.error?.error ||
            'Impossible de créer le membre.';

          this.snackBar.open(message, 'OK', {
            duration: 5000
          });
        }
      });
    });
  }

  private getCurrentAdminSite(): any | null {
    const admin = this.authService.currentAdmin();
    const adminSiteId = this.getAdminSiteId(admin);

    if (adminSiteId === null) {
      return null;
    }

    return this.sites().find(site =>
      Number(site.id) === Number(adminSiteId)
    ) ?? null;
  }

  private isSiteAdmin(admin: any): boolean {
    return admin?.typeAdmin === 'SITE';
  }

  private getAdminSiteId(admin: any): number | null {
    return admin?.siteId ?? admin?.site?.id ?? null;
  }
}
