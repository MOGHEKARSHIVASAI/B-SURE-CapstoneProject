import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BusinessesComponent } from './businesses.component';
import { BusinessService } from '../services/business.service';
import { PoliciesService } from '../../policies/services/policies.service';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { signal } from '@angular/core';

describe('BusinessesComponent', () => {
    let component: BusinessesComponent;
    let fixture: ComponentFixture<BusinessesComponent>;
    let businessSvcSpy: any;
    let policySvcSpy: jasmine.SpyObj<PoliciesService>;

    beforeEach(async () => {
        businessSvcSpy = jasmine.createSpyObj('BusinessService', ['load', 'executeAction', 'update', 'createBusiness', 'delete']);
        businessSvcSpy.items = signal([]);
        businessSvcSpy.loading = signal(false);
        businessSvcSpy.error = signal('');
        businessSvcSpy.success = signal('');
        policySvcSpy = jasmine.createSpyObj('PoliciesService', ['getPoliciesByBusiness']);

        await TestBed.configureTestingModule({
            imports: [BusinessesComponent, FormsModule],
            providers: [
                { provide: BusinessService, useValue: businessSvcSpy },
                { provide: PoliciesService, useValue: policySvcSpy },
                { provide: AuthService, useValue: {} }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(BusinessesComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('openCreate should reset form and show it', () => {
        component.showForm.set(false);
        component.openCreate();
        expect(component.showForm()).toBeTrue();
        expect(component.editingId()).toBeNull();
    });

    it('save should set error if companyName is missing', () => {
        component.formData.companyName = '';
        const errorSpy = spyOn(businessSvcSpy.error, 'set');
        component.save();
        expect(errorSpy).toHaveBeenCalledWith('Company name is required.');
    });

    it('togglePolicies should fetch policies if not already in map', () => {
        const busId = 123;
        policySvcSpy.getPoliciesByBusiness.and.returnValue(of([]));
        
        component.togglePolicies(busId);

        expect(component.expandedBusinessId()).toBe(busId);
        expect(policySvcSpy.getPoliciesByBusiness).toHaveBeenCalledWith(busId);
    });
});
