import { TestBed } from '@angular/core/testing';
import { ProductsService } from './products.service';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { of, throwError } from 'rxjs';
import { Product } from '../../../core/models/models';

describe('ProductsService', () => {
    let service: ProductsService;
    let apiSpy: jasmine.SpyObj<ApiService>;
    let authSpy: jasmine.SpyObj<AuthService>;

    beforeEach(() => {
        const aSpy = jasmine.createSpyObj('ApiService', ['get', 'post', 'put', 'deleteText']);
        const auSpy = jasmine.createSpyObj('AuthService', ['isAdmin']);

        TestBed.configureTestingModule({
            providers: [
                ProductsService,
                { provide: ApiService, useValue: aSpy },
                { provide: AuthService, useValue: auSpy }
            ]
        });
        service = TestBed.inject(ProductsService);
        apiSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
        authSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('load should fetch all for admin if user is admin', () => {
        const mockData = [{ id: 1, active: true }] as any[];
        authSpy.isAdmin.and.returnValue(true);
        apiSpy.get.and.returnValue(of(mockData));

        service.load();
        expect(service.items()[0].id).toBe(1);
        expect(apiSpy.get).toHaveBeenCalledWith('/products/all');
    });

    it('load should fetch regular list if user is not admin', () => {
        const mockData = [{ id: 1, active: true }] as any[];
        authSpy.isAdmin.and.returnValue(false);
        apiSpy.get.and.returnValue(of(mockData));

        service.load();
        expect(service.items()[0].id).toBe(1);
        expect(apiSpy.get).toHaveBeenCalledWith('/products');
    });

    it('resolveActive should handle various input types', () => {
        expect((service as any).resolveActive({ active: true })).toBeTrue();
        expect((service as any).resolveActive({ active: false })).toBeFalse();
        expect((service as any).resolveActive({ active: 1 })).toBeTrue();
        expect((service as any).resolveActive({ active: 0 })).toBeFalse();
        expect((service as any).resolveActive({ active: 'true' })).toBeTrue();
        expect((service as any).resolveActive({ active: 'false' })).toBeFalse();
        expect((service as any).resolveActive({ active: 'active' })).toBeTrue();
    });

    it('create should call api post', () => {
        apiSpy.post.and.returnValue(of({ id: 1 }));
        service.create({ name: 'test' } as any).subscribe();
        expect(apiSpy.post).toHaveBeenCalledWith('/products', { name: 'test' } as any);
    });

    it('deactivate should call api deleteText', () => {
        apiSpy.deleteText.and.returnValue(of('deactivated'));
        service.deactivate(1).subscribe();
        expect(apiSpy.deleteText).toHaveBeenCalledWith('/products/1');
    });
});
