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
    if (this.isEditMode()) {
      this.initializeEditMode();
      return;
    }

    if (this.data?.admin?.typeAdmin === 'SITE') {
      this.typeId.set(2);
    }

    this.loadNextMatricule();
  }

  isEditMode(): boolean {
    return this.data?.mode === 'EDIT';
  }

  getTitle(): string {
    return this.isEditMode() ? 'Modifier le membre' : 'Ajouter un membre';
  }

  getSubmitLabel(): string {
    return this.isEditMode() ? 'Modifier' : 'Créer';
  }

  onTypeChange(value: number) {
    if (this.isEditMode()) {
      return;
    }

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

    if (this.isEditMode()) {
      this.dialogRef.close({
        id: this.data?.member?.id,
        matricule: this.matricule().toUpperCase(),
        firstName: this.firstName().trim(),
        lastName: this.lastName().trim(),
        email: this.email().trim()
      });
      return;
    }

    const admin = this.data?.admin;
    const isSiteAdmin = admin?.typeAdmin === 'SITE';

    this.dialogRef.close({
      matricule: this.matricule().toUpperCase(),
      firstName: this.firstName().trim(),
      lastName: this.lastName().trim(),
      email: this.email().trim(),
      type: this.buildType(),
      siteId: isSiteAdmin ? admin.siteId : null,
      active: true
    });
  }

  close() {
    this.dialogRef.close();
  }

  private initializeEditMode() {
    const member = this.data?.member;

    if (!member) {
      return;
    }

    this.typeId.set(member.type?.id ?? this.getTypeIdFromCode(member.type?.code));
    this.matricule.set(member.matricule ?? '');
    this.firstName.set(member.firstName ?? '');
    this.lastName.set(member.lastName ?? '');
    this.email.set(member.email ?? '');
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

  private getTypeIdFromCode(code: string | undefined): number {
    if (code === 'GLOBAL') {
      return 1;
    }

    if (code === 'SITE') {
      return 2;
    }

    return 3;
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
