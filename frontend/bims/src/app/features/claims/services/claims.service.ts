import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { Claim } from '../../../core/models/models';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ClaimsService {
  private api = inject(ApiService);
  private auth = inject(AuthService);

  items = signal<Claim[]>([]);
  loading = signal(false);
  error = signal('');
  success = signal('');

  load() {
    this.loading.set(true);
    this.error.set('');
    let obs;
    if (this.auth.isCustomer()) obs = this.getMyClaims();
    else if (this.auth.isOfficer()) obs = this.getAssigned();
    else obs = this.getAll();
    obs.subscribe({
      next: (d) => {
        this.items.set(d);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load claims.');
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
        const msg = e?.error?.message || e?.error?.error || (typeof e?.error === 'string' ? e.error : null) || e?.message || 'Action failed';
        this.error.set(`${msg} [${e?.status || 'Error'}]`);
        this.loading.set(false);
      }
    });
  }
  getAll() { return this.api.get<Claim[]>('/claims'); }
  getMyClaims() { return this.api.get<Claim[]>('/claims'); }
  getAssigned() { return this.api.get<Claim[]>('/claims/assigned'); }
  file(data: any) { return this.api.post<Claim>('/claims', data); }
  update(id: number, data: any) { return this.api.put<Claim>(`/claims/${id}`, data); }
  assign(id: number, officerId: number) { return this.api.put<Claim>(`/claims/${id}/assign/${officerId}`, {}); }
  investigate(id: number) { return this.api.post<Claim>(`/claims/${id}/investigate`, {}); }
  approve(id: number, approvedAmount: number) { return this.api.post<Claim>(`/claims/${id}/approve`, { approvedAmount }); }
  reject(id: number, rejectionReason: string) { return this.api.post<Claim>(`/claims/${id}/reject`, { rejectionReason }); }
  settle(id: number, settledAmount: number) { return this.api.post<Claim>(`/claims/${id}/settle`, { settledAmount }); }
  appeal(id: number) { return this.api.post<Claim>(`/claims/${id}/appeal`, {}); }
  submit(id: number) { return this.api.post<Claim>(`/claims/${id}/submit`, {}); }
  delete(id: number) { return this.api.delete(`/claims/${id}`); }
}
