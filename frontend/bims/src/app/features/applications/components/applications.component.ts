import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ApplicationsService } from '../services/applications.service';
import { ProductsService } from '../../products/services/products.service';
import { UsersService } from '../../users/services/users.service';
import { BusinessService } from '../../customers/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { Application, Product, User, Business } from '../../../core/models/models';
import { DocumentListComponent } from '../../../shared/components/document-list.component';
import { ApplicationWizardComponent } from './application-wizard.component';

@Component({
  selector: 'app-applications',
  standalone: true,
  imports: [CommonModule, FormsModule, TitleCasePipe, DocumentListComponent, ApplicationWizardComponent],
  templateUrl: './applications.component.html'
})
export class ApplicationsComponent implements OnInit {
  svc = inject(ApplicationsService);
  prodSvc = inject(ProductsService);
  private userSvc = inject(UsersService);
  bizSvc = inject(BusinessService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  auth = inject(AuthService);

  // Signals for state
  products = signal<Product[]>([]);
  underwriters = signal<User[]>([]);
  myBusinesses = signal<Business[]>([]);
  showWizard = signal(false);
  showAssign = signal(false);
  showDocs = signal(false);
  showReason = signal(false);
  editingApp = signal<Application | undefined>(undefined);
  selectedApp = signal<Application | undefined>(undefined);
  selectedUW = '';
  decisionCache = signal<{ [id: number]: any }>({});

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.prodSvc.getAll().subscribe(p => this.products.set(p));
    
    if (this.auth.isCustomer()) {
      this.bizSvc.getUsersBusinesses().subscribe(data => {
        this.myBusinesses.set(data || []);
        
        // Check for "Apply Now" navigation from Products
        const params = this.route.snapshot.queryParams;
        if (params['new'] === '1') {
          this.openWizard();
          this.router.navigate([], { queryParams: { new: null, productId: null }, queryParamsHandling: 'merge' });
        }
      });
    } else if (this.auth.isAdmin()) {
      this.userSvc.getByRole('UNDERWRITER').subscribe(data => {
        this.underwriters.set(data || []);
      });
    }

    this.svc.load().subscribe(items => {
      items.forEach(a => {
        const needsDecision = ['APPROVED', 'REJECTED', 'CUSTOMER_ACCEPTED', 'CUSTOMER_REJECTED'].includes(a.status);
        if (needsDecision && !this.decisionCache()[a.id]) {
          this.svc.getLatestDecision(a.id).subscribe(d => this.decisionCache.update(c => ({...c, [a.id]: d})));
        }
      });
    });
  }

  // High-level Actions
  openWizard(app?: Application) {
    this.svc.error.set('');
    this.svc.success.set('');
    if (this.auth.isCustomer() && this.myBusinesses().length === 0) {
      this.svc.error.set('No business profile found. Please create a business profile before applying for insurance.');
      return;
    }
    this.editingApp.set(app);
    this.showWizard.set(true);
  }

  onWizardClose() {
    this.showWizard.set(false);
    this.refresh();
  }

  deleteDraft(id: number) {
    if (confirm('Delete this draft?')) this.svc.executeAction(this.svc.delete(id), 'Draft deleted');
  }

  submit(id: number) {
    const app = this.svc.items().find(a => a.id === id);
    if (app && app.documentCount === 0) {
      this.svc.error.set('Please upload supporting documents before submitting.');
      return;
    }
    if (confirm('Submit this application?')) this.svc.executeAction(this.svc.submit(id), 'Submitted successfully');
  }

  handleVerdict(action: 'accept' | 'reject', app: Application) {
    if (!confirm(`Are you sure you want to ${action} this decision?`)) return;
    const req$ = action === 'accept' ? this.svc.acceptDecision(app.id) : this.svc.rejectDecision(app.id);
    this.svc.executeAction(req$, `Decision ${action}ed successfully`, () => {
      if (action === 'accept') this.openPayments(app.id);
    });
  }

  assign() {
    if (!this.selectedUW) return;
    this.svc.executeAction(this.svc.assign(this.selectedApp()!.id, +this.selectedUW), 'Underwriter assigned', () => this.showAssign.set(false));
  }

  openAssign(a: Application) { this.selectedApp.set(a); this.showAssign.set(true); }
  openDocs(a: Application) { this.selectedApp.set(a); this.showDocs.set(true); }
  openReason(a: Application) { this.selectedApp.set(a); this.showReason.set(true); }
  closeDocs() { this.showDocs.set(false); this.refresh(); }
  getDecision(id: number) { return this.decisionCache()[id]; }
  
  openPayments(appId?: number) {
    if (!appId && this.auth.isCustomer()) {
      const pending = this.svc.items().find(a => a.status === 'CUSTOMER_ACCEPTED');
      if (pending) appId = pending.id;
    }
    this.router.navigate(['/payments'], { queryParams: appId ? { applicationId: appId, source: 'applications' } : { source: 'applications' } });
  }

  customerAcceptedPendingPayment = computed(() =>
    this.auth.isCustomer() && this.svc.items().some(a => a.status === 'CUSTOMER_ACCEPTED')
  );

  statusClass(s: string) {
    const map: any = { DRAFT: 'badge-gray', SUBMITTED: 'badge-blue', UNDER_REVIEW: 'badge-yellow', APPROVED: 'badge-green', REJECTED: 'badge-red', CUSTOMER_ACCEPTED: 'badge-green', POLICY_ISSUED: 'badge-purple' };
    return map[s] || 'badge-gray';
  }
}
