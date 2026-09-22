import { Component, OnInit, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { ISSUE_PRIORITIES, ISSUE_STATUSES, IssueInput } from '../issue.model';
import { IssueService } from '../issue.service';
import { Project } from '../../projects/project.model';
import { ProjectService } from '../../projects/project.service';

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
  private readonly projectService = inject(ProjectService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly statuses = ISSUE_STATUSES;
  readonly priorities = ISSUE_PRIORITIES;

  errorMessage = '';
  isEditing = false;
  isLoading = false;
  isSaving = false;
  private issueId: number | null = null;
  projects: Project[] = [];

  readonly form = this.fb.group({
    title: ['', [Validators.required, Validators.maxLength(150)]],
    description: [''],
    status: this.fb.control<IssueInput['status']>('PENDIENTE'),
    priority: this.fb.control<IssueInput['priority']>('MEDIA'),
    projectId: this.fb.control(0),
    assigneeId: this.fb.control(0)
  });

  get title() {
    return this.form.controls.title;
  }

  ngOnInit(): void {
    this.syncAssigneeAvailability();
    this.loadProjects();
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
          priority: issue.priority,
          projectId: issue.projectId ?? 0,
          assigneeId: issue.assignee?.id ?? 0
        });
        this.syncAssigneeAvailability();
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No se pudo cargar la incidencia.';
        this.isLoading = false;
      }
    });
  }

  get selectedProject(): Project | undefined {
    return this.projects.find((project) => project.id === this.form.controls.projectId.value);
  }

  onProjectChange(): void {
    const assigneeId = this.form.controls.assigneeId.value;
    const isMember = this.selectedProject?.members.some((member) => member.id === assigneeId) ?? false;
    if (!isMember) {
      this.form.controls.assigneeId.setValue(0);
    }
    this.syncAssigneeAvailability();
  }

  /** Sin proyecto o sin miembros no hay a quién asignar: se deshabilita el control, no el `<select>`. */
  private syncAssigneeAvailability(): void {
    const hasMembers = (this.selectedProject?.members.length ?? 0) > 0;
    if (hasMembers) {
      this.form.controls.assigneeId.enable();
    } else {
      this.form.controls.assigneeId.disable();
    }
  }

  save(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.isSaving = true;
    this.errorMessage = '';
    const value = this.form.getRawValue();
    const input: IssueInput = {
      ...value,
      projectId: value.projectId > 0 ? value.projectId : null,
      assigneeId: value.assigneeId > 0 ? value.assigneeId : null
    };
    const saveRequest = this.isEditing && this.issueId !== null
      ? this.issueService.update(this.issueId, input)
      : this.issueService.create(input);

    saveRequest.subscribe({
      next: () => this.router.navigate(['/issues']),
      error: () => {
        this.errorMessage = 'No se pudo guardar la incidencia.';
        this.isSaving = false;
      }
    });
  }

  private loadProjects(): void {
    this.projectService.getAll().subscribe({
      next: (projects) => {
        this.projects = projects;
        this.syncAssigneeAvailability();
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar los proyectos.';
      }
    });
  }
}
