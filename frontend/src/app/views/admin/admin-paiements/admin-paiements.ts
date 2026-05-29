import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { DatePipe } from '@angular/common';
import { MatIconModule } from '@angular/material/icon';

import { PadelService } from '../../../services/padel.service';

@Component({
  selector: 'app-admin-paiements',
  standalone: true,
  imports: [RouterLink, DatePipe, MatIconModule],
  templateUrl: './admin-paiements.html'
})
export class AdminPaiements implements OnInit {
  private padelService = inject(PadelService);

  payments = signal<any[]>([]);
  search = signal('');
  members = signal<any[]>([]);

  ngOnInit() {
    this.loadPayments();
    this.loadMembers();
  }

  loadPayments() {
    this.padelService.getPayments().subscribe(payments => {
      console.log(payments);
      this.payments.set(payments);
    });
  }

  filteredPayments() {
    const query = this.search().toLowerCase().trim();

    if (!query) {
      return this.payments();
    }

    return this.payments().filter(payment =>
      payment.matricule?.toLowerCase().includes(query) ||
      payment.memberName?.toLowerCase().includes(query) ||
      payment.statut?.toLowerCase().includes(query) ||
      payment.methode?.toLowerCase().includes(query)
    );
  }

  getAmount(payment: any): number {
    return (payment.montantCentimes ?? payment.montant ?? 0) / 100;
  }

  getStatusClass(payment: any): string {
    switch (payment.statut) {
      case 'VALIDE':
        return 'bg-emerald-100 text-emerald-700';
      case 'REFUSE':
        return 'bg-red-100 text-red-700';
      case 'REMBOURSE':
        return 'bg-violet-100 text-violet-700';
      case 'ANNULE':
        return 'bg-slate-100 text-slate-500';
      default:
        return 'bg-orange-100 text-orange-700';
    }
  }

  getTotalPaid(): number {
    return this.payments()
      .filter(payment => payment.statut === 'VALIDE')
      .reduce((sum, payment) => sum + this.getAmount(payment), 0);
  }

  getTotalRefunded(): number {
    return this.payments()
      .filter(payment => payment.statut === 'REMBOURSE')
      .reduce((sum, payment) => sum + this.getAmount(payment), 0);
  }

  getPendingCount(): number {
    return this.payments()
      .filter(payment => payment.statut === 'EN_ATTENTE')
      .length;
  }
  loadMembers() {
    this.padelService.getMembers().subscribe(members => {
      this.members.set(members);
    });
  }

  getMember(payment: any): any | undefined {
    return this.members().find(member => member.id === payment.membreId);
  }

  getMemberName(payment: any): string {
    const member = this.getMember(payment);

    if (!member) {
      return 'Membre inconnu';
    }

    return `${member.firstName} ${member.lastName}`;
  }

  getMemberMatricule(payment: any): string {
    return this.getMember(payment)?.matricule || 'Matricule inconnu';
  }
}
