import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UnderwritingComponent } from './underwriting.component';
import { UnderwritingService } from '../services/underwriting.service';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

describe('UnderwritingComponent', () => {
    let component: UnderwritingComponent;
    let fixture: ComponentFixture<UnderwritingComponent>;
    let underwritingSvcSpy: jasmine.SpyObj<UnderwritingService>;

    beforeEach(async () => {
        underwritingSvcSpy = jasmine.createSpyObj('UnderwritingService', ['getQueue', 'getRiskScore', 'getBusinessById', 'submitDecision']);

        await TestBed.configureTestingModule({
            imports: [UnderwritingComponent, FormsModule],
            providers: [
                { provide: UnderwritingService, useValue: underwritingSvcSpy },
                { provide: AuthService, useValue: {} }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(UnderwritingComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('ngOnInit should fetch queue', () => {
        underwritingSvcSpy.getQueue.and.returnValue(of([]));
        component.ngOnInit();
        expect(underwritingSvcSpy.getQueue).toHaveBeenCalled();
    });

    it('expandApplication should fetch business and risk core if not loaded', () => {
        const mockApp = { id: 1, businessId: 10, annualPremium: 100 } as any;
        underwritingSvcSpy.getBusinessById.and.returnValue(of({ id: 10 } as any));
        underwritingSvcSpy.getRiskScore.and.returnValue(of({ riskScore: 50 }));

        component.expandApplication(mockApp);

        expect(component.expandedApp()).toBe(1);
        expect(underwritingSvcSpy.getBusinessById).toHaveBeenCalledWith(10);
        expect(underwritingSvcSpy.getRiskScore).toHaveBeenCalledWith(1);
    });

    it('submitDecision should set modalError if rejected without comments', () => {
        component.selectedApp.set({ id: 1 } as any);
        component.form.decision = 'REJECTED';
        component.form.comments = '';

        component.submitDecision();

        expect(component.modalError()).toBe('Rejection reason is mandatory. Please provide a reason for rejection.');
    });
});
