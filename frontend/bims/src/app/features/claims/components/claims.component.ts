import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule, TitleCasePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClaimsService } from '../services/claims.service';
import { PoliciesService } from '../../policies/services/policies.service';
import { UsersService } from '../../users/services/users.service';
import { AuthService } from '../../../core/services/auth.service';
import { Claim, Policy, User, DocumentType } from '../../../core/models/models';
import { DocumentListComponent } from '../../../shared/components/document-list.component';

@Component({
  selector: 'app-claims',
  standalone: true,
  imports: [CommonModule, FormsModule, TitleCasePipe, DocumentListComponent],
  templateUrl: './claims.component.html'
})
export class ClaimsComponent implements OnInit {
  svc = inject(ClaimsService);
  private policySvc = inject(PoliciesService);
  private userSvc = inject(UsersService);
  auth = inject(AuthService);

  myPolicies = signal<Policy[]>([]);
  officers = signal<User[]>([]);

  showFile = signal(false);
  showAssignModal = signal(false);
  showApprove = signal(false);
  showReject = signal(false);
  showSettle = signal(false);
  showDocs = signal(false);

  expandedClaim = signal<number | null>(null);
  activeTab = signal<string>('ALL');

  fileError = signal('');
  actionTarget = signal<Claim | null>(null);
  selectedOfficer = '';
  approvedAmount = 0;
  rejectionReason = '';
  settledAmount = 0;
  investigationNotes = '';
  fileForm: any = {};

  // Multi-step Wizard
  formStep = signal<number>(1); // 1: Details, 2: Documents, 3: Review
  savedClaimId = signal<number | undefined>(undefined);
  docCountForDraft = signal<number>(0);

  claimUploadTypes: { label: string, type: DocumentType, required?: boolean }[] = [
    { label: 'Damage Photo', type: 'CLAIM_PHOTO', required: true },
    { label: 'Repair Bill', type: 'REPAIR_BILL', required: true },
    { label: 'Police FIR', type: 'POLICE_REPORT' },
    { label: 'Other', type: 'OTHER' }
  ];

  filteredClaims = computed(() => {
    const tab = this.activeTab();
    const items = this.svc.items();
    if (tab === 'ALL') return items;
    return items.filter(c => c.status === tab);
  });

  statusCounts = computed(() => {
    const items = this.svc.items();
    return {
      ALL: items.length,
      SUBMITTED: items.filter(c => c.status === 'SUBMITTED').length,
      ASSIGNED: items.filter(c => c.status === 'ASSIGNED').length,
      UNDER_INVESTIGATION: items.filter(c => c.status === 'UNDER_INVESTIGATION').length,
      APPROVED: items.filter(c => c.status === 'APPROVED').length,
      REJECTED: items.filter(c => c.status === 'REJECTED').length,
      SETTLED: items.filter(c => c.status === 'SETTLED').length,
      DRAFT: items.filter(c => c.status === 'DRAFT').length,
      APPEALED: items.filter(c => c.status === 'APPEALED').length,
    };
  });

  ngOnInit() {
    this.svc.load();
    if (this.auth.isAdmin()) this.userSvc.getByRole('CLAIMS_OFFICER').subscribe(u => this.officers.set(u));
    if (this.auth.isCustomer()) this.policySvc.getMyPolicies().subscribe(p => this.myPolicies.set(p.filter(x => x.status === 'ACTIVE')));
  }

  // Wizard Methods
  openFileWizard() {
    if (this.myPolicies().length === 0) {
      alert('You do not have any active policies to file a claim against.');
      return;
    }
    this.fileForm = {
      policyId: this.myPolicies()[0]?.id,
      incidentDate: new Date().toISOString().split('T')[0],
      claimedAmount: 0,
      incidentDescription: ''
    };
    this.fileError.set('');
    this.formStep.set(1);
    this.savedClaimId.set(undefined);
    this.docCountForDraft.set(0);
    this.showFile.set(true);
  }

  saveDraftAndNext() {
    if (!this.fileForm.policyId) { this.fileError.set('Please select a policy.'); return; }
    if (!this.fileForm.claimedAmount || this.fileForm.claimedAmount <= 0) { this.fileError.set('Please enter a valid claimed amount.'); return; }
    if (!this.fileForm.incidentDate) { this.fileError.set('Please select incident date.'); return; }
    if (new Date(this.fileForm.incidentDate) > new Date()) { this.fileError.set('Incident date cannot be in the future.'); return; }
    if (!this.fileForm.incidentDescription?.trim()) { this.fileError.set('Please provide incident description.'); return; }

    const obs$ = this.savedClaimId() 
      ? this.svc.update(this.savedClaimId()!, this.fileForm)
      : this.svc.file(this.fileForm);

    this.svc.loading.set(true);
    obs$.subscribe({
      next: (res) => {
        this.savedClaimId.set(res.id);
        this.docCountForDraft.set(res.documentCount || 0);
        this.formStep.set(2);
        this.svc.loading.set(false);
      },
      error: (e) => {
        this.fileError.set(this.formatApiError(e, 'Failed to save claim draft'));
        this.svc.loading.set(false);
      }
    });
  }

  onDocCountChanged(count: number) {
    this.docCountForDraft.set(count);
  }

  goToReview() {
    this.formStep.set(3);
  }

  submitFromWizard() {
    if (!this.savedClaimId()) return;
    this.svc.executeAction(this.svc.submit(this.savedClaimId()!), 'Claim submitted successfully!', () => {
      this.closeWizard();
    });
  }

  closeWizard() {
    this.showFile.set(false);
    this.savedClaimId.set(undefined);
    this.svc.load();
  }

  // Existing Actions
  private formatApiError(e: any, fallback: string): string {
    const status = e?.status ? ` [${e.status}]` : '';
    const msg = e?.error?.message || e?.error?.error || (typeof e?.error === 'string' ? e.error : null) || e?.message;
    return msg ? `${msg}${status}` : `${fallback}${status}`;
  }

  openAssign(c: Claim) { this.actionTarget.set(c); this.selectedOfficer = ''; this.showAssignModal.set(true); }
  assignOfficer() { this.svc.executeAction(this.svc.assign(this.actionTarget()!.id, +this.selectedOfficer), 'Officer assigned successfully', () => this.showAssignModal.set(false)); }

  investigate(id: number) { this.svc.executeAction(this.svc.investigate(id), 'Claim investigation started'); }

  openApprove(c: Claim) { this.actionTarget.set(c); this.approvedAmount = c.claimedAmount; this.showApprove.set(true); }
  approveClaim() {
    if (this.approvedAmount <= 0) { alert('Approved amount must be positive.'); return; }
    if (this.approvedAmount > this.actionTarget()!.claimedAmount) { alert('Approved amount cannot exceed claimed amount.'); return; }
    this.svc.executeAction(this.svc.approve(this.actionTarget()!.id, this.approvedAmount), 'Claim approved successfully', () => this.showApprove.set(false));
  }

  openReject(c: Claim) { this.actionTarget.set(c); this.rejectionReason = ''; this.showReject.set(true); }
  rejectClaim() {
    if (!this.rejectionReason.trim()) { alert('Rejection reason is mandatory.'); return; }
    this.svc.executeAction(this.svc.reject(this.actionTarget()!.id, this.rejectionReason), 'Claim rejected', () => this.showReject.set(false));
  }

  openSettle(c: Claim) { this.actionTarget.set(c); this.settledAmount = c.approvedAmount || c.claimedAmount; this.showSettle.set(true); }
  settleClaim() {
    if (this.settledAmount <= 0) { alert('Settled amount must be positive.'); return; }
    if (this.settledAmount > (this.actionTarget()!.approvedAmount || 0)) { alert('Settled amount cannot exceed approved amount.'); return; }
    this.svc.executeAction(this.svc.settle(this.actionTarget()!.id, this.settledAmount), 'Claim settled successfully', () => this.showSettle.set(false));
  }

  appeal(id: number) { this.svc.executeAction(this.svc.appeal(id), 'Claim appealed successfully'); }
  submit(id: number) { this.svc.executeAction(this.svc.submit(id), 'Claim submitted successfully'); }
  delete(id: number) {
    if (!confirm('Delete this claim draft?')) return;
    this.svc.executeAction(this.svc.delete(id), 'Claim draft deleted');
  }

  expandClaimCard(c: Claim) {
    if (this.expandedClaim() === c.id) {
      this.expandedClaim.set(null);
      return;
    }
    this.expandedClaim.set(c.id);
    this.actionTarget.set(c);
    this.investigationNotes = '';
  }

  getSelectedPolicyNumber(): string {
    const p = this.myPolicies().find(x => x.id == this.fileForm.policyId);
    return p ? p.policyNumber : '';
  }

  setTab(tab: string) { this.activeTab.set(tab); }

  statusClass(s: string) {
    const m: any = { DRAFT: 'badge-gray', SUBMITTED: 'badge-blue', ASSIGNED: 'badge-yellow', UNDER_INVESTIGATION: 'badge-yellow', APPROVED: 'badge-green', REJECTED: 'badge-red', SETTLED: 'badge-purple', APPEALED: 'badge-blue' };
    return m[s] || 'badge-gray';
  }

  statusIcon(s: string): string {
    const m: any = { DRAFT: 'edit_note', SUBMITTED: 'send', ASSIGNED: 'person_add', UNDER_INVESTIGATION: 'manage_search', APPROVED: 'check_circle', REJECTED: 'cancel', SETTLED: 'paid', APPEALED: 'gavel' };
    return m[s] || 'help';
  }

  openDocs(c: Claim) { this.actionTarget.set(c); this.showDocs.set(true); }
  closeDocs() { this.showDocs.set(false); this.svc.load(); }
}
