import { Injectable, inject } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Payment } from '../../../core/models/models';

@Injectable({ providedIn: 'root' })
export class PaymentsService {
  private api = inject(ApiService);
  getAll() { return this.api.get<Payment[]>('/payments'); }
  getMyPayments() { return this.api.get<Payment[]>('/payments'); }
  makePayment(data: any) { return this.api.post<Payment>('/payments', data); }
  getByApplication(appId: number) { return this.api.get<Payment[]>(`/payments/application/${appId}`); }
  getByStatus(status: string) { return this.api.get<Payment[]>(`/payments/status/${status}`); }
}
