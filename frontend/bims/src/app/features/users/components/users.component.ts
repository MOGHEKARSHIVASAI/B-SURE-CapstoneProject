import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UsersService } from '../services/users.service';
import { User } from '../../../core/models/models';

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users.component.html'})
export class UsersComponent implements OnInit {
  private svc = inject(UsersService);
  users = signal<User[]>([]);
  loading = signal(true);
  saving = signal(false);
  showCreate = signal(false);
  showReset = signal(false);
  createError = signal('');
  actionError = signal('');
  resetUser = signal<User | null>(null);
  resetMsg = signal('');
  newPassword = '';
  form: any = { role: 'UNDERWRITER' };

  ngOnInit() { this.load(); }

  load() {
    this.svc.getAll().subscribe({
      next: (u) => {
        const normalized = u
          .filter(x => x.role !== 'CUSTOMER')
          .map(x => ({ ...x, active: this.resolveActive(x as any) }));
        this.users.set(normalized);
        this.actionError.set('');
        this.loading.set(false);
      },
      error: () => this.loading.set(false)
    });
  }

  private resolveActive(u: any): boolean {
    // Backend payloads may vary: active, isActive, enabled, status, etc.
    const raw = u.active ?? u.isActive ?? u.enabled ?? u.userActive ?? u.accountActive ?? u.status;
    if (typeof raw === 'boolean') return raw;
    if (typeof raw === 'number') return raw === 1;
    if (typeof raw === 'string') {
      const v = raw.trim().toLowerCase();
      return v === 'true' || v === '1' || v === 'active' || v === 'enabled';
    }
    return false;
  }

  isUserActive(u: User): boolean {
    return !!(u.active ?? u.isActive);
  }

  openCreate() { this.form = { role: 'UNDERWRITER' }; this.createError.set(''); this.showCreate.set(true); }

  create() {
    this.saving.set(true);
    this.svc.create(this.form).subscribe({
      next: () => { this.showCreate.set(false); this.load(); this.saving.set(false); },
      error: (e) => { this.createError.set(e.error?.message || 'Error'); this.saving.set(false); }
    });
  }

  deactivate(id: number) {
    if (!confirm('Deactivate this user?')) return;
    this.svc.deactivate(id).subscribe({
      next: () => this.load(),
      error: (e) => this.actionError.set(e.error?.message || e.error?.error || 'Failed to deactivate user.')
    });
  }

  activate(id: number) {
    this.svc.activate(id).subscribe({
      next: () => this.load(),
      error: (e) => this.actionError.set(e.error?.message || e.error?.error || 'Failed to activate user.')
    });
  }

  openReset(u: User) { this.resetUser.set(u); this.newPassword = ''; this.resetMsg.set(''); this.showReset.set(true); }

  resetPassword() {
    this.svc.resetPassword(this.resetUser()!.id, this.newPassword).subscribe({
      next: () => this.resetMsg.set('Password reset successfully!'),
      error: () => this.resetMsg.set('Error resetting password')
    });
  }

  roleClass(r: string) {
    return r === 'UNDERWRITER' ? 'badge-blue' : r === 'CLAIMS_OFFICER' ? 'badge-purple' : 'badge-gray';
  }
}
