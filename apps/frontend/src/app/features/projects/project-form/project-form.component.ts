import { Component, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { ProjectService } from '../project.service';

@Component({
  selector: 'app-project-form',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './project-form.component.html',
  styleUrl: './project-form.component.css'
})
export class ProjectFormComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly projectService = inject(ProjectService);
  private readonly router = inject(Router);

  isSaving = false;
  errorMessage = '';

  readonly form = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.maxLength(2000)]]
  });

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';
    this.projectService.create({
      name: this.form.controls.name.value.trim(),
      description: this.form.controls.description.value.trim()
    }).subscribe({
      next: (project) => this.router.navigate(['/proyectos', project.id]),
      error: () => {
        this.errorMessage = 'No se pudo crear el proyecto.';
        this.isSaving = false;
      }
    });
  }
}
