import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { Application } from '../../../core/models/models';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ApplicationsService {
  private api = inject(ApiService);
  private auth = inject(AuthService);

  // State
  items = signal<Application[]>([]);
  loading = signal(false);
  error = signal('');
  success = signal('');

  load(): Observable<Application[]> {
    this.loading.set(true);
    this.error.set('');
    const obs = this.auth.isCustomer() ? this.getMyApplications() : this.getAll();
    obs.subscribe({
      next: (data) => {
        this.items.set(data);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load applications.');
        this.loading.set(false);
      }
    });
    return obs;
  }

  executeAction(obs$: Observable<any>, successMsg: string, callback?: () => void) {
    this.loading.set(true);
    this.error.set('');
    this.success.set('');
    obs$.subscribe({
      next: (res) => {
        this.success.set(successMsg);
        this.load();
        if (callback) callback();
        // Clear success after 5s
        setTimeout(() => this.success.set(''), 5000);
      },
      error: (e) => {
        const msg = e?.error?.message || e?.error?.error || (typeof e?.error === 'string' ? e.error : null) || e?.message || 'Action failed';
        this.error.set(`${msg} [${e?.status || 'Error'}]`);
        this.loading.set(false);
        // Clear error after 8s
        setTimeout(() => this.error.set(''), 8000);
      }
    });
  }
  getAll() { return this.api.get<Application[]>('/applications'); }
  getMyApplications() { return this.api.get<Application[]>('/applications'); }
  getById(id: number) { return this.api.get<Application>(`/applications/${id}`); }
  create(data: any) { return this.api.post<Application>('/applications', data); }
  update(id: number, data: any) { return this.api.put<Application>(`/applications/${id}`, data); }
  delete(id: number) { return this.api.delete<void>(`/applications/${id}`); }
  submit(id: number) { return this.api.post<Application>(`/applications/${id}/submit`, {}); }
  assign(id: number, underwriterId: number) { return this.api.put<Application>(`/applications/${id}/assign/${underwriterId}`, {}); }
  acceptDecision(id: number) {
    return this.api.post<{ message: string; applicationId: string; newStatus: string }>(`/applications/${id}/accept`, {});
  }
  rejectDecision(id: number) {
    return this.api.post<{ message: string; applicationId: string; newStatus: string }>(`/applications/${id}/reject`, {});
  }
  getAssigned() { return this.api.get<Application[]>('/applications/assigned'); }
  getByStatus(status: string) { return this.api.get<Application[]>(`/applications/status/${status}`); }
  getDecisions(appId: number) { return this.api.get<any>(`/underwriting/application/${appId}/decisions`); }
  getLatestDecision(appId: number) { return this.api.get<any>(`/underwriting/application/${appId}/latest-decision`); }
}
