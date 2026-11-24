import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { UserSessionService } from './features/auth/user-session.service';

/** Evita que un usuario sin rol ADMIN entre a las pantallas de administración desde una URL directa. */
export const adminGuard: CanActivateFn = () => {
  const session = inject(UserSessionService).currentUser();
  if (session?.role === 'ADMIN') {
    return true;
  }
  return inject(Router).createUrlTree([session ? '/issues' : '/auth/login']);
};
