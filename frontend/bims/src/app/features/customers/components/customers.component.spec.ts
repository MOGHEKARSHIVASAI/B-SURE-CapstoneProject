import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CustomersComponent } from './customers.component';
import { BusinessService } from '../services/business.service';
import { of } from 'rxjs';

describe('CustomersComponent', () => {
    let component: CustomersComponent;
    let fixture: ComponentFixture<CustomersComponent>;
    let businessSvcSpy: jasmine.SpyObj<BusinessService>;

    beforeEach(async () => {
        businessSvcSpy = jasmine.createSpyObj('BusinessService', ['getAll']);

        await TestBed.configureTestingModule({
            imports: [CustomersComponent],
            providers: [{ provide: BusinessService, useValue: businessSvcSpy }]
        }).compileComponents();

        fixture = TestBed.createComponent(CustomersComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('ngOnInit should fetch customers', () => {
        const mockData = [{ id: 1 } as any];
        businessSvcSpy.getAll.and.returnValue(of(mockData));

        component.ngOnInit();

        expect(businessSvcSpy.getAll).toHaveBeenCalled();
        expect(component.customers()).toEqual(mockData);
        expect(component.loading()).toBeFalse();
    });
});
