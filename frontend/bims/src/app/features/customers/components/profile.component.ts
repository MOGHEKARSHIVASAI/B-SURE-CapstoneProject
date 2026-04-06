import { Component, inject, signal, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { BusinessService } from '../services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { Business } from '../../../core/models/models';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  private svc = inject(BusinessService);
  private auth = inject(AuthService);
  business = signal<Business | null>(null);
  loading = signal(true);
  saving = signal(false);
  success = signal(false);
  error = signal('');
  noProfile = signal(false);
  form: any = {};

  ngOnInit() {
    this.loadProfile();
  }

  private loadProfile() {
    this.loading.set(true);
    this.error.set('');
    this.noProfile.set(false);
    this.svc.getMyProfile().subscribe({
      next: (b) => {
        if (!b) {
          this.noProfile.set(true);
        } else {
          this.business.set(b);
          this.form = { ...b };
        }
        this.loading.set(false);
      },
      error: (err) => {
        if (err.status === 404) {
          this.noProfile.set(true);
        } else {
          this.error.set('Unable to load profile.');
        }
        this.loading.set(false);
      }
    });
  }

  save() {
    this.saving.set(true);
    this.error.set('');
    this.svc.updateMyProfile(this.form).subscribe({
      next: (b) => {
        this.business.set(b);
        this.success.set(true);
        this.saving.set(false);
        setTimeout(() => this.success.set(false), 3000);
      },
      error: (e) => {
        this.error.set(e.error?.message || 'Update failed');
        this.saving.set(false);
      }
    });
  }

  create() {
    this.saving.set(true);
    this.error.set('');
    this.svc.createBusiness(this.form).subscribe({
      next: (b) => {
        this.business.set(b);
        this.noProfile.set(false);
        this.success.set(true);
        this.saving.set(false);
        setTimeout(() => this.success.set(false), 3000);
      },
      error: (e) => {
        this.error.set(e.error?.message || 'Failed to create business profile');
        this.saving.set(false);
      }
    });
  }
}
