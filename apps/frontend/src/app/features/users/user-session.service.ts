import { Injectable, signal } from '@angular/core';

import { User } from './user.model';

const SESSION_KEY = 'mini-jira-current-user';

@Injectable({ providedIn: 'root' })
export class UserSessionService {
  readonly currentUser = signal<User | null>(this.readStoredUser());

  startSession(user: User): void {
    localStorage.setItem(SESSION_KEY, JSON.stringify(user));
    this.currentUser.set(user);
  }

  endSession(): void {
    localStorage.removeItem(SESSION_KEY);
    this.currentUser.set(null);
  }

  private readStoredUser(): User | null {
    const storedUser = localStorage.getItem(SESSION_KEY);
    if (!storedUser) {
      return null;
    }

    try {
      return JSON.parse(storedUser) as User;
    } catch {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
  }
}
