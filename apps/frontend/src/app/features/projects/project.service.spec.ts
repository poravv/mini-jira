import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Project, ProjectInput } from './project.model';
import { ProjectService } from './project.service';

describe('ProjectService', () => {
  let service: ProjectService;
  let httpTesting: HttpTestingController;

  const project: Project = {
    id: 7,
    name: 'Portal interno',
    description: 'Proyecto del equipo',
    createdAt: '2026-09-12T10:00:00Z',
    updatedAt: '2026-09-12T10:00:00Z',
    members: []
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(ProjectService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('lista los proyectos', () => {
    service.getAll().subscribe((result) => expect(result).toEqual([project]));

    const request = httpTesting.expectOne('/api/proyectos');
    expect(request.request.method).toBe('GET');
    request.flush([project]);
  });

  it('crea un proyecto con POST', () => {
    const input: ProjectInput = { name: project.name, description: project.description ?? '' };
    service.create(input).subscribe((result) => expect(result).toEqual(project));

    const request = httpTesting.expectOne('/api/proyectos');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(input);
    request.flush(project);
  });

  it('agrega un miembro existente al proyecto', () => {
    service.addMember(7, { userId: 3 }).subscribe((result) => expect(result).toEqual(project));

    const request = httpTesting.expectOne('/api/proyectos/7/miembros');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ userId: 3 });
    request.flush(project);
  });

  it('quita un miembro con DELETE', () => {
    service.removeMember(7, 3).subscribe();

    const request = httpTesting.expectOne('/api/proyectos/7/miembros/3');
    expect(request.request.method).toBe('DELETE');
    request.flush(null);
  });
});
