import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { User, UserRole } from '../../users/user.model';
import { UserService } from '../../users/user.service';
import { UserSessionService } from '../../users/user-session.service';
import { Issue } from '../../issues/issue.model';
import { IssueService } from '../../issues/issue.service';
import { Project } from '../project.model';
import { ProjectService } from '../project.service';

@Component({
  selector: 'app-project-detail',
  imports: [DatePipe, RouterLink, ReactiveFormsModule],
  templateUrl: './project-detail.component.html',
  styleUrl: './project-detail.component.css'
})
export class ProjectDetailComponent implements OnInit {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly projectService = inject(ProjectService);
  private readonly userService = inject(UserService);
  private readonly issueService = inject(IssueService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly session = inject(UserSessionService);

  project: Project | null = null;
  availableUsers: User[] = [];
  isLoading = true;
  isEditing = false;
  isSaving = false;
  isAddingMember = false;
  removingMemberId: number | null = null;
  expandedMemberId: number | null = null;
  memberIssues: Issue[] = [];
  isLoadingMemberIssues = false;
  memberIssuesErrorMessage = '';
  errorMessage = '';
  memberErrorMessage = '';
  private projectId: number | null = null;

  readonly editForm = this.fb.group({
    name: ['', [Validators.required, Validators.maxLength(150)]],
    description: ['', [Validators.maxLength(2000)]]
  });

  readonly memberForm = this.fb.group({
    userId: this.fb.control(0, [Validators.min(1)])
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);
    if (idParam === null || !Number.isInteger(id) || id <= 0) {
      this.errorMessage = 'El proyecto solicitado no es válido.';
      this.isLoading = false;
      return;
    }

    this.projectId = id;
    this.loadProject();
    if (this.canManageMembers()) {
      this.loadAvailableUsers();
    }
  }

  canManageMembers(): boolean {
    return this.session.hasRole('ADMIN');
  }

  startEditing(): void {
    if (!this.project) {
      return;
    }
    this.editForm.setValue({
      name: this.project.name,
      description: this.project.description ?? ''
    });
    this.isEditing = true;
  }

  cancelEditing(): void {
    this.isEditing = false;
    this.editForm.reset();
  }

  save(): void {
    if (this.projectId === null || this.editForm.invalid) {
      this.editForm.markAllAsTouched();
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';
    this.projectService.update(this.projectId, {
      name: this.editForm.controls.name.value.trim(),
      description: this.editForm.controls.description.value.trim()
    }).subscribe({
      next: (project) => {
        this.project = project;
        this.isEditing = false;
        this.isSaving = false;
      },
      error: () => {
        this.errorMessage = 'No se pudo actualizar el proyecto.';
        this.isSaving = false;
      }
    });
  }

  deleteProject(): void {
    if (this.projectId === null || !this.project) {
      return;
    }
    if (!window.confirm(`¿Seguro que querés eliminar el proyecto "${this.project.name}"?`)) {
      return;
    }

    this.isSaving = true;
    this.errorMessage = '';
    this.projectService.delete(this.projectId).subscribe({
      next: () => this.router.navigate(['/proyectos']),
      error: () => {
        this.errorMessage = 'No se pudo eliminar el proyecto.';
        this.isSaving = false;
      }
    });
  }

  addMember(): void {
    if (this.projectId === null || this.memberForm.invalid) {
      this.memberForm.markAllAsTouched();
      return;
    }

    this.isAddingMember = true;
    this.memberErrorMessage = '';
    this.projectService.addMember(this.projectId, { userId: this.memberForm.controls.userId.value }).subscribe({
      next: (project) => {
        this.project = project;
        this.memberForm.reset({ userId: 0 });
        this.isAddingMember = false;
      },
      error: () => {
        this.memberErrorMessage = 'No se pudo agregar el miembro. Verificá que no esté agregado.';
        this.isAddingMember = false;
      }
    });
  }

  removeMember(user: User): void {
    if (this.projectId === null) {
      return;
    }
    if (!window.confirm(`¿Seguro que querés quitar a "${this.userName(user)}" del proyecto?`)) {
      return;
    }

    this.removingMemberId = user.id;
    this.memberErrorMessage = '';
    this.projectService.removeMember(this.projectId, user.id).subscribe({
      next: () => {
        if (this.project) {
          this.project = {
            ...this.project,
            members: this.project.members.filter((member) => member.id !== user.id)
          };
        }
        if (this.expandedMemberId === user.id) {
          this.expandedMemberId = null;
          this.memberIssues = [];
        }
        this.removingMemberId = null;
      },
      error: () => {
        this.memberErrorMessage = 'No se pudo quitar el miembro.';
        this.removingMemberId = null;
      }
    });
  }

  toggleMemberIssues(user: User): void {
    if (this.projectId === null) {
      return;
    }
    if (this.expandedMemberId === user.id) {
      this.expandedMemberId = null;
      this.memberIssues = [];
      this.memberIssuesErrorMessage = '';
      return;
    }

    this.expandedMemberId = user.id;
    this.memberIssues = [];
    this.memberIssuesErrorMessage = '';
    this.isLoadingMemberIssues = true;
    this.issueService.getAll(undefined, undefined, this.projectId, user.id).subscribe({
      next: (issues) => {
        this.memberIssues = issues;
        this.isLoadingMemberIssues = false;
      },
      error: () => {
        this.memberIssuesErrorMessage = 'No se pudieron cargar las incidencias del integrante.';
        this.isLoadingMemberIssues = false;
      }
    });
  }

  userName(user: User): string {
    return `${user.firstName} ${user.lastName}`.trim() || user.username;
  }

  roleLabel(role: UserRole): string {
    return { ADMIN: 'Administrador', SUPPORT: 'Soporte', USER: 'Usuario' }[role];
  }

  private loadProject(): void {
    if (this.projectId === null) {
      return;
    }
    this.projectService.getById(this.projectId).subscribe({
      next: (project) => {
        this.project = project;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No se pudo cargar el proyecto.';
        this.isLoading = false;
      }
    });
  }

  private loadAvailableUsers(): void {
    this.userService.getAll(true).subscribe({
      next: (users) => {
        this.availableUsers = users;
      },
      error: () => {
        this.memberErrorMessage = 'No se pudieron cargar los usuarios disponibles.';
      }
    });
  }
}
