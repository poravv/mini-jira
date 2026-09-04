import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { UserSessionService } from './user-session.service';

export const authGuard: CanActivateFn = () => {
  const session = inject(UserSessionService);
  const router = inject(Router);
  return session.isAuthenticated() ? true : router.createUrlTree(['/users/account']);
};

export const adminGuard: CanActivateFn = () => {
  const session = inject(UserSessionService);
  const router = inject(Router);
  return session.hasRole('ADMIN') ? true : router.createUrlTree(['/issues']);
};
