import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

import { UserSessionService } from './features/auth/user-session.service';

export const authGuard: CanActivateFn = () => {
  const session = inject(UserSessionService);
  return session.currentUser() ? true : inject(Router).createUrlTree(['/auth/login']);
};
