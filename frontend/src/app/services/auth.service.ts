gir sdimport { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  currentMember = signal<any | null>(null);

  login(member: any) {
    this.currentMember.set(member);
  }

  logout() {
    this.currentMember.set(null);
  }

  isLoggedIn() {
    return this.currentMember() !== null;
  }
}
