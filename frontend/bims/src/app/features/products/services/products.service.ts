import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product } from '../../../core/models/models';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ProductsService {
  private api = inject(ApiService);
  private auth = inject(AuthService);

  // State
  items = signal<Product[]>([]);
  loading = signal(false);
  error = signal('');
  success = signal('');

  load() {
    this.loading.set(true);
    this.error.set('');
    const obs = this.auth.isAdmin() ? this.getAllForAdmin() : this.getAll();
    obs.subscribe({
      next: (data) => {
        const normalized = data.map(p => ({ ...p, isActive: this.resolveActive(p), active: this.resolveActive(p) }));
        this.items.set(normalized);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set('Failed to load products');
        this.loading.set(false);
      }
    });
  }

  private resolveActive(p: any): boolean {
    const raw = p.isActive ?? p.active ?? p.enabled ?? p.status;
    if (typeof raw === 'boolean') return raw;
    if (typeof raw === 'number') return raw === 1;
    if (typeof raw === 'string') {
      const v = raw.trim().toLowerCase();
      return v === 'true' || v === '1' || v === 'active' || v === 'enabled';
    }
    return false;
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
        this.error.set(e.error?.message || e.error?.error || e.message || 'Action failed');
        this.loading.set(false);
      }
    });
  }
  getAll() { return this.api.get<Product[]>('/products'); }
  getAllForAdmin() { return this.api.get<Product[]>('/products/all'); }
  getById(id: number) { return this.api.get<Product>(`/products/${id}`); }
  getByCategory(category: string) { return this.api.get<Product[]>(`/products/category/${category}`); }
  create(data: Partial<Product>) { return this.api.post<Product>('/products', data); }
  update(id: number, data: Partial<Product>) { return this.api.put<Product>(`/products/${id}`, data); }
  deactivate(id: number) { return this.api.deleteText(`/products/${id}`); }
}
