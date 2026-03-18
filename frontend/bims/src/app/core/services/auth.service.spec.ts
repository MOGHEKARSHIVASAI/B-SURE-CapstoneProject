import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';
import { AuthResponse } from '../models/models';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;
  let router: jasmine.SpyObj<Router>;

  beforeEach(() => {
    const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        AuthService,
        { provide: Router, useValue: routerSpy }
      ]
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router) as jasmine.SpyObj<Router>;

    // Clean localStorage before each test
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('login should set session and navigate', fakeAsync(() => {
    const mockRes: AuthResponse = { accessToken: 'fake-token', tokenType: 'Bearer' };
    service.login('test@test.com', 'password');

    const req = httpMock.expectOne('http://localhost:8050/api/v1/auth/login');
    expect(req.request.method).toBe('POST');
    req.flush(mockRes);

    tick(600); // Wait for setTimeout

    expect(localStorage.getItem('token')).toBe('fake-token');
    expect((service as any)._token()).toBe('fake-token');
    expect(router.navigate).toHaveBeenCalledWith(['/dashboard']);
    expect(service.success()).toBe('Login successful! Redirecting...');
  }));

  it('logout should clear session and navigate', () => {
    localStorage.setItem('token', 'fake-token');
    (service as any)._token.set('fake-token');

    service.logout();

    const req = httpMock.expectOne('http://localhost:8050/api/v1/auth/logout');
    expect(req.request.method).toBe('POST');
    req.flush('Logged out', { status: 200, statusText: 'OK' });

    expect(localStorage.getItem('token')).toBeNull();
    expect((service as any)._token()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('forceLogout should clear session and navigate immediately', () => {
    localStorage.setItem('token', 'fake-token');
    (service as any)._token.set('fake-token');

    service.forceLogout();

    expect(localStorage.getItem('token')).toBeNull();
    expect((service as any)._token()).toBeNull();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
  });

  it('isLoggedIn should return true if token exists', () => {
    (service as any)._token.set('token');
    expect(service.isLoggedIn()).toBeTrue();
  });

  it('isLoggedIn should return false if no token', () => {
    (service as any)._token.set(null);
    expect(service.isLoggedIn()).toBeFalse();
  });
});
