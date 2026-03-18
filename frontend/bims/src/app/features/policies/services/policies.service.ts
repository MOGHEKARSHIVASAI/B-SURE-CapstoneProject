import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { Policy } from '../../../core/models/models';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class PoliciesService {
  private api = inject(ApiService);
  private auth = inject(AuthService);

  items = signal<Policy[]>([]);
  loading = signal(false);
  error = signal('');
  success = signal('');

  load() {
    this.loading.set(true);
    this.error.set('');
    const obs = this.auth.isCustomer()
      ? this.getMyPolicies()
      : this.getAll();
    obs.subscribe({
      next: (d) => {
        this.items.set(d);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Failed to load policies.');
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
  getAll() { return this.api.get<Policy[]>('/policies'); }
  getMyPolicies() { return this.api.get<Policy[]>('/policies'); }
  getById(id: number) { return this.api.get<Policy>(`/policies/${id}`); }
  getByPolicyNumber(policyNumber: string) { return this.api.get<Policy>(`/policies/number/${policyNumber}`); }
  issue(appId: number, underwriterId: number) { return this.api.post<Policy>(`/policies/application/${appId}/issue/${underwriterId}`, {}); }
  cancel(id: number, reason: string) { return this.api.post<Policy>(`/policies/${id}/cancel`, { cancellationReason: reason }); }
  suspend(id: number) { return this.api.put<Policy>(`/policies/${id}/suspend`, {}); }
  reactivate(id: number) { return this.api.put<Policy>(`/policies/${id}/reactivate`, {}); }
  getPoliciesByBusiness(businessId: number) { return this.api.get<Policy[]>(`/policies/business/${businessId}`); }
}
