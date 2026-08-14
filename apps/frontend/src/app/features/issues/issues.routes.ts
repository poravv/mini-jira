import { Routes } from '@angular/router';

import { IssueFormComponent } from './issue-form/issue-form.component';
import { IssueListComponent } from './issue-list/issue-list.component';

/** Rutas de la feature issues, cargadas lazy desde app.routes.ts bajo el prefijo /issues. */
export const ISSUES_ROUTES: Routes = [
  { path: '', component: IssueListComponent },
  { path: 'new', component: IssueFormComponent }
];
