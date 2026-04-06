import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { ApplicationsService } from '../applications/services/applications.service';
import { PoliciesService } from '../policies/services/policies.service';
import { ClaimsService } from '../claims/services/claims.service';
import { PaymentsService } from '../payments/services/payments.service';
import { UnderwritingService } from '../underwriting/services/underwriting.service';
import { Application, Claim } from '../../core/models/models';

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
  private uwSvc = inject(UnderwritingService);

  appCount = signal(0);
  policyCount = signal(0);
  claimCount = signal(0);
  paymentTotal = signal(0);
  
  // Specific stats for staff
  settledClaimCount = signal(0);
  rejectedClaimCount = signal(0);
  approvedAppCount = signal(0);
  rejectedAppCount = signal(0);

  loading = signal(true);
  error = signal('');
  myApps = signal<Application[]>([]);

  stats = computed(() => {
    if (this.auth.isCustomer()) {
      return [
        { label: 'My Applications', value: this.appCount(), sub: 'Total submitted', color: 'var(--burg-700)', icon: 'description' },
        { label: 'Active Policies', value: this.policyCount(), sub: 'Currently active', color: 'var(--ok)', icon: 'policy' },
        { label: 'My Claims', value: this.claimCount(), sub: 'All time', color: 'var(--burg-500)', icon: 'assignment_late' },
        { label: 'Total Paid', value: '\u20B9' + this.paymentTotal().toLocaleString(), sub: 'Premiums paid', color: 'var(--cream-800)', icon: 'payments' },
      ];
    }
    if (this.auth.isOfficer()) {
      return [
        { label: 'Assigned Claims', value: this.claimCount(), sub: 'Pending action', color: 'var(--burg-700)', icon: 'assignment_ind' },
        { label: 'Settled', value: this.settledClaimCount(), sub: 'Fully processed', color: 'var(--ok)', icon: 'check_circle' },
        { label: 'Rejected', value: this.rejectedClaimCount(), sub: 'Declined claims', color: 'var(--burg-400)', icon: 'cancel' },
        { label: 'Settled Amount', value: '\u20B9' + this.paymentTotal().toLocaleString(), sub: 'Total disbursed', color: 'var(--cream-800)', icon: 'payments' },
      ];
    }
    if (this.auth.isUnderwriter()) {
      return [
        { label: 'My Queue', value: this.appCount(), sub: 'Pending review', color: 'var(--burg-700)', icon: 'queue' },
        { label: 'Approved', value: this.approvedAppCount(), sub: 'Ready for client', color: 'var(--ok)', icon: 'verified' },
        { label: 'Rejected', value: this.rejectedAppCount(), sub: 'Declined apps', color: 'var(--burg-400)', icon: 'do_not_disturb_on' },
        { label: 'Total Handled', value: this.appCount() + this.approvedAppCount() + this.rejectedAppCount(), sub: 'Lifetime volume', color: 'var(--cream-800)', icon: 'analytics' },
      ];
    }
    return [
      { label: 'Applications', value: this.appCount(), sub: 'Total in system', color: 'var(--burg-700)', icon: 'description' },
      { label: 'Policies', value: this.policyCount(), sub: 'Total issued', color: 'var(--ok)', icon: 'policy' },
      { label: 'Claims', value: this.claimCount(), sub: 'Total filed', color: 'var(--burg-500)', icon: 'assignment_late' },
      { label: 'Revenue', value: '\u20B9' + this.paymentTotal().toLocaleString(), sub: 'Collected premiums', color: 'var(--cream-800)', icon: 'payments' },
    ];
  });

  quickActions = computed(() => {
    if (this.auth.isCustomer()) {
      const pendingPayment = this.myApps().find(a => a.status === 'CUSTOMER_ACCEPTED');
      const paymentLink = pendingPayment ? `/payments?applicationId=${pendingPayment.id}` : '/payments';
      
      return [
        { icon: 'category', label: 'Browse Products & Apply', link: '/products' },
        { icon: 'policy', label: 'View My Policies', link: '/policies' },
        { icon: 'assignment_late', label: 'File a Claim', link: '/claims' },
        { icon: 'credit_card', label: pendingPayment ? 'Complete Pending Payment' : 'Make a Payment', link: paymentLink },
      ];
    }
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
        next: (d) => {
          this.appCount.set(d.length);
          this.myApps.set(d);
        },
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
      this.uwSvc.getAssigned().subscribe({
        next: (apps) => {
          this.appCount.set(apps.filter(a => a.status === 'UNDER_REVIEW').length);
          this.approvedAppCount.set(apps.filter(a => a.status === 'APPROVED' || a.status === 'CUSTOMER_ACCEPTED' || a.status === 'POLICY_ISSUED').length);
          this.rejectedAppCount.set(apps.filter(a => a.status === 'REJECTED' || a.status === 'CUSTOMER_REJECTED').length);
          this.loading.set(false);
        },
        error: () => { this.error.set('Failed to load apps'); this.loading.set(false); }
      });
    } else if (this.auth.isOfficer()) {
      this.claimSvc.getAssigned().subscribe({
        next: (claims) => {
          this.claimCount.set(claims.filter(c => c.status !== 'SETTLED' && c.status !== 'REJECTED').length);
          this.settledClaimCount.set(claims.filter(c => c.status === 'SETTLED').length);
          this.rejectedClaimCount.set(claims.filter(c => c.status === 'REJECTED').length);
          this.paymentTotal.set(claims.filter(c => c.status === 'SETTLED').reduce((sum, c) => sum + (c.settledAmount || 0), 0));
          this.loading.set(false);
        },
        error: () => { this.error.set('Failed to load claims'); this.loading.set(false); }
      });
    }
  }
}
