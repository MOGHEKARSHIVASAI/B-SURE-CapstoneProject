import { Component, OnInit, signal, inject, computed } from '@angular/core';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BusinessService } from '../services/business.service';
import { PoliciesService } from '../../policies/services/policies.service';
import { AuthService } from '../../../core/services/auth.service';
import { Business, Policy } from '../../../core/models/models';

@Component({
    selector: 'app-businesses',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './businesses.component.html'
})
export class BusinessesComponent implements OnInit {
    svc = inject(BusinessService);
    private policySvc = inject(PoliciesService);
    auth = inject(AuthService);

    policiesMap = signal<Map<number, Policy[]>>(new Map());
    showForm = signal(false);
    editingId = signal<number | null>(null);
    expandedBusinessId = signal<number | null>(null);

    formData: any = {
        companyName: '',
        industryType: '',
        annualRevenue: 0,
        numEmployees: 0,
        addressLine1: '',
        addressLine2: '',
        city: '',
        state: '',
        postalCode: '',
        country: 'India',
        companyRegNumber: '',
        taxId: ''
    };

    ngOnInit() {
        this.svc.load();
    }

    // load and executeAction moved to service

    openCreate() {
        this.resetForm();
        this.editingId.set(null);
        this.showForm.set(true);
    }

    openEdit(b: Business) {
        this.formData = { ...b };
        this.editingId.set(b.id);
        this.showForm.set(true);
    }

    resetForm() {
        this.formData = {
            companyName: '',
            industryType: '',
            annualRevenue: 0,
            numEmployees: 0,
            addressLine1: '',
            addressLine2: '',
            city: '',
            state: '',
            postalCode: '',
            country: 'India',
            companyRegNumber: '',
            taxId: ''
        };
        this.svc.error.set('');
    }

    save() {
        if (!this.formData.companyName) { this.svc.error.set('Company name is required.'); return; }

        const id = this.editingId();
        const obs = id ? this.svc.update(id, this.formData) : this.svc.createBusiness(this.formData);

        this.svc.executeAction(obs, 'Business details saved successfully.', () => {
            this.showForm.set(false);
        });
    }

    delete(id: number) {
        if (confirm('Are you sure you want to delete this business? This may affect associated applications and policies.')) {
            this.svc.executeAction(this.svc.delete(id), 'Business deleted successfully.');
        }
    }

    togglePolicies(businessId: number) {
        if (this.expandedBusinessId() === businessId) {
            this.expandedBusinessId.set(null);
        } else {
            this.expandedBusinessId.set(businessId);
            if (!this.policiesMap().has(businessId)) {
                this.policySvc.getPoliciesByBusiness(businessId).subscribe(data => {
                    const map = new Map(this.policiesMap());
                    map.set(businessId, data);
                    this.policiesMap.set(map);
                });
            }
        }
    }

    getPolicies(businessId: number): Policy[] {
        return this.policiesMap().get(businessId) || [];
    }
}
