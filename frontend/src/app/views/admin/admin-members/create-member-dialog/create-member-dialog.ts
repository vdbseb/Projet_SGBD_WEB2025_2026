import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { PadelService } from '../../../../services/padel.service';

@Component({
  selector: 'app-create-member-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './create-member-dialog.html'
})
export class CreateMemberDialog implements OnInit {
  private padelService = inject(PadelService);
  private dialogRef = inject(MatDialogRef<CreateMemberDialog>);

  data = inject(MAT_DIALOG_DATA);

  typeId = signal(1);
  matricule = signal('');
  firstName = signal('');
  lastName = signal('');
  email = signal('');

  ngOnInit() {
    if (this.data?.admin?.typeAdmin === 'SITE') {
      this.typeId.set(2);
    }

    this.loadNextMatricule();
  }

  onTypeChange(value: number) {
    if (this.data?.admin?.typeAdmin === 'SITE') {
      this.typeId.set(2);
      this.loadNextMatricule();
      return;
    }

    this.typeId.set(Number(value));
    this.loadNextMatricule();
  }

  save() {
    if (!this.matricule() || !this.firstName() || !this.lastName() || !this.email()) {
      return;
    }

    const admin = this.data?.admin;
    const isSiteAdmin = admin?.typeAdmin === 'SITE';

    this.dialogRef.close({
      matricule: this.matricule().toUpperCase(),
      firstName: this.firstName(),
      lastName: this.lastName(),
      email: this.email(),
      type: this.buildType(),
      siteId: isSiteAdmin ? admin.siteId : null,
      active: true
    });
  }

  close() {
    this.dialogRef.close();
  }

  private loadNextMatricule() {
    const typeCode = this.getTypeCode();

    this.padelService.getNextMatricule(typeCode).subscribe({
      next: matricule => {
        this.matricule.set(matricule);
      }
    });
  }

  private getTypeCode(): 'GLOBAL' | 'SITE' | 'LIBRE' {
    if (this.typeId() === 1) {
      return 'GLOBAL';
    }

    if (this.typeId() === 2) {
      return 'SITE';
    }

    return 'LIBRE';
  }

  private buildType() {
    const typeCode = this.getTypeCode();

    if (typeCode === 'GLOBAL') {
      return {
        id: 1,
        code: 'GLOBAL',
        delai_reservation_jours: 21
      };
    }

    if (typeCode === 'SITE') {
      return {
        id: 2,
        code: 'SITE',
        delai_reservation_jours: 14
      };
    }

    return {
      id: 3,
      code: 'LIBRE',
      delai_reservation_jours: 5
    };
  }
}
