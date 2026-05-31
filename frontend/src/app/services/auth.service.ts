import { Injectable, signal, inject } from '@angular/core';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private router = inject(Router);

  currentMember = signal<any | null>(null);
  currentAdmin = signal<any | null>(null);

  login(member: any) {
    this.currentMember.set(member);
    this.currentAdmin.set(null);
  }

  logout() {
    this.currentMember.set(null);
    this.currentAdmin.set(null);
    this.router.navigate(['/']);
  }

  isLoggedIn() {
    return this.currentMember() !== null;
  }

  loginAdmin(admin: any) {
    this.currentAdmin.set(admin);
    this.currentMember.set(null);
  }

  logoutAdmin() {
    this.currentAdmin.set(null);
    this.currentMember.set(null);
    this.router.navigate(['/']);
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
