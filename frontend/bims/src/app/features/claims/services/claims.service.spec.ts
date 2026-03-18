import { TestBed } from '@angular/core/testing';
import { ClaimsService } from './claims.service';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { of, throwError } from 'rxjs';
import { Claim } from '../../../core/models/models';

describe('ClaimsService', () => {
    let service: ClaimsService;
    let apiSpy: jasmine.SpyObj<ApiService>;
    let authSpy: jasmine.SpyObj<AuthService>;

    beforeEach(() => {
        const aSpy = jasmine.createSpyObj('ApiService', ['get', 'post', 'put', 'delete']);
        const auSpy = jasmine.createSpyObj('AuthService', ['isCustomer', 'isOfficer']);

        TestBed.configureTestingModule({
            providers: [
                ClaimsService,
                { provide: ApiService, useValue: aSpy },
                { provide: AuthService, useValue: auSpy }
            ]
        });
        service = TestBed.inject(ClaimsService);
        apiSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
        authSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('load should fetch my claims if user is a customer', () => {
        const mockData: Claim[] = [{ id: 1 } as Claim];
        authSpy.isCustomer.and.returnValue(true);
        apiSpy.get.and.returnValue(of(mockData));

        service.load();
        expect(service.items()).toEqual(mockData);
        expect(apiSpy.get).toHaveBeenCalledWith('/claims');
    });

    it('load should fetch assigned claims if user is an officer', () => {
        const mockData: Claim[] = [{ id: 1 } as Claim];
        authSpy.isCustomer.and.returnValue(false);
        authSpy.isOfficer.and.returnValue(true);
        apiSpy.get.and.returnValue(of(mockData));

        service.load();
        expect(service.items()).toEqual(mockData);
        expect(apiSpy.get).toHaveBeenCalledWith('/claims/assigned');
    });

    it('load should fetch all claims if user is neither customer nor officer', () => {
        const mockData: Claim[] = [{ id: 1 } as Claim];
        authSpy.isCustomer.and.returnValue(false);
        authSpy.isOfficer.and.returnValue(false);
        apiSpy.get.and.returnValue(of(mockData));

        service.load();
        expect(service.items()).toEqual(mockData);
        expect(apiSpy.get).toHaveBeenCalledWith('/claims');
    });

    it('load should set error on failure', () => {
        authSpy.isCustomer.and.returnValue(false);
        authSpy.isOfficer.and.returnValue(false);
        apiSpy.get.and.returnValue(throwError(() => new Error('Error')));

        service.load();
        expect(service.error()).toBe('Failed to load claims.');
        expect(service.loading()).toBeFalse();
    });

    it('executeAction should set success message and reload', () => {
        const obs$ = of({ success: true });
        spyOn(service, 'load');

        service.executeAction(obs$, 'Success Message');

        expect(service.success()).toBe('Success Message');
        expect(service.load).toHaveBeenCalled();
    });

    it('file should call api post', () => {
        apiSpy.post.and.returnValue(of({ id: 1 }));
        service.file({ name: 'test' }).subscribe();
        expect(apiSpy.post).toHaveBeenCalledWith('/claims', { name: 'test' });
    });

    it('update should call api put', () => {
        apiSpy.put.and.returnValue(of({ id: 1 }));
        service.update(1, { name: 'updated' }).subscribe();
        expect(apiSpy.put).toHaveBeenCalledWith('/claims/1', { name: 'updated' });
    });

    it('delete should call api delete', () => {
        apiSpy.delete.and.returnValue(of(undefined));
        service.delete(1).subscribe();
        expect(apiSpy.delete).toHaveBeenCalledWith('/claims/1');
    });
});
