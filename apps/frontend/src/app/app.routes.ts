import { Routes } from '@angular/router';
import { authGuard } from './auth.guard';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES)
  },
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
