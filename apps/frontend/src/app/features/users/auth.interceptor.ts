import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { UserSessionService } from './user-session.service';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const session = inject(UserSessionService);
  const router = inject(Router);
  const token = session.token();
  const isLoginRequest = request.url.endsWith('/api/auth/login') || request.url.endsWith('/api/users/login');
  const authorizedRequest = token && !isLoginRequest
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;

  return next(authorizedRequest).pipe(
    catchError((error) => {
      if (error.status === 401 && !isLoginRequest) {
        session.endSession();
        void router.navigate(['/users/account']);
      }
      return throwError(() => error);
    })
  );
};
