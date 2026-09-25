import { Component, OnInit, inject } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { IssueInput, IssuePriority, IssueStatus } from '../issue.model';
import { IssueService } from '../issue.service';
import { getAllowedPriorityTransitions, getAllowedStatusTransitions } from '../issue-transition.rules';

/** Formulario de alta y edición: carga la incidencia al editarla y vuelve al listado al guardar. */
@Component({
  selector: 'app-issue-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './issue-form.component.html',
  styleUrl: './issue-form.component.css'
})
export class IssueFormComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly issueService = inject(IssueService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  errorMessage = '';
  isEditing = false;
  isLoading = false;
  isSaving = false;
  isUpdatingStatus = false;
  isUpdatingPriority = false;
  isSynchronizingWorkflow = false;
  private issueId: number | null = null;

  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: [''],
    status: this.fb.control<IssueInput['status']>('PENDIENTE'),
    priority: this.fb.control<IssueInput['priority']>('MEDIA')
  });

  get title() {
    return this.form.controls.title;
  }

  get currentStatus(): IssueStatus {
    return this.form.controls.status.value;
  }

  get currentPriority(): IssuePriority {
    return this.form.controls.priority.value;
  }

  get isWorkflowBusy(): boolean {
    return this.isSaving || this.isUpdatingStatus || this.isUpdatingPriority || this.isSynchronizingWorkflow;
  }

  get allowedStatusChanges(): readonly IssueStatus[] {
    return getAllowedStatusTransitions(this.currentStatus);
  }

  get allowedPriorityChanges(): readonly IssuePriority[] {
    return getAllowedPriorityTransitions(this.currentPriority);
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam === null) {
      return;
    }

    const id = Number(idParam);
    if (!Number.isInteger(id) || id <= 0) {
      this.errorMessage = 'La incidencia solicitada no es válida.';
      return;
    }

    this.isEditing = true;
    this.issueId = id;
    this.isLoading = true;
    this.issueService.getById(id).subscribe({
      next: (issue) => {
        this.form.setValue({
          title: issue.title,
          description: issue.description ?? '',
          status: issue.status,
          priority: issue.priority
        });
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No se pudo cargar la incidencia.';
        this.isLoading = false;
      }
    });
  }

  save(): void {
    if (this.isWorkflowBusy) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSaving = true;
    this.errorMessage = '';
    const { title, description } = this.form.getRawValue();
    const saveRequest = this.isEditing && this.issueId !== null
      ? this.issueService.update(this.issueId, { title, description })
      : this.issueService.create(this.form.getRawValue());

    saveRequest.subscribe({
      next: () => this.router.navigate(['/issues']),
      error: () => {
        this.errorMessage = 'No se pudo guardar la incidencia.';
        this.isSaving = false;
      }
    });
  }

  changeStatus(status: IssueStatus): void {
    if (this.issueId === null || this.isWorkflowBusy || !this.allowedStatusChanges.includes(status)) {
      return;
    }

    this.isUpdatingStatus = true;
    this.errorMessage = '';
    this.issueService.updateStatus(this.issueId, { status }).subscribe({
      next: (issue) => {
        this.form.controls.status.setValue(issue.status);
        this.isUpdatingStatus = false;
      },
      error: (error: unknown) => {
        this.isUpdatingStatus = false;
        this.handleWorkflowError(error, 'estado');
      }
    });
  }

  changePriority(priority: IssuePriority): void {
    if (this.issueId === null || this.isWorkflowBusy || !this.allowedPriorityChanges.includes(priority)) {
      return;
    }

    this.isUpdatingPriority = true;
    this.errorMessage = '';
    this.issueService.updatePriority(this.issueId, { priority }).subscribe({
      next: (issue) => {
        this.form.controls.priority.setValue(issue.priority);
        this.isUpdatingPriority = false;
      },
      error: (error: unknown) => {
        this.isUpdatingPriority = false;
        this.handleWorkflowError(error, 'prioridad');
      }
    });
  }

  private handleWorkflowError(error: unknown, field: 'estado' | 'prioridad'): void {
    if (error instanceof HttpErrorResponse && error.status === 409) {
      this.errorMessage = `No se pudo cambiar el ${field} porque la incidencia fue actualizada. Se actualizarán los valores vigentes.`;
      this.synchronizeWorkflow();
      return;
    }

    this.errorMessage = `No se pudo cambiar el ${field}.`;
  }

  /** Obtiene únicamente los valores del workflow para no sobrescribir cambios de texto sin guardar. */
  private synchronizeWorkflow(): void {
    if (this.issueId === null || this.isSynchronizingWorkflow) {
      return;
    }

    this.isSynchronizingWorkflow = true;
    this.issueService.getById(this.issueId).subscribe({
      next: (issue) => {
        this.form.controls.status.setValue(issue.status);
        this.form.controls.priority.setValue(issue.priority);
        this.isSynchronizingWorkflow = false;
      },
      error: () => {
        this.errorMessage = `${this.errorMessage} No se pudo sincronizar la incidencia.`;
        this.isSynchronizingWorkflow = false;
      }
    });
  }
}
