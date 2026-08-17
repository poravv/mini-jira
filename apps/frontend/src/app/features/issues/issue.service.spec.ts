import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { Issue, IssueInput } from './issue.model';
import { IssueService } from './issue.service';

describe('IssueService', () => {
  let service: IssueService;
  let httpTesting: HttpTestingController;

  const issue: Issue = {
    id: 7,
    title: 'Actualizar documentación',
    description: 'Añadir guía de edición',
    status: 'EN_PROGRESO',
    priority: 'MEDIA',
    createdAt: '2026-08-17T10:00:00Z',
    updatedAt: '2026-08-17T10:00:00Z'
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(IssueService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('obtiene una incidencia por id para rellenar el formulario de edición', () => {
    service.getById(7).subscribe((result) => expect(result).toEqual(issue));

    const request = httpTesting.expectOne('/api/issues/7');
    expect(request.request.method).toBe('GET');
    request.flush(issue);
  });

  it('actualiza una incidencia con PUT', () => {
    const input: IssueInput = {
      title: issue.title,
      description: issue.description,
      status: issue.status,
      priority: issue.priority
    };
    service.update(7, input).subscribe((result) => expect(result).toEqual(issue));

    const request = httpTesting.expectOne('/api/issues/7');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual(input);
    request.flush(issue);
  });
});
