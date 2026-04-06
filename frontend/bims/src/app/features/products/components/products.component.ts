import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProductsService } from '../services/products.service';
import { AuthService } from '../../../core/services/auth.service';
import { Product } from '../../../core/models/models';

@Component({
  selector: 'app-products',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './products.component.html'
})
export class ProductsComponent implements OnInit {
  svc = inject(ProductsService);
  private router = inject(Router);
  auth = inject(AuthService);

  showModal = signal(false);
  editId = signal<number | null>(null);
  form: any = {};

  ngOnInit() {
    this.svc.load();
  }


  // Removed load() and resolveActive as they are in the service now
  getCategoryIcon(category: string): string {
    switch (category?.toUpperCase()) {
      case 'LIABILITY': return 'gavel';
      case 'PROPERTY': return 'home';
      case 'CYBER': return 'vpn_lock';
      case 'HEALTH': return 'medical_services';
      case 'VEHICLE': return 'directions_car';
      case 'MARINE': return 'directions_boat';
      default: return 'inventory_2';
    }
  }

  getCategoryColor(category: string): string {
    switch (category?.toUpperCase()) {
      case 'LIABILITY': return '#7A1C35';
      case 'PROPERTY': return '#A8958A';
      case 'CYBER': return '#3B0915';
      case 'HEALTH': return '#982340';
      case 'VEHICLE': return '#5A4038';
      case 'MARINE': return '#1E0309';
      default: return '#8C7A70';
    }
  }

  openModal() {
    this.form = { category: '', isActive: true, active: true };
    this.editId.set(null);
    this.showModal.set(true);
  }
  closeModal() { this.showModal.set(false); }

  editProduct(p: Product) {
    this.form = { ...p }; this.editId.set(p.id); this.showModal.set(true);
  }

  save() {
    if (!this.form.category) {
      this.svc.error.set('Please select a category.');
      return;
    }
    const payload = this.editId() ? this.form : { ...this.form, isActive: true, active: true };
    const obs = this.editId() ? this.svc.update(this.editId()!, payload) : this.svc.create(payload);

    this.svc.executeAction(obs, 'Product saved successfully.', () => this.closeModal());
  }

  deactivate(id: number) {
    if (!confirm('Deactivate this product?')) return;
    this.svc.executeAction(this.svc.deactivate(id), 'Product deactivated.');
  }

  openApply(p: Product) {
    if (!this.auth.isCustomer()) {
      alert('Only customers can apply for products.');
      return;
    }

    if (!p.isActive && !p.active) {
      alert('This product is currently inactive.');
      return;
    }

    this.router.navigate(['/applications'], { queryParams: { new: 1, productId: p.id } });
  }
}
