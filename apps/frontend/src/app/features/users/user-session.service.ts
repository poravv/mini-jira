import { Injectable, signal } from '@angular/core';

import { AuthResponse, User } from './user.model';

const SESSION_KEY = 'mini-jira-auth-session';

interface StoredSession {
  token: string;
  user: User;
}

@Injectable({ providedIn: 'root' })
export class UserSessionService {
  private readonly storedSession = this.readStoredSession();
  readonly currentUser = signal<User | null>(this.storedSession?.user ?? null);
  readonly token = signal<string | null>(this.storedSession?.token ?? null);

  startSession(auth: AuthResponse): void {
    const session: StoredSession = { token: auth.token, user: auth.user };
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    this.currentUser.set(auth.user);
    this.token.set(auth.token);
  }

  endSession(): void {
    localStorage.removeItem(SESSION_KEY);
    this.currentUser.set(null);
    this.token.set(null);
  }

  isAuthenticated(): boolean {
    const token = this.token();
    if (!token) {
      return false;
    }

    const expiration = this.readTokenExpiration(token);
    if (expiration !== null && expiration <= Math.floor(Date.now() / 1000)) {
      this.endSession();
      return false;
    }
    return true;
  }

  hasRole(role: User['role']): boolean {
    return this.isAuthenticated() && this.currentUser()?.role === role;
  }

  private readStoredSession(): StoredSession | null {
    const storedSession = localStorage.getItem(SESSION_KEY);
    if (!storedSession) {
      return null;
    }

    try {
      const session = JSON.parse(storedSession) as StoredSession;
      if (!session.token || !session.user) {
        throw new Error('Invalid session');
      }
      return session;
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }

  private readTokenExpiration(token: string): number | null {
    try {
      const payload = token.split('.')[1];
      if (!payload) {
        return null;
      }
      const decoded = JSON.parse(atob(payload.replace(/-/g, '+').replace(/_/g, '/'))) as { exp?: number };
      return typeof decoded.exp === 'number' ? decoded.exp : null;
    } catch {
      return null;
    }
  }
}
