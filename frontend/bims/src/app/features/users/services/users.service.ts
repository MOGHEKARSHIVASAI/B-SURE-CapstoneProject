import { Injectable, inject } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { User } from '../../../core/models/models';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private api = inject(ApiService);
  getAll() { return this.api.get<User[]>('/users'); }
  getById(id: number) { return this.api.get<User>(`/users/${id}`); }
  getByRole(role: string) { return this.api.get<User[]>(`/users/role/${role}`); }
  create(data: any) { return this.api.post<User>('/users', data); }
  update(id: number, data: any) { return this.api.put<User>(`/users/${id}`, data); }
  deactivate(id: number) { return this.api.deleteText(`/users/${id}`); }
  activate(id: number) { return this.api.putText(`/users/${id}/activate`); }
  resetPassword(id: number, newPassword: string) { return this.api.putText(`/users/${id}/reset-password`, { newPassword }); }
}
