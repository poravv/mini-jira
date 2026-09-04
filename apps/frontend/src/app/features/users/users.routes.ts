import { Routes } from '@angular/router';

import { UserFormComponent } from './user-form/user-form.component';
import { UserListComponent } from './user-list/user-list.component';
import { AccountPageComponent } from './account-page/account-page.component';
import { adminGuard, authGuard } from './auth.guard';

export const USERS_ROUTES: Routes = [
  { path: 'account/new', component: UserFormComponent, data: { isAccountCreation: true } },
  { path: 'account', component: AccountPageComponent },
  { path: '', component: UserListComponent, canActivate: [authGuard, adminGuard] },
  { path: 'new', component: UserFormComponent, canActivate: [authGuard, adminGuard] },
  { path: ':id/edit', component: UserFormComponent, canActivate: [authGuard, adminGuard] }
];
