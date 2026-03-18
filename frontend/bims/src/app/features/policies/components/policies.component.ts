import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { Observable } from 'rxjs';
import { CommonModule, DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PoliciesService } from '../services/policies.service';
import { ApplicationsService } from '../../applications/services/applications.service';
import { UsersService } from '../../users/services/users.service';
import { PaymentsService } from '../../payments/services/payments.service';
import { AuthService } from '../../../core/services/auth.service';
import { Policy, Application, User } from '../../../core/models/models';

@Component({
  selector: 'app-policies',
  standalone: true,
  imports: [CommonModule, FormsModule, DecimalPipe],
  templateUrl: './policies.component.html'
})
export class PoliciesComponent implements OnInit {
  svc = inject(PoliciesService);
  private appSvc = inject(ApplicationsService);
  private userSvc = inject(UsersService);
  private paymentSvc = inject(PaymentsService);
  auth = inject(AuthService);

  acceptedApps = signal<Application[]>([]);
  underwriters = signal<User[]>([]);
  issueError = signal('');
  showIssue = signal(false);
  showCancel = signal(false);
  cancelTarget = signal<Policy | null>(null);
  cancelReason = '';
  issueAppId = '';
  issueUwId = '';

  selectedIssueApp = () => this.acceptedApps().find(a => a.id === +this.issueAppId) ?? null;

  totalCoverage = computed(() => this.svc.items().reduce((s, p) => s + (p.coverageAmount || 0), 0));
  activeCount = computed(() => this.svc.items().filter(p => p.status === 'ACTIVE').length);

  ngOnInit() {
    this.svc.load();
    if (this.auth.isAdmin()) {
      this.refreshIssuableApps();
      this.userSvc.getByRole('UNDERWRITER').subscribe(u => this.underwriters.set(u));
    }
  }

  private refreshIssuableApps() {
    this.appSvc.getByStatus('CUSTOMER_ACCEPTED').subscribe({
      next: (apps) => {
        this.acceptedApps.set([]);
        apps.forEach(a =>
          this.paymentSvc.getByApplication(a.id).subscribe({
            next: (payments) => {
              const paid = payments.some(p => p.status === 'PAID');
              if (paid) this.acceptedApps.update(list => list.some(x => x.id === a.id) ? list : [...list, a]);
              else this.acceptedApps.update(list => list.filter(x => x.id !== a.id));
            }
          })
        );
      },
      error: () => this.acceptedApps.set([])
    });
  }

  // removed local load and executeAction

  openIssue() {
    this.issueAppId = '';
    this.issueUwId = '';
    this.issueError.set('');
    this.showIssue.set(true);
  }

  issue() {
    const app = this.selectedIssueApp();
    if (!app) {
      this.issueError.set('Please select an application.');
      return;
    }

    const uwId = app.assignedUnderwriterId || +this.issueUwId;
    if (!uwId) {
      this.issueError.set('No underwriter selected or assigned.');
      return;
    }

    this.svc.executeAction(this.svc.issue(app.id, uwId), 'Policy issued successfully.', () => {
      this.showIssue.set(false);
      this.refreshIssuableApps();
    });
  }

  openCancel(p: Policy) { this.cancelTarget.set(p); this.cancelReason = ''; this.showCancel.set(true); }

  cancel() {
    this.svc.executeAction(this.svc.cancel(this.cancelTarget()!.id, this.cancelReason), 'Policy cancelled successfully.');
  }

  suspend(id: number) { this.svc.executeAction(this.svc.suspend(id), 'Policy suspended successfully.'); }
  reactivate(id: number) { this.svc.executeAction(this.svc.reactivate(id), 'Policy reactivated successfully.'); }

  statusClass(s: string) {
    const m: any = { ACTIVE: 'badge-green', EXPIRED: 'badge-gray', CANCELLED: 'badge-red', SUSPENDED: 'badge-yellow' };
    return m[s] || 'badge-gray';
  }
}
