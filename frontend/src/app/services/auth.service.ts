import { Injectable, signal, inject, computed } from '@angular/core';
import { Router } from '@angular/router';

const ADMIN_STORAGE_KEY = 'padel_current_admin';
const ADMIN_TOKEN_STORAGE_KEY = 'padel_admin_token';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private router = inject(Router);

  currentMember = signal<any | null>(null);
  currentAdmin = signal<any | null>(this.readStoredAdmin());
  adminToken = signal<string | null>(this.readStoredAdminToken());
  sessionVersion = signal(0);

  readonly isAuthenticated = computed(() =>
    this.currentMember() !== null || this.currentAdmin() !== null
  );

  login(member: any) {
    this.currentMember.set(member);
    this.clearAdminSession();
    this.touchSession();
  }

  logout() {
    this.currentMember.set(null);
    this.clearAdminSession();
    this.touchSession();
    this.router.navigate(['/']);
  }

  isLoggedIn() {
    return this.currentMember() !== null;
  }

  loginAdmin(admin: any, token: string) {
    this.currentMember.set(null);
    this.currentAdmin.set(admin);
    this.adminToken.set(token);

    localStorage.setItem(ADMIN_STORAGE_KEY, JSON.stringify(admin));
    localStorage.setItem(ADMIN_TOKEN_STORAGE_KEY, token);

    this.touchSession();
  }

  logoutAdmin() {
    this.clearAdminSession();
    this.currentMember.set(null);
    this.touchSession();
    this.router.navigate(['/']);
  }

  clearSession() {
    this.currentMember.set(null);
    this.clearAdminSession();
    this.touchSession();
    this.router.navigate(['/']);
  }

  isAdminLoggedIn() {
    return this.currentAdmin() !== null && this.adminToken() !== null;
  }

  isGlobalAdmin() {
    return this.currentAdmin()?.typeAdmin === 'GLOBAL';
  }

  isSiteAdmin() {
    return this.currentAdmin()?.typeAdmin === 'SITE';
  }

  getAdminToken() {
    return this.adminToken();
  }

  private clearAdminSession() {
    this.currentAdmin.set(null);
    this.adminToken.set(null);
    localStorage.removeItem(ADMIN_STORAGE_KEY);
    localStorage.removeItem(ADMIN_TOKEN_STORAGE_KEY);
  }

  private readStoredAdmin(): any | null {
    const rawAdmin = localStorage.getItem(ADMIN_STORAGE_KEY);

    if (!rawAdmin) {
      return null;
    }

    try {
      return JSON.parse(rawAdmin);
    } catch {
      localStorage.removeItem(ADMIN_STORAGE_KEY);
      return null;
    }
  }

  private readStoredAdminToken(): string | null {
    return localStorage.getItem(ADMIN_TOKEN_STORAGE_KEY);
  }

  private touchSession() {
    this.sessionVersion.update(version => version + 1);
  }
}
