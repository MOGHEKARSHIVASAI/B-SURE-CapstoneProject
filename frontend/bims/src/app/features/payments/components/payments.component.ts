import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaymentsService } from '../services/payments.service';
import { ApplicationsService } from '../../applications/services/applications.service';
import { AuthService } from '../../../core/services/auth.service';
import { Payment, Application } from '../../../core/models/models';

@Component({
  selector: 'app-payments',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './payments.component.html'
})
export class PaymentsComponent implements OnInit {
  private svc = inject(PaymentsService);
  private appSvc = inject(ApplicationsService);
  auth = inject(AuthService);

  payments = signal<Payment[]>([]);
  payableApps = signal<Application[]>([]);
  loading = signal(true);
  saving = signal(false);
  showModal = signal(false);
  error = signal('');
  myCustomerId = signal<number | null>(null);
  form: any = {};

  ngOnInit() {
    if (this.auth.isCustomer()) {
      this.load();
      this.reloadPayableApps();
    } else {
      this.load();
    }
  }

  private formatApiError(e: any, fallback: string): string {
    const status = e?.status ? ` [${e.status}]` : '';
    const msg = e?.error?.message || e?.error?.error || (typeof e?.error === 'string' ? e.error : null) || e?.message;
    return msg ? `${msg}${status}` : `${fallback}${status}`;
  }

  private reloadPayableApps() {
    this.appSvc.getMyApplications().subscribe({
      next: (apps) => {
        const accepted = apps.filter(a => a.status === 'CUSTOMER_ACCEPTED');
        const paidAppIds = new Set(this.payments().filter(p => p.status === 'PAID').map(p => p.applicationId));
        this.payableApps.set(accepted.filter(a => !paidAppIds.has(a.id)));
      },
      error: (e) => this.error.set(this.formatApiError(e, 'Unable to load payable applications.'))
    });
  }

  load() {
    const obs = this.auth.isCustomer() ? this.svc.getMyPayments() : this.svc.getAll();
    obs.subscribe({
      next: (d) => {
        this.payments.set(d);
        this.loading.set(false);
        if (this.auth.isCustomer()) {
          this.reloadPayableApps();
        }
      },
      error: (e) => {
        this.error.set(this.formatApiError(e, 'Unable to load payments.'));
        this.loading.set(false);
      }
    });
  }

  openPay() { this.form = {}; this.error.set(''); this.showModal.set(true); }

  onAppSelect(appId: number) {
    const app = this.payableApps().find(a => a.id == appId);
    if (app) {
      this.form.amount = app.annualPremium || (app.coverageAmount * 0.025);
      this.form.remarks = `Premium payment for application #${appId}`;
    }
  }

  pay() {
    if (!this.form.applicationId) { this.error.set('Please select an application.'); return; }
    if (!this.form.amount || this.form.amount <= 0) { this.error.set('Please enter a valid amount.'); return; }
    this.saving.set(true);
    this.svc.makePayment(this.form).subscribe({
      next: () => {
        this.showModal.set(false);
        this.load();
        this.saving.set(false);
      },
      error: (e) => { this.error.set(this.formatApiError(e, 'Payment failed')); this.saving.set(false); }
    });
  }

  statusClass(s: string) {
    const m: any = { PAID: 'badge-green', PENDING: 'badge-yellow', FAILED: 'badge-red', REFUNDED: 'badge-blue' };
    return m[s] || 'badge-gray';
  }
}
