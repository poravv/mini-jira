import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { Issue, IssueInput } from './issue.model';

/**
 * Cliente HTTP del recurso issues: única pieza que habla con el backend (/api/issues,
 * ruta relativa que resuelven el proxy de dev o nginx). Los componentes solo consumen estos métodos.
 */
@Injectable({ providedIn: 'root' })
export class IssueService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/issues';

  getAll(): Observable<Issue[]> {
    return this.http.get<Issue[]>(this.baseUrl);
  }

  create(input: IssueInput): Observable<Issue> {
    return this.http.post<Issue>(this.baseUrl, input);
  }
}
