import { Component, inject, signal, OnInit, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UnderwritingService } from '../services/underwriting.service';
import { AuthService } from '../../../core/services/auth.service';
import { Application, Business } from '../../../core/models/models';
import { DocumentListComponent } from '../../../shared/components/document-list.component';

@Component({
  selector: 'app-underwriting',
  standalone: true,
  imports: [CommonModule, FormsModule, DocumentListComponent],
  templateUrl: './underwriting.component.html'
})
export class UnderwritingComponent implements OnInit {
  private svc = inject(UnderwritingService);
  auth = inject(AuthService);

  queue = signal<Application[]>([]);
  loading = signal(true);
  loadError = signal('');
  success = signal('');
  saving = signal(false);
  showDocs = signal(false);
  modalError = signal('');
  selectedApp = signal<Application | null>(null);
  selectedBusiness = signal<Business | null>(null);
  riskScores = signal<{ [id: number]: number }>({});
  expandedApp = signal<number | null>(null);
  businessLoading = signal(false);

  form: any = {
    decision: 'APPROVED',
    riskScore: 50,
    premiumAdjustmentPct: 0,
    comments: '',
    riskCategory: 'MODERATE',
    riskNotes: ''
  };

  riskCategories = ['LOW', 'MODERATE', 'HIGH', 'VERY_HIGH'];

  basePremium = signal<number>(0);

  finalPremium = computed(() => {
    const base = this.basePremium();
    const adj = this.form.premiumAdjustmentPct || 0;
    return base + (base * adj / 100);
  });

  riskAdjustmentAmount = computed(() => {
    const base = this.basePremium();
    const adj = this.form.premiumAdjustmentPct || 0;
    return base * adj / 100;
  });

  ngOnInit() {
    this.svc.getQueue().subscribe({
      next: (d) => {
        this.queue.set(d);
        this.loading.set(false);
      },
      error: (e) => {
        this.loadError.set(this.formatApiError(e, 'Failed to load underwriting queue.'));
        this.loading.set(false);
      }
    });
  }

  private formatApiError(e: any, fallback: string): string {
    const status = e?.status ? ` [${e.status}]` : '';
    const msg = e?.error?.message || e?.error?.error || e?.message || (typeof e?.error === 'string' ? e.error : null);
    return msg ? `${msg}${status}` : `${fallback}${status}`;
  }

  fetchRisk(appId: number) {
    this.svc.getRiskScore(appId).subscribe(r => {
      this.riskScores.update(m => ({ ...m, [appId]: r.riskScore }));
      if (this.expandedApp() === appId) {
        this.form.riskScore = r.riskScore;
        this.form.riskCategory = this.getRiskCategoryFromScore(r.riskScore);
        
        // Auto-apply recommendation logic after getting new score
        const app = this.selectedApp();
        if (app) {
          this.applyAiRecommendation({ ...app, aiRiskScore: r.riskScore });
        }
      }
    });
  }

  getRiskCategoryFromScore(score: number): string {
    if (score <= 25) return 'LOW';
    if (score <= 50) return 'MODERATE';
    if (score <= 75) return 'HIGH';
    return 'VERY_HIGH';
  }

  getRiskColor(score: number): string {
    if (score <= 25) return 'var(--ok)';
    if (score <= 50) return 'var(--warn)';
    if (score <= 75) return '#c65d00';
    return 'var(--burg-600)';
  }

  expandApplication(a: Application) {
    if (this.expandedApp() === a.id) {
      this.expandedApp.set(null);
      this.selectedBusiness.set(null);
      return;
    }

    this.expandedApp.set(a.id);
    this.selectedApp.set(a);
    this.form = {
      decision: 'APPROVED',
      riskScore: this.riskScores()[a.id] ?? 50,
      premiumAdjustmentPct: 0,
      comments: '',
      riskCategory: this.getRiskCategoryFromScore(this.riskScores()[a.id] ?? 50),
      riskNotes: a.riskNotes || ''
    };
    this.modalError.set('');
    this.basePremium.set(a.basePremium || a.annualPremium || 0);

    // Load business details
    if (a.businessId) {
      this.businessLoading.set(true);
      this.svc.getBusinessById(a.businessId).subscribe({
        next: (b) => {
          this.selectedBusiness.set(b);
          this.businessLoading.set(false);
        },
        error: (e) => {
          console.error('Error fetching business:', e);
          this.businessLoading.set(false);
          this.modalError.set('Could not retrieve business details. Please check if the business profile exists.');
        }
      });
    } else {
      this.modalError.set('No business associated with this application.');
    }

    // Auto-fetch risk score if not already loaded
    if (this.riskScores()[a.id] === undefined) {
      this.fetchRisk(a.id);
    }
  }

  applyAiRecommendation(a: Application) {
    const score = a.aiRiskScore ?? this.form.riskScore;
    if (score !== undefined) {
      this.form.riskScore = score;
      this.form.riskCategory = this.getRiskCategoryFromScore(score);
    }

    if (a.recommendedPremium && this.basePremium() > 0) {
      const diff = a.recommendedPremium - this.basePremium();
      this.form.premiumAdjustmentPct = +((diff / this.basePremium()) * 100).toFixed(2);
    } 
    // Fallback: Automatic heuristic based on risk score if no AI rec premium
    else if (score !== undefined) {
      if (score > 80) this.form.premiumAdjustmentPct = 35;
      else if (score > 65) this.form.premiumAdjustmentPct = 20;
      else if (score > 50) this.form.premiumAdjustmentPct = 10;
      else if (score > 35) this.form.premiumAdjustmentPct = 5;
      else if (score < 15) this.form.premiumAdjustmentPct = -5;
      else this.form.premiumAdjustmentPct = 0;
    }

    if (a.aiUnderwritingAnalysis) {
      this.form.comments = `AI Analysis: ${a.aiUnderwritingAnalysis}\n\nManual Adjudication: `;
    }
  }

  submitDecision() {
    if (this.form.decision === 'REJECTED' && !this.form.comments?.trim()) {
      this.modalError.set('Rejection reason is mandatory. Please provide a reason for rejection.');
      return;
    }

    this.saving.set(true);
    const appId = this.selectedApp()!.id;
    const payload = {
      applicationId: appId,
      decision: this.form.decision,
      riskScore: this.form.riskScore,
      premiumAdjustmentPct: this.form.premiumAdjustmentPct,
      comments: this.form.comments
    };
    this.svc.submitDecision(appId, payload).subscribe({
      next: () => {
        this.expandedApp.set(null);
        this.queue.update(q => q.filter(a => a.id !== appId));
        this.saving.set(false);
        this.selectedBusiness.set(null);
        this.success.set('Adjudication verdict executed successfully.');
        setTimeout(() => this.success.set(''), 5000);
      },
      error: (e) => {
        this.modalError.set(this.formatApiError(e, 'Unable to submit decision.'));
        this.saving.set(false);
        setTimeout(() => this.modalError.set(''), 8000);
      }
    });
  }

  openDocs(a: Application) {
    this.selectedApp.set(a);
    this.showDocs.set(true);
  }

  getIndustryIcon(type: string): string {
    const map: any = {
      'technology': 'computer', 'tech': 'computer',
      'manufacturing': 'factory', 'retail': 'storefront',
      'healthcare': 'local_hospital', 'finance': 'account_balance',
      'construction': 'construction', 'food': 'restaurant',
      'transportation': 'local_shipping', 'education': 'school',
    };
    const lower = (type || '').toLowerCase();
    for (const [key, icon] of Object.entries(map)) {
      if (lower.includes(key)) return icon as string;
    }
    return 'business';
  }

  statusClass(status: string): string {
    const s = (status || '').toUpperCase();
    if (s === 'SUBMITTED' || s === 'DRAFT') return 'badge-stone text-stone-600';
    if (s === 'UNDER_REVIEW') return 'badge-burgundy text-white';
    if (s === 'APPROVED') return 'badge-green text-green-700';
    if (s === 'REJECTED') return 'badge-red text-red-700';
    return 'badge-stone text-stone-500';
  }

  statusIcon(status: string): string {
    const s = (status || '').toUpperCase();
    if (s === 'SUBMITTED') return 'send';
    if (s === 'UNDER_REVIEW') return 'manage_search';
    if (s === 'APPROVED') return 'verified';
    if (s === 'REJECTED') return 'cancel';
    return 'help';
  }
}
