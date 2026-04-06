import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BusinessService } from '../services/business.service';
import { Business } from '../../../core/models/models';

@Component({
  selector: 'app-customers',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './customers.component.html'
})
export class CustomersComponent implements OnInit {
  private svc = inject(BusinessService);
  customers = signal<Business[]>([]);
  loading = signal(true);

  ngOnInit() {
    this.svc.getAll().subscribe({
      next: (data) => { this.customers.set(data); this.loading.set(false); },
      error: () => this.loading.set(false)
    });
  }
}
