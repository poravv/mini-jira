import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { AuthResponse } from './user.model';
import { authInterceptor } from './auth.interceptor';
import { UserSessionService } from './user-session.service';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let session: UserSessionService;

  const auth: AuthResponse = {
    token: 'signed.jwt.token',
    tokenType: 'Bearer',
    expiresIn: 3600,
    user: {
      id: 7,
      username: 'ana',
      email: 'ana@example.com',
      firstName: 'Ana',
      lastName: 'Admin',
      isActive: true,
      role: 'USER',
      createdAt: '2026-08-25T10:00:00Z',
      updatedAt: '2026-08-25T10:00:00Z'
    }
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    session = TestBed.inject(UserSessionService);
  });

  afterEach(() => {
    httpTesting.verify();
    localStorage.clear();
  });

  it('agrega el Bearer token a las peticiones protegidas', () => {
    session.startSession(auth);
    http.get('/api/issues').subscribe();

    const request = httpTesting.expectOne('/api/issues');
    expect(request.request.headers.get('Authorization')).toBe('Bearer signed.jwt.token');
    request.flush([]);
  });

  it('limpia la sesión cuando la API devuelve 401', () => {
    session.startSession(auth);
    http.get('/api/issues').subscribe({ error: () => undefined });

    const request = httpTesting.expectOne('/api/issues');
    request.flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(session.token()).toBeNull();
    expect(session.currentUser()).toBeNull();
  });
});
