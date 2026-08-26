import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { User, UserCreateInput, UserLoginInput, UserUpdateInput } from './user.model';

@Injectable({ providedIn: 'root' })
export class UserService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/users';

  getAll(isActive?: boolean): Observable<User[]> {
    let params = new HttpParams();
    if (isActive !== undefined) {
      params = params.set('active', isActive);
    }
    return this.http.get<User[]>(this.baseUrl, { params });
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/${id}`);
  }

  login(input: UserLoginInput): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/login`, input);
  }

  create(input: UserCreateInput): Observable<User> {
    return this.http.post<User>(this.baseUrl, input);
  }

  update(id: number, input: UserUpdateInput): Observable<User> {
    return this.http.put<User>(`${this.baseUrl}/${id}`, input);
  }

  updateStatus(id: number, isActive: boolean): Observable<User> {
    return this.http.patch<User>(`${this.baseUrl}/${id}/status`, { isActive });
  }
}
