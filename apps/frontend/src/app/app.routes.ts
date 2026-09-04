import { Routes } from '@angular/router';

import { authGuard } from './features/users/auth.guard';

export const routes: Routes = [
  {
    path: 'issues',
    canActivate: [authGuard],
    loadChildren: () => import('./features/issues/issues.routes').then((m) => m.ISSUES_ROUTES)
  },
  {
    path: 'users',
    loadChildren: () => import('./features/users/users.routes').then((m) => m.USERS_ROUTES)
  },
  { path: '', redirectTo: 'issues', pathMatch: 'full' },
  { path: '**', redirectTo: 'issues' }
];
