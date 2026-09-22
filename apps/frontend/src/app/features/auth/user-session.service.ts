import { Injectable, signal } from '@angular/core';

import { LoginResponse, UserSession } from './auth.model';

const SESSION_KEY = 'mini-jira-current-user';

@Injectable({ providedIn: 'root' })
export class UserSessionService {
  readonly currentUser = signal<UserSession | null>(this.readStoredUser());

  startSession(login: LoginResponse): void {
    const session: UserSession = {
      accessToken: login.accessToken,
      userId: login.userId,
      username: login.username,
      role: login.role
    };
    localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    this.currentUser.set(session);
  }

  endSession(): void {
    localStorage.removeItem(SESSION_KEY);
    this.currentUser.set(null);
  }

  private readStoredUser(): UserSession | null {
    const storedUser = localStorage.getItem(SESSION_KEY);
    if (!storedUser) {
      return null;
    }
    try {
      const session = JSON.parse(storedUser) as UserSession;
      return session.accessToken && session.userId && session.username && session.role ? session : null;
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }
}
