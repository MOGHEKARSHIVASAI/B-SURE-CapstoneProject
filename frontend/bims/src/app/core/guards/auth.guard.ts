import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/models';

export const authGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) {
    return router.parseUrl('/login');
  }
  return true;
};

// Redirects logged-in users away from public pages (home, login, register)
export const guestGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (auth.isLoggedIn()) {
    return router.parseUrl('/dashboard');
  }
  return true;
};

export const roleGuard = (roles: Role[]): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isLoggedIn()) { return router.parseUrl('/login'); }
  if (!roles.includes(auth.role()!)) { return router.parseUrl('/dashboard'); }
  return true;
};
