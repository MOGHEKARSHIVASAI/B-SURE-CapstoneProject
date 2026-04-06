import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Business } from '../../../core/models/models';
import { Observable, map, throwError } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class BusinessService {
  private api = inject(ApiService);

  items = signal<Business[]>([]);
  loading = signal(false);
  error = signal('');
  success = signal('');

  load() {
    this.loading.set(true);
    this.error.set('');
    this.getUsersBusinesses().subscribe({
      next: (data) => {
        this.items.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load businesses.');
        this.loading.set(false);
      }
    });
  }

  executeAction(obs$: Observable<any>, successMsg: string, callback?: () => void) {
    this.loading.set(true);
    this.error.set('');
    this.success.set('');
    obs$.subscribe({
      next: () => {
        this.success.set(successMsg);
        this.load();
        if (callback) callback();
      },
      error: (e) => {
        const msg = e?.error?.message || e?.error?.error || e?.message || 'Action failed';
        this.error.set(`${msg} [${e?.status || 'Error'}]`);
        this.loading.set(false);
      }
    });
  }

  getAll() {
    return this.api.get<Business[]>('/businesses');
  }

  getById(id: number) {
    return this.api.get<Business>(`/businesses/${id}`);
  }

  getMyProfile() {
    return this.api.get<Business[]>('/businesses').pipe(
      map(list => list[0])
    );
  }

  updateMyProfile(data: any): Observable<Business> {
    if (!data || !data.id) {
      // If no ID, we can't update. This might happen if profile is not yet loaded.
      return throwError(() => new Error('Business ID is required for update'));
    }
    return this.update(data.id, data);
  }

  createBusiness(data: any) {
    return this.api.post<Business>('/businesses', data);
  }

  getUsersBusinesses() {
    return this.api.get<Business[]>('/businesses');
  }

  update(id: number, data: any) {
    return this.api.put<Business>(`/businesses/${id}`, data);
  }

  delete(id: number) {
    return this.api.delete<any>(`/businesses/${id}`);
  }
}
