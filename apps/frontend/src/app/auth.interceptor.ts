import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { UserSessionService } from './features/auth/user-session.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const sessionService = inject(UserSessionService);
  const router = inject(Router);
  const session = sessionService.currentUser();
  if (!session || !request.url.startsWith('/api/') || request.url === '/api/auth/login') {
    return next(request);
  }
  return next(request.clone({ setHeaders: { Authorization: `Bearer ${session.accessToken}` } })).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        sessionService.endSession();
        // Una carga puede disparar varias peticiones protegidas a la vez. Si el
        // token ya no es válido, todas responden 401; navegar una sola vez
        // evita un ciclo de navegación en navegadores que conservan la sesión.
        if (!router.url.startsWith('/auth/login')) {
          void router.navigate(['/auth/login']);
        }
      }
      return throwError(() => error);
    })
  );
};
