import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { PadelService } from '../../../services/padel.service';
import { CreateMemberDialog } from './create-member-dialog/create-member-dialog';

@Component({
  selector: 'app-admin-members',
  standalone: true,
  imports: [
    RouterLink,
    MatIconModule,
    MatDialogModule,
    MatSnackBarModule
  ],
  templateUrl: './admin-members.html'
})
export class AdminMembers implements OnInit {
  private padelService = inject(PadelService);
  private dialog = inject(MatDialog);
  private snackBar = inject(MatSnackBar);

  members = signal<any[]>([]);
  search = signal('');

  ngOnInit() {
    this.loadMembers();
  }

  loadMembers() {
    this.padelService.getMembers().subscribe(members => {
      this.members.set(members);
    });
  }

  filteredMembers() {
    const query = this.search().toLowerCase().trim();

    if (!query) {
      return this.members();
    }

    return this.members().filter(member =>
      member.firstName?.toLowerCase().includes(query) ||
      member.lastName?.toLowerCase().includes(query) ||
      member.matricule?.toLowerCase().includes(query)
    );
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
