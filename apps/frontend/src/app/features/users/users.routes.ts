import { Routes } from '@angular/router';

import { UserFormComponent } from './user-form/user-form.component';
import { UserListComponent } from './user-list/user-list.component';
import { AccountPageComponent } from './account-page/account-page.component';

export const USERS_ROUTES: Routes = [
  { path: 'account/new', component: UserFormComponent, data: { isAccountCreation: true } },
  { path: 'account', component: AccountPageComponent },
  { path: '', component: UserListComponent },
  { path: 'new', component: UserFormComponent },
  { path: ':id/edit', component: UserFormComponent }
];
