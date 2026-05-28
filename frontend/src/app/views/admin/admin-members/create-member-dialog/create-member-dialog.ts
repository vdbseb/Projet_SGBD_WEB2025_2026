import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-create-member-dialog',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './create-member-dialog.html'
})
export class CreateMemberDialog {
  private dialogRef = inject(MatDialogRef<CreateMemberDialog>);

  matricule = signal('');
  firstName = signal('');
  lastName = signal('');
  email = signal('');
  typeId = signal(1);

  save() {
    if (!this.matricule() || !this.firstName() || !this.lastName() || !this.email()) {
      return;
    }

    this.dialogRef.close({
      matricule: this.matricule().toUpperCase(),
      firstName: this.firstName(),
      lastName: this.lastName(),
      email: this.email(),
      typeId: this.typeId(),
      active: true
    });
  }

  close() {
    this.dialogRef.close();
  }
}
