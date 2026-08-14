import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Issue } from '../issue.model';
import { IssueService } from '../issue.service';

/** Pantalla de listado (/issues): pide las incidencias a IssueService y muestra tabla, carga o error. */
@Component({
  selector: 'app-issue-list',
  imports: [RouterLink, DatePipe],
  templateUrl: './issue-list.component.html',
  styleUrl: './issue-list.component.css'
})
export class IssueListComponent implements OnInit {
  private readonly issueService = inject(IssueService);

  issues: Issue[] = [];
  isLoading = true;
  errorMessage = '';

  ngOnInit(): void {
    this.loadIssues();
  }

  loadIssues(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.issueService.getAll().subscribe({
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

}
