import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationWizardComponent } from './application-wizard.component';
import { ApplicationsService } from '../services/applications.service';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { signal } from '@angular/core';

describe('ApplicationWizardComponent', () => {
    let component: ApplicationWizardComponent;
    let fixture: ComponentFixture<ApplicationWizardComponent>;
    let appSvcSpy: any;

    beforeEach(async () => {
        appSvcSpy = jasmine.createSpyObj('ApplicationsService', ['update', 'create', 'submit', 'executeAction']);
        appSvcSpy.loading = signal(false);
        appSvcSpy.error = signal('');
        appSvcSpy.success = signal('');

        await TestBed.configureTestingModule({
            imports: [ApplicationWizardComponent, FormsModule],
            providers: [
                { provide: ApplicationsService, useValue: appSvcSpy },
                { provide: AuthService, useValue: {} }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ApplicationWizardComponent);
        component = fixture.componentInstance;
        component.products = [{ id: 1, productName: 'Test', basePremiumRate: 0.1 } as any];
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('calcPremium should return correct premium', () => {
        component.form.productId = 1;
        component.form.coverageAmount = 1000;
        expect(component.calcPremium()).toBe(100);
    });

    it('saveDraft should call create if no savedId', () => {
        component.form.productId = 1;
        component.form.coverageStartDate = '2024-01-01';
        component.form.coverageEndDate = '2025-01-01';
        appSvcSpy.create.and.returnValue(of({ id: 100 }));

        component.saveDraft();

        expect(appSvcSpy.create).toHaveBeenCalled();
        expect(component.savedId()).toBe(100);
    });

    it('saveDraft with nextStep should increase formStep', () => {
        component.form.productId = 1;
        component.form.coverageStartDate = '2024-01-01';
        component.form.coverageEndDate = '2025-01-01';
        appSvcSpy.create.and.returnValue(of({ id: 100 }));

        component.saveDraft(true);

        expect(component.formStep()).toBe(2);
    });
});
