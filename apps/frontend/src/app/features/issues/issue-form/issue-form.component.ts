import { Component, OnInit, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ISSUE_PRIORITIES, ISSUE_STATUSES, IssueInput } from '../issue.model';
import { IssueService } from '../issue.service';

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

  readonly statuses = ISSUE_STATUSES;
  readonly priorities = ISSUE_PRIORITIES;

  errorMessage = '';
  isEditing = false;
  isLoading = false;
  isSaving = false;
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
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSaving = true;
    this.errorMessage = '';
    const saveRequest = this.isEditing && this.issueId !== null
      ? this.issueService.update(this.issueId, this.form.getRawValue())
      : this.issueService.create(this.form.getRawValue());

    saveRequest.subscribe({
      next: () => this.router.navigate(['/issues']),
      error: () => {
        this.errorMessage = 'No se pudo guardar la incidencia.';
        this.isSaving = false;
      }
    });
  }
}
