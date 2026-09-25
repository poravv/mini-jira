import { HttpErrorResponse } from '@angular/common/http';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, Subject, throwError } from 'rxjs';

import { Issue } from '../issue.model';
import { IssueService } from '../issue.service';
import { IssueFormComponent } from './issue-form.component';

describe('IssueFormComponent', () => {
  let component: IssueFormComponent;
  let fixture: ComponentFixture<IssueFormComponent>;
  let issueService: jasmine.SpyObj<IssueService>;

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
    issueService = jasmine.createSpyObj<IssueService>('IssueService', ['getById', 'updateStatus', 'updatePriority']);
    issueService.getById.and.returnValue(of(issue));

    TestBed.configureTestingModule({
      imports: [IssueFormComponent],
      providers: [
        provideRouter([]),
        { provide: IssueService, useValue: issueService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap({ id: String(issue.id) }) } }
        }
      ]
    });

    fixture = TestBed.createComponent(IssueFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('envía un PATCH de estado, bloquea duplicados y actualiza el Issue al completarse', () => {
    const response = new Subject<Issue>();
    issueService.updateStatus.and.returnValue(response.asObservable());

    component.changeStatus('RESUELTA');
    component.changeStatus('RESUELTA');

    expect(issueService.updateStatus).toHaveBeenCalledOnceWith(issue.id, { status: 'RESUELTA' });
    expect(component.isUpdatingStatus).toBeTrue();
    expect(component.isWorkflowBusy).toBeTrue();

    response.next({ ...issue, status: 'RESUELTA' });
    response.complete();

    expect(component.currentStatus).toBe('RESUELTA');
    expect(component.isUpdatingStatus).toBeFalse();
  });

  it('envía un PATCH de prioridad y actualiza el Issue al completarse', () => {
    issueService.updatePriority.and.returnValue(of({ ...issue, priority: 'CRITICA' }));

    component.changePriority('CRITICA');

    expect(issueService.updatePriority).toHaveBeenCalledOnceWith(issue.id, { priority: 'CRITICA' });
    expect(component.currentPriority).toBe('CRITICA');
    expect(component.isUpdatingPriority).toBeFalse();
  });

  it('muestra un mensaje específico y resincroniza el Issue cuando el backend devuelve 409', () => {
    issueService.getById.and.returnValue(of({ ...issue, status: 'PENDIENTE', priority: 'ALTA' }));
    issueService.updateStatus.and.returnValue(throwError(() => new HttpErrorResponse({ status: 409 })));

    component.changeStatus('RESUELTA');

    expect(component.errorMessage).toContain('incidencia fue actualizada');
    expect(issueService.getById).toHaveBeenCalledTimes(2);
    expect(component.currentStatus).toBe('PENDIENTE');
    expect(component.currentPriority).toBe('ALTA');
    expect(component.isSynchronizingWorkflow).toBeFalse();
  });
});
