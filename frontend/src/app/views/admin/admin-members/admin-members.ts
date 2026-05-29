import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

import { PadelService } from '../../../services/padel.service';
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
    this.loadMembers();
    this.loadSites();
  }

  loadMembers() {
    this.padelService.getMembers().subscribe(members => {
      this.members.set(members);
    });
  }

  loadSites() {
    this.padelService.getSites().subscribe(sites => {
      this.sites.set(sites);
    });
  }

  filteredMembers() {
    const query = this.search().toLowerCase().trim();
    const selectedType = this.selectedType();
    const selectedSiteId = this.selectedSiteId();
    const selectedStatus = this.selectedStatus();

    return this.members().filter(member => {
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
        member.siteId === selectedSiteId;

      const matchesStatus =
        selectedStatus === 'ALL' ||
        (selectedStatus === 'ACTIVE' && member.active) ||
        (selectedStatus === 'INACTIVE' && !member.active);

      return matchesSearch && matchesType && matchesSite && matchesStatus;
    });
  }

  showCardsView() {
    this.viewMode.set('cards');
  }

  showTableView() {
    this.viewMode.set('table');
  }

  resetFilters() {
    this.search.set('');
    this.selectedType.set('ALL');
    this.selectedSiteId.set('ALL');
    this.selectedStatus.set('ALL');
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
    const dialogRef = this.dialog.open(CreateMemberDialog);

    dialogRef.afterClosed().subscribe(member => {
      if (!member) {
        return;
      }

      this.padelService.createMember(member).subscribe({
        next: () => {
          this.snackBar.open('Membre créé avec succès.', 'OK', {
            duration: 3000
          });

          this.loadMembers();
        },
        error: () => {
          this.snackBar.open('Impossible de créer le membre.', 'OK', {
            duration: 4000
          });
        }
      });
    });
  }
}
