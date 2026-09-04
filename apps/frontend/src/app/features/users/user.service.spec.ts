import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { AuthResponse, User, UserCreateInput } from './user.model';
import { UserService } from './user.service';

describe('UserService', () => {
  let service: UserService;
  let httpTesting: HttpTestingController;

  const user: User = {
    id: 7,
    username: 'ana',
    email: 'ana@example.com',
    firstName: 'Ana',
    lastName: 'Admin',
    isActive: true,
    role: 'ADMIN',
    createdAt: '2026-08-25T10:00:00Z',
    updatedAt: '2026-08-25T10:00:00Z'
  };

  const auth: AuthResponse = { token: 'signed.jwt.token', tokenType: 'Bearer', expiresIn: 3600, user };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()]
    });
    service = TestBed.inject(UserService);
    httpTesting = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpTesting.verify());

  it('obtiene usuarios filtrando por estado activo', () => {
    service.getAll(true).subscribe((result) => expect(result).toEqual([user]));

    const request = httpTesting.expectOne((req) => req.url === '/api/users' && req.params.get('active') === 'true');
    expect(request.request.method).toBe('GET');
    request.flush([user]);
  });

  it('registra un usuario con POST', () => {
    const input: UserCreateInput = {
      username: 'ana', email: 'ana@example.com', password: 'secreto123', firstName: 'Ana', lastName: 'Admin'
    };
    service.create(input).subscribe((result) => expect(result).toEqual(user));

    const request = httpTesting.expectOne('/api/users');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual(input);
    request.flush(user);
  });

  it('inicia sesión con correo y contraseña', () => {
    service.login({ identifier: 'ana@example.com', password: 'secreto123' }).subscribe((result) => expect(result).toEqual(auth));

    const request = httpTesting.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ identifier: 'ana@example.com', password: 'secreto123' });
    request.flush(auth);
  });

  it('cambia el estado con PATCH', () => {
    service.updateStatus(7, false).subscribe((result) => expect(result).toEqual({ ...user, isActive: false }));

    const request = httpTesting.expectOne('/api/users/7/status');
    expect(request.request.method).toBe('PATCH');
    expect(request.request.body).toEqual({ isActive: false });
    request.flush({ ...user, isActive: false });
  });
});
