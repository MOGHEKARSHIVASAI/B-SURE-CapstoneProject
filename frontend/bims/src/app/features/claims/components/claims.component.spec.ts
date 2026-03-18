import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClaimsComponent } from './claims.component';
import { ClaimsService } from '../services/claims.service';
import { PoliciesService } from '../../policies/services/policies.service';
import { UsersService } from '../../users/services/users.service';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { signal } from '@angular/core';

describe('ClaimsComponent', () => {
    let component: ClaimsComponent;
    let fixture: ComponentFixture<ClaimsComponent>;
    let claimSvcSpy: any;
    let policySvcSpy: jasmine.SpyObj<PoliciesService>;
    let userSvcSpy: jasmine.SpyObj<UsersService>;
    let authSvcSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        claimSvcSpy = jasmine.createSpyObj('ClaimsService', ['load', 'file', 'update', 'submit', 'executeAction', 'assign', 'investigate', 'approve', 'reject', 'settle', 'appeal', 'delete']);
        claimSvcSpy.items = signal([]);
        claimSvcSpy.loading = signal(false);
        claimSvcSpy.error = signal('');
        claimSvcSpy.success = signal('');
        policySvcSpy = jasmine.createSpyObj('PoliciesService', ['getMyPolicies']);
        userSvcSpy = jasmine.createSpyObj('UsersService', ['getByRole']);
        authSvcSpy = jasmine.createSpyObj('AuthService', ['isAdmin', 'isCustomer', 'isOfficer']);

        await TestBed.configureTestingModule({
            imports: [ClaimsComponent, FormsModule],
            providers: [
                { provide: ClaimsService, useValue: claimSvcSpy },
                { provide: PoliciesService, useValue: policySvcSpy },
                { provide: UsersService, useValue: userSvcSpy },
                { provide: AuthService, useValue: authSvcSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ClaimsComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('openFileWizard should initialize form', () => {
        component.myPolicies.set([{ id: 1, policyNumber: 'P1' } as any]);
        authSvcSpy.isCustomer.and.returnValue(true);

        component.openFileWizard();

        expect(component.showFile()).toBeTrue();
        expect(component.fileForm.policyId).toBe(1);
    });

    it('saveDraftAndNext should call file if no savedClaimId', () => {
        component.fileForm = { policyId: 1, claimedAmount: 100, incidentDate: '2024-01-01', incidentDescription: 'desc' };
        claimSvcSpy.file.and.returnValue(of({ id: 100 }));

        component.saveDraftAndNext();

        expect(claimSvcSpy.file).toHaveBeenCalled();
        expect(component.savedClaimId()).toBe(100);
        expect(component.formStep()).toBe(2);
    });

    it('statusClass should return correct badge class', () => {
        expect(component.statusClass('SUBMITTED')).toBe('badge-blue');
        expect(component.statusClass('APPROVED')).toBe('badge-green');
    });
});
