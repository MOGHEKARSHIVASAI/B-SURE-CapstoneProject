import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PoliciesComponent } from './policies.component';
import { PoliciesService } from '../services/policies.service';
import { ApplicationsService } from '../../applications/services/applications.service';
import { UsersService } from '../../users/services/users.service';
import { PaymentsService } from '../../payments/services/payments.service';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { signal } from '@angular/core';

describe('PoliciesComponent', () => {
    let component: PoliciesComponent;
    let fixture: ComponentFixture<PoliciesComponent>;
    let policySvcSpy: any;
    let appSvcSpy: jasmine.SpyObj<ApplicationsService>;
    let userSvcSpy: jasmine.SpyObj<UsersService>;
    let paymentSvcSpy: jasmine.SpyObj<PaymentsService>;
    let authSvcSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        policySvcSpy = jasmine.createSpyObj('PoliciesService', ['load', 'executeAction', 'issue', 'cancel', 'suspend', 'reactivate']);
        policySvcSpy.items = signal([]);
        policySvcSpy.loading = signal(false);
        policySvcSpy.error = signal('');
        policySvcSpy.success = signal('');
        appSvcSpy = jasmine.createSpyObj('ApplicationsService', ['getByStatus']);
        userSvcSpy = jasmine.createSpyObj('UsersService', ['getByRole']);
        paymentSvcSpy = jasmine.createSpyObj('PaymentsService', ['getByApplication']);
        authSvcSpy = jasmine.createSpyObj('AuthService', ['isAdmin']);

        await TestBed.configureTestingModule({
            imports: [PoliciesComponent, FormsModule],
            providers: [
                { provide: PoliciesService, useValue: policySvcSpy },
                { provide: ApplicationsService, useValue: appSvcSpy },
                { provide: UsersService, useValue: userSvcSpy },
                { provide: PaymentsService, useValue: paymentSvcSpy },
                { provide: AuthService, useValue: authSvcSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(PoliciesComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('ngOnInit should load policies', () => {
        authSvcSpy.isAdmin.and.returnValue(false);
        component.ngOnInit();
        expect(policySvcSpy.load).toHaveBeenCalled();
    });

    it('issue should set error if no app is selected', () => {
        component.issueAppId = '';
        component.issue();
        expect(component.issueError()).toBe('Please select an application.');
    });

    it('statusClass should return correct badge class', () => {
        expect(component.statusClass('ACTIVE')).toBe('badge-green');
        expect(component.statusClass('CANCELLED')).toBe('badge-red');
    });
});
