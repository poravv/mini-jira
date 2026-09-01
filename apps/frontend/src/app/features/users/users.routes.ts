import { Routes } from '@angular/router';

import { UserFormComponent } from './user-form/user-form.component';
import { UserListComponent } from './user-list/user-list.component';
import { adminGuard } from '../../admin.guard';

export const USERS_ROUTES: Routes = [
  { path: '', component: UserListComponent, canActivate: [adminGuard] },
  { path: 'new', component: UserFormComponent, canActivate: [adminGuard] },
  { path: ':id/edit', component: UserFormComponent, canActivate: [adminGuard] }
];
