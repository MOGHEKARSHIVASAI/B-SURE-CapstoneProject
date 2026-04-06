import { Injectable, inject } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Application, Decision, Business } from '../../../core/models/models';

@Injectable({ providedIn: 'root' })
export class UnderwritingService {
  private api = inject(ApiService);
  getQueue() { return this.api.get<Application[]>('/underwriting/queue'); }
  getAssigned() { return this.api.get<Application[]>('/underwriting/assigned'); }
  submitDecision(appId: number, data: any) { return this.api.post<Decision>(`/underwriting/application/${appId}/decision`, data); }
  getRiskScore(appId: number) { return this.api.get<{ riskScore: number }>(`/underwriting/application/${appId}/risk-score`); }
  getAllDecisions() { return this.api.get<Decision[]>('/underwriting/decisions'); }
  getDecisionById(decisionId: number) { return this.api.get<Decision>(`/underwriting/decisions/${decisionId}`); }
  getBusinessById(businessId: number) { return this.api.get<Business>(`/businesses/${businessId}`); }
  getApplicationById(appId: number) { return this.api.get<Application>(`/applications/${appId}`); }
}
