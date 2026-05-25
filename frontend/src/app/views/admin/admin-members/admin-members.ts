import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { PadelService } from '../../../services/padel.service';

@Component({
  selector: 'app-admin-members',
  standalone: true,
  imports: [RouterLink, MatIconModule],
  templateUrl: './admin-members.html'
})
export class AdminMembers implements OnInit {
  private padelService = inject(PadelService);

  members = signal<any[]>([]);
  search = signal('');

  ngOnInit() {
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
}
