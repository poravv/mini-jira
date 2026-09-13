import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { AddProjectMemberInput, Project, ProjectInput } from './project.model';

@Injectable({ providedIn: 'root' })
export class ProjectService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/proyectos';

  getAll(): Observable<Project[]> {
    return this.http.get<Project[]>(this.baseUrl);
  }

  getById(id: number): Observable<Project> {
    return this.http.get<Project>(`${this.baseUrl}/${id}`);
  }

  create(input: ProjectInput): Observable<Project> {
    return this.http.post<Project>(this.baseUrl, input);
  }

  update(id: number, input: ProjectInput): Observable<Project> {
    return this.http.put<Project>(`${this.baseUrl}/${id}`, input);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  addMember(projectId: number, input: AddProjectMemberInput): Observable<Project> {
    return this.http.post<Project>(`${this.baseUrl}/${projectId}/miembros`, input);
  }

  removeMember(projectId: number, userId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${projectId}/miembros/${userId}`);
  }
}
