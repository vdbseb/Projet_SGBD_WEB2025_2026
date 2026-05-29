import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { MatDialog } from '@angular/material/dialog';

import { PadelService } from '../../../services/padel.service';
import { ConfirmDialogComponent } from '../../confirm-dialog/confirm-dialog';

@Component({
  selector: 'app-admin-paiements',
  standalone: true,
  imports: [RouterLink, DatePipe, MatIconModule, FormsModule],
  templateUrl: './admin-paiements.html'
})
export class AdminPaiements implements OnInit {

  private padelService = inject(PadelService);
  private snackBar = inject(MatSnackBar);
  private dialog = inject(MatDialog);

  payments = signal<any[]>([]);
  members = signal<any[]>([]);

  search = signal('');
  selectedStatus = signal<'ALL' | 'EN_ATTENTE' | 'VALIDE' | 'REFUSE' | 'REMBOURSE' | 'ANNULE'>('ALL');
  selectedMethod = signal<string>('ALL');
  viewMode = signal<'cards' | 'table'>('cards');

  ngOnInit() {
    this.loadPayments();
    this.loadMembers();
  }

  private loadPayments() {
    this.padelService.getPayments().subscribe(payments => {
      this.payments.set(payments);
    });
  }

  private loadMembers() {
    this.padelService.getMembers().subscribe(members => {
      this.members.set(members);
    });
  }

  filteredPayments() {
    const query = this.search().toLowerCase().trim();
    const selectedStatus = this.selectedStatus();
    const selectedMethod = this.selectedMethod();

    return this.payments().filter(payment => {
      const matchesSearch =
        !query ||
        this.getMemberMatricule(payment).toLowerCase().includes(query) ||
        this.getMemberName(payment).toLowerCase().includes(query) ||
        this.getStatusLabel(payment).toLowerCase().includes(query) ||
        this.getMethodLabel(payment).toLowerCase().includes(query) ||
        this.getPaymentTypeLabel(payment).toLowerCase().includes(query) ||
        String(payment.reservationId || '').includes(query);

      const matchesStatus =
        selectedStatus === 'ALL' ||
        this.getPaymentStatus(payment) === selectedStatus;

      const matchesMethod =
        selectedMethod === 'ALL' ||
        this.getMethodLabel(payment) === selectedMethod;

      return matchesSearch && matchesStatus && matchesMethod;
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
    this.selectedStatus.set('ALL');
    this.selectedMethod.set('ALL');
  }

  availableMethods(): string[] {
    return [...new Set(
      this.payments()
        .map(payment => this.getMethodLabel(payment))
        .filter(method => method !== 'Non renseignée')
    )];
  }

  getAmount(payment: any): number {
    return (payment.montantCentimes ?? 0) / 100;
  }

  getPaymentStatus(payment: any): 'EN_ATTENTE' | 'VALIDE' | 'REFUSE' | 'REMBOURSE' | 'ANNULE' {
    return payment.statut || 'EN_ATTENTE';
  }

  getStatusLabel(payment: any): string {
    const status = this.getPaymentStatus(payment);

    switch (status) {
      case 'EN_ATTENTE':
        return 'En attente';

      case 'VALIDE':
        return 'Validé';

      case 'REFUSE':
        return 'Refusé';

      case 'REMBOURSE':
        return 'Remboursé';

      case 'ANNULE':
        return 'Annulé';

      default:
        return status;
    }
  }

  getStatusClass(payment: any): string {
    switch (this.getPaymentStatus(payment)) {
      case 'VALIDE':
        return 'bg-emerald-100 text-emerald-700';

      case 'REFUSE':
        return 'bg-red-100 text-red-700';

      case 'REMBOURSE':
        return 'bg-violet-100 text-violet-700';

      case 'ANNULE':
        return 'bg-slate-100 text-slate-500';

      case 'EN_ATTENTE':
      default:
        return 'bg-orange-100 text-orange-700';
    }
  }

  getMethodLabel(payment: any): string {
    return payment.methode || 'Non renseignée';
  }

  getPaymentTypeLabel(payment: any): string {
    return payment.participationId ? 'Participation' : 'Réservation';
  }

  getPaymentDate(payment: any): any {
    return payment.datePaiement || payment.dateCreation;
  }

  getTotalPaid(): number {
    return this.payments()
      .filter(payment => this.getPaymentStatus(payment) === 'VALIDE')
      .reduce((sum, payment) => sum + this.getAmount(payment), 0);
  }

  getTotalRefunded(): number {
    return this.payments()
      .filter(payment => this.getPaymentStatus(payment) === 'REMBOURSE')
      .reduce((sum, payment) => sum + this.getAmount(payment), 0);
  }

  getNetAmount(): number {
    return this.getTotalPaid() - this.getTotalRefunded();
  }

  getPendingCount(): number {
    return this.payments()
      .filter(payment => this.getPaymentStatus(payment) === 'EN_ATTENTE')
      .length;
  }

  getRefusedCount(): number {
    return this.payments()
      .filter(payment => this.getPaymentStatus(payment) === 'REFUSE')
      .length;
  }

  getMember(payment: any): any | undefined {
    return this.members()
      .find(member => member.id === payment.membreId);
  }

  getMemberName(payment: any): string {
    const member = this.getMember(payment);

    if (!member) {
      return 'Membre inconnu';
    }

    return `${member.firstName} ${member.lastName}`;
  }

  getMemberMatricule(payment: any): string {
    return this.getMember(payment)?.matricule ?? 'Matricule inconnu';
  }

  canConfirm(payment: any): boolean {
    return this.getPaymentStatus(payment) === 'EN_ATTENTE';
  }

  canRefuse(payment: any): boolean {
    return this.getPaymentStatus(payment) === 'EN_ATTENTE';
  }

  canRefund(payment: any): boolean {
    return this.getPaymentStatus(payment) === 'VALIDE';
  }

  confirmPayment(payment: any) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Confirmer le paiement',
        message: `Confirmer le paiement de ${this.getAmount(payment)}€ pour ${this.getMemberName(payment)} ?`,
        confirmLabel: 'Confirmer',
        cancelLabel: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.padelService.confirmPayment(payment.id).subscribe({
        next: () => {
          this.loadPayments();

          this.snackBar.open(
            'Paiement confirmé.',
            'OK',
            { duration: 3000 }
          );
        },
        error: () => {
          this.snackBar.open(
            'Impossible de confirmer le paiement.',
            'OK',
            { duration: 4000 }
          );
        }
      });
    });
  }

  refusePayment(payment: any) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Refuser le paiement',
        message: `Refuser le paiement de ${this.getAmount(payment)}€ pour ${this.getMemberName(payment)} ?`,
        confirmLabel: 'Refuser',
        cancelLabel: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.padelService.refusePayment(payment.id).subscribe({
        next: () => {
          this.loadPayments();

          this.snackBar.open(
            'Paiement refusé.',
            'OK',
            { duration: 3000 }
          );
        },
        error: () => {
          this.snackBar.open(
            'Impossible de refuser le paiement.',
            'OK',
            { duration: 4000 }
          );
        }
      });
    });
  }

  refundPayment(payment: any) {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Rembourser le paiement',
        message: `Rembourser le paiement de ${this.getAmount(payment)}€ pour ${this.getMemberName(payment)} ?`,
        confirmLabel: 'Rembourser',
        cancelLabel: 'Annuler'
      }
    });

    dialogRef.afterClosed().subscribe(confirmed => {
      if (!confirmed) {
        return;
      }

      this.padelService.refundPayment(payment.id).subscribe({
        next: () => {
          this.loadPayments();

          this.snackBar.open(
            'Remboursement effectué.',
            'OK',
            { duration: 3000 }
          );
        },
        error: () => {
          this.snackBar.open(
            'Impossible d’effectuer le remboursement.',
            'OK',
            { duration: 4000 }
          );
        }
      });
    });
  }
}
