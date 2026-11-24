import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';

import { UserSessionService } from './features/auth/user-session.service';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let httpTesting: HttpTestingController;
  let router: Router;
  let session: UserSessionService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting()
      ]
    });
    http = TestBed.inject(HttpClient);
    httpTesting = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    session = TestBed.inject(UserSessionService);
    session.startSession({ accessToken: 'expired-token', tokenType: 'Bearer', userId: 1, username: 'ana', role: 'USER' });
  });

  afterEach(() => {
    session.endSession();
    httpTesting.verify();
  });

  it('no vuelve a navegar al login si ya está en esa ruta después de un 401', () => {
    spyOnProperty(router, 'url', 'get').and.returnValue('/auth/login');
    const navigate = spyOn(router, 'navigate');

    http.get('/api/issues').subscribe({ error: () => undefined });
    httpTesting.expectOne('/api/issues').flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(session.currentUser()).toBeNull();
    expect(navigate).not.toHaveBeenCalled();
  });
});
