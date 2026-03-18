import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ApplicationsService } from '../applications/services/applications.service';
import { PoliciesService } from '../policies/services/policies.service';
import { ClaimsService } from '../claims/services/claims.service';
import { PaymentsService } from '../payments/services/payments.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  auth = inject(AuthService);
  private appSvc = inject(ApplicationsService);
  private policySvc = inject(PoliciesService);
  private claimSvc = inject(ClaimsService);
  private paymentSvc = inject(PaymentsService);

  appCount = signal(0);
  policyCount = signal(0);
  claimCount = signal(0);
  paymentTotal = signal(0);

  loading = signal(true);
  error = signal('');

  stats = computed(() => {
    if (this.auth.isCustomer()) {
      return [
        { label: 'My Applications', value: this.appCount(), sub: 'Total submitted', color: 'var(--burg-700)' },
        { label: 'Active Policies', value: this.policyCount(), sub: 'Currently active', color: 'var(--ok)' },
        { label: 'My Claims', value: this.claimCount(), sub: 'All time', color: 'var(--burg-500)' },
        { label: 'Total Paid', value: '\u20B9' + this.paymentTotal().toLocaleString(), sub: 'Premiums paid', color: 'var(--cream-800)' },
      ];
    }
    return [
      { label: 'Applications', value: this.appCount(), sub: 'Total in system', color: 'var(--burg-700)' },
      { label: 'Policies', value: this.policyCount(), sub: 'Total issued', color: 'var(--ok)' },
      { label: 'Claims', value: this.claimCount(), sub: 'Total filed', color: 'var(--burg-500)' },
      { label: 'Revenue', value: '\u20B9' + this.paymentTotal().toLocaleString(), sub: 'Collected premiums', color: 'var(--cream-800)' },
    ];
  });

  quickActions = computed(() => {
    if (this.auth.isCustomer()) return [
      { icon: 'category', label: 'Browse Products & Apply', link: '/products' },
      { icon: 'policy', label: 'View My Policies', link: '/policies' },
      { icon: 'assignment_late', label: 'File a Claim', link: '/claims' },
      { icon: 'credit_card', label: 'Make a Payment', link: '/payments' },
    ];
    if (this.auth.isAdmin()) return [
      { icon: 'group', label: 'Manage Staff', link: '/users' },
      { icon: 'description', label: 'Review Applications', link: '/applications' },
      { icon: 'policy', label: 'Manage Policies', link: '/policies' },
      { icon: 'assignment_late', label: 'Manage Claims', link: '/claims' },
    ];
    if (this.auth.isUnderwriter()) return [
      { icon: 'task_alt', label: 'My Underwriting Queue', link: '/underwriting' },
      { icon: 'description', label: 'All Applications', link: '/applications' },
    ];
    return [{ icon: 'assignment_late', label: 'My Assigned Claims', link: '/claims' }];
  });

  greeting() {
    const h = new Date().getHours();
    if (h < 12) return 'morning';
    if (h < 17) return 'afternoon';
    return 'evening';
  }

  ngOnInit() {
    this.loading.set(true);
    this.error.set('');

    if (this.auth.isCustomer()) {
      this.appSvc.getMyApplications().subscribe({
        next: (d) => this.appCount.set(d.length),
        error: () => this.error.set('Failed to load apps.')
      });
      this.policySvc.getMyPolicies().subscribe({
        next: (d) => this.policyCount.set(d.filter(p => p.status === 'ACTIVE').length),
        error: () => this.error.set('Failed to load policies.')
      });
      this.claimSvc.getMyClaims().subscribe({
        next: (d) => this.claimCount.set(d.length),
        error: () => this.error.set('Failed to load claims.')
      });
      this.paymentSvc.getMyPayments().subscribe({
        next: (d) => {
          this.paymentTotal.set(d.filter(p => p.status === 'PAID').reduce((s, p) => s + p.amount, 0));
          this.loading.set(false);
        },
        error: () => { this.error.set('Failed to load payments.'); this.loading.set(false); }
      });
    } else if (this.auth.isAdmin()) {
      this.appSvc.getAll().subscribe({
        next: (d) => this.appCount.set(d.length),
        error: () => this.error.set('Failed to load apps.')
      });
      this.policySvc.getAll().subscribe({
        next: (d) => this.policyCount.set(d.length),
        error: () => this.error.set('Failed to load policies.')
      });
      this.claimSvc.getAll().subscribe({
        next: (d) => this.claimCount.set(d.length),
        error: () => this.error.set('Failed to load claims.')
      });
      this.paymentSvc.getAll().subscribe({
        next: (d) => {
          this.paymentTotal.set(d.filter(p => p.status === 'PAID').reduce((s, p) => s + p.amount, 0));
          this.loading.set(false);
        },
        error: () => { this.error.set('Failed to load payments.'); this.loading.set(false); }
      });
    } else if (this.auth.isUnderwriter()) {
      this.appSvc.getAll().subscribe({
        next: (d) => { this.appCount.set(d.length); this.loading.set(false); },
        error: () => { this.error.set('Failed to load apps'); this.loading.set(false); }
      });
    } else if (this.auth.isOfficer()) {
      this.claimSvc.getAssigned().subscribe({
        next: (d) => { this.claimCount.set(d.length); this.loading.set(false); },
        error: () => { this.error.set('Failed to load claims'); this.loading.set(false); }
      });
    }
  }
}
