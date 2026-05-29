import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';

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

  resetFilters() {
    this.search.set('');
    this.selectedType.set('ALL');
    this.selectedSiteId.set('ALL');
    this.selectedStatus.set('ALL');
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
