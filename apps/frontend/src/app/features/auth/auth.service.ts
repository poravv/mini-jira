import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { LoginInput, LoginResponse } from './auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);

  login(input: LoginInput): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', input);
  }
}
