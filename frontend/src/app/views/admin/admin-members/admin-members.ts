import { Component, inject, OnInit, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

import { PadelService } from '../../../services/padel.service';
import { AuthService } from '../../../services/auth.service';
import { CreateMemberDialog } from './create-member-dialog/create-member-dialog';
import { getHttpErrorUserMessage } from '../../../shared/api-error.util';
import { AdminPageShellComponent } from '../shared/admin-page-shell/admin-page-shell';
import { AdminViewMode, AdminViewToggleComponent } from '../shared/admin-view-toggle/admin-view-toggle';

type MemberTypeFilter = 'ALL' | 'GLOBAL' | 'SITE' | 'LIBRE';
type MemberStatusFilter = 'ALL' | 'ACTIVE' | 'INACTIVE';

@Component({
  selector: 'app-admin-members',
  standalone: true,
  imports: [
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule,
    FormsModule,
    AdminPageShellComponent,
    AdminViewToggleComponent
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
  selectedType = signal<MemberTypeFilter>('ALL');
  selectedSiteId = signal<number | 'ALL'>('ALL');
  selectedStatus = signal<MemberStatusFilter>('ALL');
  viewMode = signal<AdminViewMode>('cards');

  ngOnInit() {
    this.initializeAdminScope();
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
      error: error => {
        this.members.set([]);

        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
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

          this.applySiteAdminFilterDefaults(adminSiteId);
          return;
        }

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
        (selectedStatus === 'ACTIVE' && member.active !== false) ||
        (selectedStatus === 'INACTIVE' && member.active === false);

      return matchesAdminScope && matchesSearch && matchesType && matchesSite && matchesStatus;
    });
  }

  setTypeFilter(type: MemberTypeFilter): void {
    if (this.isTypeFilterDisabledForCurrentAdmin(type)) {
      this.snackBar.open(this.getTypeFilterDisabledReason(type), 'OK', {
        duration: 3500
      });
      return;
    }

    this.selectedType.set(type);
  }

  onSiteFilterChange(value: number | string): void {
    const admin = this.authService.currentAdmin();

    if (this.isSiteAdmin(admin)) {
      const adminSiteId = this.getAdminSiteId(admin);

      if (adminSiteId !== null) {
        this.selectedSiteId.set(Number(adminSiteId));
      }

      return;
    }

    this.selectedSiteId.set(value === 'ALL' ? 'ALL' : Number(value));
  }

  isCurrentAdminSiteAdmin(): boolean {
    return this.isSiteAdmin(this.authService.currentAdmin());
  }

  isTypeFilterDisabledForCurrentAdmin(type: MemberTypeFilter): boolean {
    const admin = this.authService.currentAdmin();

    if (!this.isSiteAdmin(admin)) {
      return false;
    }

    return type === 'ALL' || type === 'GLOBAL' || type === 'LIBRE';
  }

  getTypeFilterDisabledReason(type: MemberTypeFilter): string {
    if (!this.isTypeFilterDisabledForCurrentAdmin(type)) {
      return '';
    }

    if (type === 'ALL') {
      return 'Un administrateur de site peut uniquement gérer les membres de type site.';
    }

    if (type === 'GLOBAL') {
      return 'Un administrateur de site ne peut pas gérer les membres globaux.';
    }

    if (type === 'LIBRE') {
      return 'Un administrateur de site ne peut pas gérer les membres libres.';
    }

    return '';
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

  setViewMode(mode: AdminViewMode) {
    this.viewMode.set(mode);
  }

  resetFilters() {
    const admin = this.authService.currentAdmin();
    const adminSiteId = this.getAdminSiteId(admin);

    this.search.set('');
    this.selectedStatus.set('ALL');

    if (this.isSiteAdmin(admin)) {
      this.applySiteAdminFilterDefaults(adminSiteId);
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
    return member.active !== false ? 'Actif' : 'Suspendu';
  }

  getMemberStatusClass(member: any): string {
    return member.active !== false
      ? 'bg-emerald-100 text-emerald-700'
      : 'bg-red-100 text-red-700';
  }

  getMemberSiteLabel(member: any): string {
    return member.siteName || 'Global';
  }

  toggleMemberActive(member: any) {
    const admin = this.authService.currentAdmin();

    if (this.isSiteAdmin(admin) && !this.canSiteAdminManageMember(member, admin)) {
      this.snackBar.open(
        'Un administrateur de site ne peut gérer que les membres de son site.',
        'OK',
        { duration: 4000 }
      );
      return;
    }

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
        error: error => {
          this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
            duration: 5000
          });
        }
      });
    });
  }
openEditMemberDialog(member: any) {
  const admin = this.authService.currentAdmin();

  if (this.isSiteAdmin(admin) && !this.canSiteAdminManageMember(member, admin)) {
    this.snackBar.open(
      'Un administrateur de site ne peut modifier que les membres de son site.',
      'OK',
      { duration: 4000 }
    );
    return;
  }

  if (member.active === false) {
    this.snackBar.open(
      'Modification impossible : ce membre est suspendu. Réactive-le d’abord.',
      'OK',
      { duration: 4000 }
    );
    return;
  }

  const dialogRef = this.dialog.open(CreateMemberDialog, {
    data: {
      mode: 'EDIT',
      member,
      admin,
      sites: this.sites(),
      isSiteAdmin: this.isSiteAdmin(admin),
      adminSiteId: this.getAdminSiteId(admin)
    }
  });

  dialogRef.afterClosed().subscribe(updatedMember => {
    if (!updatedMember) {
      return;
    }

    this.padelService.updateOwnMemberProfile(
      member.id,
      {
        firstName: updatedMember.firstName,
        lastName: updatedMember.lastName,
        email: updatedMember.email
      }
    ).subscribe({
      next: () => {
        this.snackBar.open('Membre modifié avec succès.', 'OK', {
          duration: 3000
        });

        this.loadMembers();
      },
      error: error => {
        this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
          duration: 5000
        });
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
        sites: this.sites(),
        isSiteAdmin: this.isSiteAdmin(admin),
        adminSiteId: this.getAdminSiteId(admin)
      }
    });

    dialogRef.afterClosed().subscribe(member => {
      if (!member) {
        return;
      }

      const payload = this.prepareMemberPayloadForAdmin(member, admin);

      this.padelService.createMemberAsAdmin(admin.matricule, payload).subscribe({
        next: () => {
          this.snackBar.open('Membre créé avec succès.', 'OK', {
            duration: 3000
          });

          this.loadMembers();
        },
        error: error => {
          this.snackBar.open(getHttpErrorUserMessage(error), 'OK', {
            duration: 5000
          });
        }
      });
    });
  }

  private initializeAdminScope(): void {
    const admin = this.authService.currentAdmin();

    if (!this.isSiteAdmin(admin)) {
      return;
    }

    this.applySiteAdminFilterDefaults(this.getAdminSiteId(admin));
  }

  private applySiteAdminFilterDefaults(adminSiteId: number | null): void {
    this.selectedType.set('SITE');

    if (adminSiteId !== null) {
      this.selectedSiteId.set(Number(adminSiteId));
    }
  }

  private prepareMemberPayloadForAdmin(member: any, admin: any): any {
    if (!this.isSiteAdmin(admin)) {
      return member;
    }

    const adminSiteId = this.getAdminSiteId(admin);

    return {
      ...member,
      typeCode: 'SITE',
      typeMembre: 'SITE',
      siteId: adminSiteId
    };
  }

  private canSiteAdminManageMember(member: any, admin: any): boolean {
    const adminSiteId = this.getAdminSiteId(admin);

    return (
      member.type?.code === 'SITE' &&
      Number(member.siteId) === Number(adminSiteId)
    );
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
