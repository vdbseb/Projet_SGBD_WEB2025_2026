import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';
import { PadelService } from '../../../../services/padel.service';

@Component({
  selector: 'app-create-member-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './create-member-dialog.html'
})
export class CreateMemberDialog implements OnInit {
  private dialogRef = inject(MatDialogRef<CreateMemberDialog>);
  private padelService = inject(PadelService);

  matricule = signal('');
  firstName = signal('');
  lastName = signal('');
  email = signal('');
  typeId = signal(1);

  ngOnInit() {
    this.loadNextMatricule();
  }

  onTypeChange(value: number) {
    this.typeId.set(Number(value));
    this.loadNextMatricule();
  }

  save() {
    if (!this.matricule() || !this.firstName() || !this.lastName() || !this.email()) {
      return;
    }

    this.dialogRef.close({
      matricule: this.matricule().toUpperCase(),
      firstName: this.firstName(),
      lastName: this.lastName(),
      email: this.email(),
      type: this.buildType(),
      active: true
    });
  }

  close() {
    this.dialogRef.close();
  }

  private loadNextMatricule() {
    const typeCode = this.getTypeCodeFromId(this.typeId());

    this.padelService.getNextMatricule(typeCode).subscribe({
      next: (matricule: string) => {
        this.matricule.set(matricule);
      },
      error: () => {
        this.matricule.set('');
      }
    });
  }

  private getTypeCodeFromId(typeId: number): string {
    switch (Number(typeId)) {
      case 1:
        return 'GLOBAL';
      case 2:
        return 'SITE';
      case 3:
        return 'LIBRE';
      default:
        return 'GLOBAL';
    }
  }

  private buildType() {
    switch (this.typeId()) {
      case 1:
        return {
          id: 1,
          code: 'GLOBAL',
          delai_reservation_jours: 21
        };

      case 2:
        return {
          id: 2,
          code: 'SITE',
          delai_reservation_jours: 14
        };

      case 3:
        return {
          id: 3,
          code: 'LIBRE',
          delai_reservation_jours: 5
        };

      default:
        return {
          id: 1,
          code: 'GLOBAL',
          delai_reservation_jours: 21
        };
    }
  }
}
