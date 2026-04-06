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
    // Clear stale error/success messages when wizard opens
    this.svc.error.set('');
    this.svc.success.set('');
    if (this.editingApp) {
      this.form = { ...this.editingApp };
      this.savedId.set(this.editingApp.id);
      this.docCount.set(this.editingApp.documentCount || 0);
    } else {
      const today = new Date().toISOString().split('T')[0];
      const nextYear = new Date(new Date().setFullYear(new Date().getFullYear() + 1)).toISOString().split('T')[0];
      this.form = {
        coverageAmount: 500000,
        coverageStartDate: today,
        coverageEndDate: nextYear,
        businessId: this.businesses[0]?.id ?? null,
        productId: null
      };
    }
  }

  // --- Logic ---
  calcPremium() {
    const p = this.products.find(x => x.id == this.form.productId);
    return p ? this.form.coverageAmount * p.basePremiumRate : 0;
  }

  saveDraft(nextStep = false) {
    if (!this.validate()) return;
    // Coerce select-bound string values to numbers before sending to backend
    const payload = {
      ...this.form,
      productId: this.form.productId ? +this.form.productId : null,
      businessId: this.form.businessId ? +this.form.businessId : null
    };
    const req$ = this.savedId() ? this.svc.update(this.savedId()!, payload) : this.svc.create(payload);
    this.svc.error.set('');
    this.svc.loading.set(true);
    req$.subscribe({
      next: (res: any) => {
        this.savedId.set(res.id || this.savedId());
        if (nextStep) this.formStep.set(2);
        else { 
          this.svc.success.set('Draft saved successfully!'); 
          this.saved.emit(); 
          setTimeout(() => this.svc.success.set(''), 5000);
        }
        this.svc.loading.set(false);
      },
      error: (e) => {
        const msg = e?.error?.message || e?.error?.error || e?.message || 'Failed to save application.';
        this.svc.error.set(msg);
        this.svc.loading.set(false);
        setTimeout(() => this.svc.error.set(''), 8000);
      }
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
    this.svc.error.set('');
    if (!this.form.productId) { this.svc.error.set('Please select an insurance product.'); return false; }
    if (!this.form.businessId) { this.svc.error.set('Please select a business.'); return false; }
    if (!this.form.coverageAmount || +this.form.coverageAmount <= 0) { this.svc.error.set('Please enter a valid coverage amount.'); return false; }
    if (!this.form.coverageStartDate || !this.form.coverageEndDate) { this.svc.error.set('Coverage start and end dates are required.'); return false; }
    if (new Date(this.form.coverageEndDate) <= new Date(this.form.coverageStartDate)) { this.svc.error.set('End date must be after start date.'); return false; }
    return true;
  }

  // Helpers for summary
  getProdName() { return this.products.find(p => p.id == this.form.productId)?.productName || 'N/A'; }
  getBizName() { return this.businesses.find(b => b.id == this.form.businessId)?.companyName || 'N/A'; }
}
