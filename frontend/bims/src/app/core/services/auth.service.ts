import { Injectable, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthResponse, User } from '../models/models';
import { jwtDecode } from 'jwt-decode';

const API = 'http://localhost:8050/api/v1';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private _token = signal<string | null>(localStorage.getItem('token'));
  private _user = signal<User | null>(this.decodeToken(this._token()));

  user = this._user.asReadonly();
  token = this._token.asReadonly();
  isLoggedIn = computed(() => this._token() ? true : false);
  role = computed(() => this._user()?.role ?? null);
  isAdmin = computed(() => this.role() === 'ADMIN');
  isCustomer = computed(() => this.role() === 'CUSTOMER');
  isUnderwriter = computed(() => this.role() === 'UNDERWRITER');
  isOfficer = computed(() => this.role() === 'CLAIMS_OFFICER');

  loading = signal(false);
  error = signal('');
  success = signal('');

  constructor(private http: HttpClient, private router: Router) { }

  private decodeToken(token: string | null): User | null {
    if (!token) return null;
    try {
      const decoded: any = jwtDecode(token);
      return {
        id: decoded.id,
        email: decoded.sub,
        role: decoded.role,
        firstName: decoded.firstName,
        lastName: decoded.lastName,
        businessId: decoded.businessId,
        createdAt: decoded.createdAt
      } as User;
    } catch {
      return null;
    }
  }

  login(email: string, password: string) {
    this.loading.set(true);
    this.error.set('');
    this.success.set('');
    this.http.post<AuthResponse>(`${API}/auth/login`, { email, password }).subscribe({
      next: (res) => {
        this.setSession(res);
        this.success.set('Login successful! Redirecting...');
        setTimeout(() => this.router.navigate(['/dashboard']), 600);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(e.error?.message || e.error?.error || 'Invalid credentials. Please check your email and password.');
        this.loading.set(false);
        // Clear error after 7s to keep UI clean
        setTimeout(() => this.error.set(''), 7000);
      }
    });
  }

  register(data: any) {
    this.loading.set(true);
    this.error.set('');
    this.success.set('');
    this.http.post<AuthResponse>(`${API}/auth/register`, data).subscribe({
      next: (res) => {
        this.setSession(res);
        this.success.set('Registration successful! Setting up your workspace...');
        setTimeout(() => this.router.navigate(['/dashboard']), 1000);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(e.error?.message || 'Registration failed. Please check your details.');
        this.loading.set(false);
        // Clear error after 7s
        setTimeout(() => this.error.set(''), 7000);
      }
    });
  }

  private setSession(res: AuthResponse) {
    if (res.accessToken) {
      localStorage.setItem('token', res.accessToken);
      this._token.set(res.accessToken);
      this._user.set(this.decodeToken(res.accessToken));
    }
  }

  logout() {
    this.http.post(`${API}/auth/logout`, {}, { responseType: 'text' }).subscribe();
    this.clearSession();
  }

  private clearSession() {
    localStorage.clear();
    this._token.set(null);
    this._user.set(null);
    this.error.set('');
    this.success.set('');
    this.router.navigate(['/login']);
  }

  getUserId(): number | null {
    return this._user()?.id ?? null;
  }

  getBusinessId(): number | null {
    return this._user()?.businessId ?? null;
  }

  getUnderwriterId(): number | null {
    return this._user()?.id ?? null;
  }

  getOfficerId(): number | null {
    return this._user()?.id ?? null;
  }
}
