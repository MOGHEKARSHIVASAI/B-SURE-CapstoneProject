import { Component, Input, Output, EventEmitter, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ApplicationsService } from '../services/applications.service';
import { AuthService } from '../../../core/services/auth.service';
import { Application, Product, Business, DocumentType } from '../../../core/models/models';
import { DocumentListComponent } from '../../../shared/components/document-list.component';

@Component({
  selector: 'app-application-wizard',
  standalone: true,
  imports: [CommonModule, FormsModule, DocumentListComponent],
  templateUrl: './application-wizard.component.html'
})
export class ApplicationWizardComponent implements OnInit {
  svc = inject(ApplicationsService);
  auth = inject(AuthService);

  @Input() products: Product[] = [];
  @Input() businesses: Business[] = [];
  @Input() editingApp?: Application;
  @Output() closed = new EventEmitter<void>();
  @Output() saved = new EventEmitter<void>();

  formStep = signal(1);
  savedId = signal<number | undefined>(undefined);
  docCount = signal(0);
  form: any = {};

  uploadTypes: any[] = [
    { label: 'GST Certificate', type: 'GST_CERTIFICATE', required: true },
    { label: 'Business Proof', type: 'BUSINESS_PROOF', required: true },
    { label: 'Asset Proof', type: 'ASSET_PROOF' },
    { label: 'Other', type: 'OTHER' }
  ];

  ngOnInit() {
    if (this.editingApp) {
      this.form = { ...this.editingApp };
      this.savedId.set(this.editingApp.id);
      this.docCount.set(this.editingApp.documentCount || 0);
    } else {
      const today = new Date().toISOString().split('T')[0];
      const nextYear = new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString().split('T')[0];
      this.form = { coverageAmount: 500000, coverageStartDate: today, coverageEndDate: nextYear, businessId: this.businesses[0]?.id };
    }
  }

  // --- Logic ---
  calcPremium() {
    const p = this.products.find(x => x.id == this.form.productId);
    return p ? this.form.coverageAmount * p.basePremiumRate : 0;
  }

  saveDraft(nextStep = false) {
    if (!this.validate()) return;
    const req$ = this.savedId() ? this.svc.update(this.savedId()!, this.form) : this.svc.create(this.form);
    
    this.svc.loading.set(true);
    req$.subscribe({
      next: (res: any) => {
        this.savedId.set(res.id || this.savedId());
        if (nextStep) this.formStep.set(2);
        else { this.svc.success.set('Draft saved!'); this.saved.emit(); }
        this.svc.loading.set(false);
      },
      error: (e) => { this.svc.error.set(e.message); this.svc.loading.set(false); }
    });
  }

  submit() {
    if (!this.savedId()) return;
    if (this.docCount() === 0) {
      this.svc.error.set('Please upload at least one supporting document before submitting.');
      return;
    }
    this.svc.executeAction(this.svc.submit(this.savedId()!), 'Submitted!', () => this.saved.emit());
  }

  private validate() {
    if (!this.form.productId) { this.svc.error.set('Select product'); return false; }
    if (!this.form.coverageStartDate || !this.form.coverageEndDate) { this.svc.error.set('Dates required'); return false; }
    return true;
  }

  // Helpers for summary
  getProdName() { return this.products.find(p => p.id == this.form.productId)?.productName || 'N/A'; }
  getBizName() { return this.businesses.find(b => b.id == this.form.businessId)?.companyName || 'N/A'; }
}
