import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ISSUE_PRIORITIES, ISSUE_STATUSES, IssueInput } from '../issue.model';
import { IssueService } from '../issue.service';

/** Formulario de alta (/issues/new): valida con Reactive Forms, crea vía IssueService y vuelve al listado. */
@Component({
  selector: 'app-issue-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './issue-form.component.html',
  styleUrl: './issue-form.component.css'
})
export class IssueFormComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly issueService = inject(IssueService);
  private readonly router = inject(Router);

  readonly statuses = ISSUE_STATUSES;
  readonly priorities = ISSUE_PRIORITIES;

  errorMessage = '';
  isSaving = false;

  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: [''],
    status: this.fb.control<IssueInput['status']>('PENDIENTE'),
    priority: this.fb.control<IssueInput['priority']>('MEDIA')
  });

  get title() {
    return this.form.controls.title;
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSaving = true;
    this.errorMessage = '';
    this.issueService.create(this.form.getRawValue()).subscribe({
      next: () => this.router.navigate(['/issues']),
      error: () => {
        this.errorMessage = 'No se pudo guardar la incidencia.';
        this.isSaving = false;
      }
    });
  }
}
