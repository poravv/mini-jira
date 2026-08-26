import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { ISSUE_PRIORITIES, ISSUE_STATUSES, Issue, IssuePriority, IssueStatus } from '../issue.model';
import { IssueService } from '../issue.service';
import { UserSessionService } from '../../users/user-session.service';

/** Pantalla de listado (/issues): pide las incidencias a IssueService y muestra tabla, carga o error. */
@Component({
  selector: 'app-issue-list',
  imports: [RouterLink, DatePipe, FormsModule],
  templateUrl: './issue-list.component.html',
  styleUrl: './issue-list.component.css'
})
export class IssueListComponent implements OnInit {
  private readonly issueService = inject(IssueService);
  readonly session = inject(UserSessionService);

  readonly statuses = ISSUE_STATUSES;
  readonly priorities = ISSUE_PRIORITIES;

  issues: Issue[] = [];
  isLoading = true;
  deletingIssueId: number | null = null;
  errorMessage = '';
  status: IssueStatus | '' = '';
  priority: IssuePriority | '' = '';

  ngOnInit(): void {
    if (!this.session.currentUser()) {
      this.isLoading = false;
      return;
    }
    this.loadIssues();
  }

  loadIssues(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.issueService.getAll(this.status || undefined, this.priority || undefined).subscribe({
      next: (issues) => {
        this.issues = issues;
        this.isLoading = false;
      },
      error: () => {
        this.errorMessage = 'No se pudieron cargar las incidencias.';
        this.isLoading = false;
      }
    });
  }

  deleteIssue(issue: Issue): void {
    const shouldDelete = window.confirm(`¿Seguro que querés eliminar la incidencia "${issue.title}"?`);
    if (!shouldDelete) {
      return;
    }

    this.deletingIssueId = issue.id;
    this.errorMessage = '';
    this.issueService.delete(issue.id).subscribe({
      next: () => {
        this.issues = this.issues.filter((currentIssue) => currentIssue.id !== issue.id);
        this.deletingIssueId = null;
      },
      error: () => {
        this.errorMessage = 'No se pudo eliminar la incidencia.';
        this.deletingIssueId = null;
      }
    });
  }
}
