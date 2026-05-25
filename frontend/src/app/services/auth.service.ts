import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  currentMember = signal<any | null>(null);
  currentAdmin = signal<any | null>(null);

  login(member: any) {
    this.currentMember.set(member);
  }

  logout() {
    this.currentMember.set(null);
  }

  isLoggedIn() {
    return this.currentMember() !== null;
  }

  loginAdmin(admin: any) {
    this.currentAdmin.set(admin);
  }

  logoutAdmin() {
    this.currentAdmin.set(null);
  }

  isAdminLoggedIn() {
    return this.currentAdmin() !== null;
  }

  isGlobalAdmin() {
    return this.currentAdmin()?.typeAdmin === 'GLOBAL';
  }

  isSiteAdmin() {
    return this.currentAdmin()?.typeAdmin === 'SITE';
  }
}
