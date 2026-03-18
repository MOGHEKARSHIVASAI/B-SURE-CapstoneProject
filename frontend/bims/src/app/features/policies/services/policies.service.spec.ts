import { TestBed } from '@angular/core/testing';
import { PoliciesService } from './policies.service';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { of, throwError } from 'rxjs';
import { Policy } from '../../../core/models/models';

describe('PoliciesService', () => {
    let service: PoliciesService;
    let apiSpy: jasmine.SpyObj<ApiService>;
    let authSpy: jasmine.SpyObj<AuthService>;

    beforeEach(() => {
        const aSpy = jasmine.createSpyObj('ApiService', ['get', 'post', 'put']);
        const auSpy = jasmine.createSpyObj('AuthService', ['isCustomer']);

        TestBed.configureTestingModule({
            providers: [
                PoliciesService,
                { provide: ApiService, useValue: aSpy },
                { provide: AuthService, useValue: auSpy }
            ]
        });
        service = TestBed.inject(PoliciesService);
        apiSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
        authSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('load should fetch my policies if user is a customer', () => {
        const mockData: Policy[] = [{ id: 1 } as Policy];
        authSpy.isCustomer.and.returnValue(true);
        apiSpy.get.and.returnValue(of(mockData));

        service.load();
        expect(service.items()).toEqual(mockData);
        expect(apiSpy.get).toHaveBeenCalledWith('/policies');
    });

    it('load should fetch all policies if user is not a customer', () => {
        const mockData: Policy[] = [{ id: 1 } as Policy];
        authSpy.isCustomer.and.returnValue(false);
        apiSpy.get.and.returnValue(of(mockData));

        service.load();
        expect(service.items()).toEqual(mockData);
        expect(apiSpy.get).toHaveBeenCalledWith('/policies');
    });

    it('issue should call api post', () => {
        apiSpy.post.and.returnValue(of({ id: 1 }));
        service.issue(123, 456).subscribe();
        expect(apiSpy.post).toHaveBeenCalledWith('/policies/application/123/issue/456', {});
    });

    it('cancel should call api post with reason', () => {
        apiSpy.post.and.returnValue(of({ id: 1 }));
        service.cancel(1, 'Reason').subscribe();
        expect(apiSpy.post).toHaveBeenCalledWith('/policies/1/cancel', { cancellationReason: 'Reason' });
    });
});
