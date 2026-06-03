import { Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { LoginResponse } from '../models/translation.model';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'nexus_token';
  private readonly USER_KEY  = 'nexus_user';

  roles = signal<string[]>([]);
  username = signal<string | null>(null);
  allowedLocales = signal<string[]>([]);

  constructor(private router: Router) {
    const stored = localStorage.getItem(this.USER_KEY);
    if (stored) {
      const user = JSON.parse(stored) as { roles?: string[]; role?: string; username: string; allowedLocales?: string[] };
      // Fallback for older sessions with single role property
      const rolesList = user.roles || (user.role ? [user.role] : []);
      this.roles.set(rolesList);
      this.username.set(user.username);
      this.allowedLocales.set(user.allowedLocales || []);
    }
  }

  saveSession(response: LoginResponse): void {
    localStorage.setItem(this.TOKEN_KEY, response.token);
    localStorage.setItem(this.USER_KEY, JSON.stringify({
      roles: response.roles,
      username: response.username,
      allowedLocales: response.allowedLocales
    }));
    this.roles.set(response.roles || []);
    this.username.set(response.username);
    this.allowedLocales.set(response.allowedLocales || []);
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  isReviewer(): boolean {
    return this.roles().includes('REVIEWER');
  }

  isManager(): boolean {
    return this.roles().includes('MANAGER');
  }

  isAdmin(): boolean {
    return this.roles().includes('ADMIN');
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.roles.set([]);
    this.username.set(null);
    this.allowedLocales.set([]);
    this.router.navigate(['/login']);
  }
}
