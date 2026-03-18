import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductsComponent } from './products.component';
import { ProductsService } from '../services/products.service';
import { AuthService } from '../../../core/services/auth.service';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

import { signal } from '@angular/core';

describe('ProductsComponent', () => {
    let component: ProductsComponent;
    let fixture: ComponentFixture<ProductsComponent>;
    let prodServiceSpy: any;
    let authServiceSpy: jasmine.SpyObj<AuthService>;
    let routerSpy: jasmine.SpyObj<Router>;

    beforeEach(async () => {
        prodServiceSpy = jasmine.createSpyObj('ProductsService', ['load', 'executeAction', 'update', 'create', 'deactivate']);
        prodServiceSpy.items = signal([]);
        prodServiceSpy.loading = signal(false);
        prodServiceSpy.error = signal('');
        prodServiceSpy.success = signal('');
        authServiceSpy = jasmine.createSpyObj('AuthService', ['isCustomer']);
        routerSpy = jasmine.createSpyObj('Router', ['navigate']);

        await TestBed.configureTestingModule({
            imports: [ProductsComponent, FormsModule],
            providers: [
                { provide: ProductsService, useValue: prodServiceSpy },
                { provide: AuthService, useValue: authServiceSpy },
                { provide: Router, useValue: routerSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ProductsComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('openModal should reset form and show modal', () => {
        component.showModal.set(false);
        component.openModal();
        expect(component.showModal()).toBeTrue();
        expect(component.editId()).toBeNull();
    });

    it('openApply should navigate if customer and product active', () => {
        authServiceSpy.isCustomer.and.returnValue(true);
        const product = { id: 1, isActive: true } as any;

        component.openApply(product);

        expect(routerSpy.navigate).toHaveBeenCalledWith(['/applications'], { queryParams: { new: 1, productId: 1 } });
    });

    it('save should set error if category is missing', () => {
        component.form = { category: '' };
        const errorSpy = spyOn(prodServiceSpy.error, 'set');
        component.save();
        expect(errorSpy).toHaveBeenCalledWith('Please select a category.');
    });
});
