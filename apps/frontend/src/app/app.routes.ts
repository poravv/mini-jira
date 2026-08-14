import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'issues',
    loadChildren: () => import('./features/issues/issues.routes').then((m) => m.ISSUES_ROUTES)
  },
  { path: '', redirectTo: 'issues', pathMatch: 'full' },
  { path: '**', redirectTo: 'issues' }
];
