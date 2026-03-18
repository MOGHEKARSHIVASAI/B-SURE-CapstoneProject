import { ComponentFixture, TestBed } from '@angular/core/testing';
import { PaymentsComponent } from './payments.component';
import { PaymentsService } from '../services/payments.service';
import { ApplicationsService } from '../../applications/services/applications.service';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

describe('PaymentsComponent', () => {
    let component: PaymentsComponent;
    let fixture: ComponentFixture<PaymentsComponent>;
    let paymentSvcSpy: jasmine.SpyObj<PaymentsService>;
    let appSvcSpy: jasmine.SpyObj<ApplicationsService>;
    let authSvcSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        paymentSvcSpy = jasmine.createSpyObj('PaymentsService', ['getMyPayments', 'getAll', 'makePayment']);
        appSvcSpy = jasmine.createSpyObj('ApplicationsService', ['getMyApplications']);
        authSvcSpy = jasmine.createSpyObj('AuthService', ['isCustomer']);

        await TestBed.configureTestingModule({
            imports: [PaymentsComponent, FormsModule],
            providers: [
                { provide: PaymentsService, useValue: paymentSvcSpy },
                { provide: ApplicationsService, useValue: appSvcSpy },
                { provide: AuthService, useValue: authSvcSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(PaymentsComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('load should fetch payments', () => {
        authSvcSpy.isCustomer.and.returnValue(false);
        const mockData = [{ id: 1 } as any];
        paymentSvcSpy.getAll.and.returnValue(of(mockData));

        component.load();

        expect(paymentSvcSpy.getAll).toHaveBeenCalled();
        expect(component.payments()).toEqual(mockData);
    });

    it('pay should call makePayment and and hide modal on success', () => {
        component.form = { applicationId: 1, amount: 100 };
        paymentSvcSpy.makePayment.and.returnValue(of({ id: 1 } as any));
        authSvcSpy.isCustomer.and.returnValue(false);
        paymentSvcSpy.getAll.and.returnValue(of([]));

        component.pay();

        expect(paymentSvcSpy.makePayment).toHaveBeenCalled();
        expect(component.showModal()).toBeFalse();
    });

    it('statusClass should return correct badge class', () => {
        expect(component.statusClass('PAID')).toBe('badge-green');
        expect(component.statusClass('FAILED')).toBe('badge-red');
    });
});
