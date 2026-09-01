import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpTesting: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(AuthService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('inicia sesión en el endpoint de autenticación', () => {
    const login = { accessToken: 'jwt', tokenType: 'Bearer' as const, userId: 1, username: 'ana', role: 'USER' as const };
    service.login({ identifier: 'ana@example.com', password: 'secreto123' }).subscribe((result) => expect(result).toEqual(login));
    const request = httpTesting.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ identifier: 'ana@example.com', password: 'secreto123' });
    request.flush(login);
  });
});
