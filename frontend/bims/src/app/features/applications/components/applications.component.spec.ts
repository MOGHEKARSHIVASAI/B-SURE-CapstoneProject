import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ApplicationsComponent } from './applications.component';
import { ApplicationsService } from '../services/applications.service';
import { ProductsService } from '../../products/services/products.service';
import { UsersService } from '../../users/services/users.service';
import { BusinessService } from '../../customers/services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { RouterTestingModule } from '@angular/router/testing';
import { ActivatedRoute, convertToParamMap } from '@angular/router';
import { of } from 'rxjs';

import { signal } from '@angular/core';

describe('ApplicationsComponent', () => {
    let component: ApplicationsComponent;
    let fixture: ComponentFixture<ApplicationsComponent>;
    let appSvcSpy: any;
    let prodSvcSpy: jasmine.SpyObj<ProductsService>;
    let bizSvcSpy: jasmine.SpyObj<BusinessService>;
    let userSvcSpy: jasmine.SpyObj<UsersService>;
    let authSvcSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        appSvcSpy = jasmine.createSpyObj('ApplicationsService', ['load', 'getLatestDecision', 'executeAction', 'delete', 'submit', 'acceptDecision', 'rejectDecision', 'assign']);
        appSvcSpy.items = signal([]);
        appSvcSpy.loading = signal(false);
        appSvcSpy.error = signal('');
        appSvcSpy.success = signal('');
        prodSvcSpy = jasmine.createSpyObj('ProductsService', ['getAll']);
        bizSvcSpy = jasmine.createSpyObj('BusinessService', ['getUsersBusinesses']);
        userSvcSpy = jasmine.createSpyObj('UsersService', ['getByRole']);
        authSvcSpy = jasmine.createSpyObj('AuthService', ['isCustomer', 'isAdmin']);

        await TestBed.configureTestingModule({
            imports: [ApplicationsComponent, RouterTestingModule],
            providers: [
                { provide: ApplicationsService, useValue: appSvcSpy },
                { provide: ProductsService, useValue: prodSvcSpy },
                { provide: BusinessService, useValue: bizSvcSpy },
                { provide: UsersService, useValue: userSvcSpy },
                { provide: AuthService, useValue: authSvcSpy },
                {
                    provide: ActivatedRoute,
                    useValue: {
                        queryParamMap: of(convertToParamMap({}))
                    }
                }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ApplicationsComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('refresh should load products and applications', () => {
        prodSvcSpy.getAll.and.returnValue(of([]));
        appSvcSpy.load.and.returnValue(of([]));
        authSvcSpy.isCustomer.and.returnValue(false);

        component.refresh();

        expect(prodSvcSpy.getAll).toHaveBeenCalled();
        expect(appSvcSpy.load).toHaveBeenCalled();
    });

    it('openWizard should set showWizard to true', () => {
        authSvcSpy.isCustomer.and.returnValue(true);
        component.myBusinesses.set([{ id: 1 } as any]);
        
        component.openWizard();

        expect(component.showWizard()).toBeTrue();
    });

    it('submit should call service submit if confirmed and has docs', () => {
        const mockApp = { id: 1, documentCount: 1 } as any;
        appSvcSpy.items.and.returnValue([mockApp]);
        spyOn(window, 'confirm').and.returnValue(true);
        appSvcSpy.submit.and.returnValue(of({}));

        component.submit(1);

        expect(appSvcSpy.executeAction).toHaveBeenCalled();
    });
});
