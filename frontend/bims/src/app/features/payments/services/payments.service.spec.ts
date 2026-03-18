import { TestBed } from '@angular/core/testing';
import { PaymentsService } from './payments.service';
import { ApiService } from '../../../core/services/api.service';
import { of } from 'rxjs';

describe('PaymentsService', () => {
    let service: PaymentsService;
    let apiSpy: jasmine.SpyObj<ApiService>;

    beforeEach(() => {
        const aSpy = jasmine.createSpyObj('ApiService', ['get', 'post']);

        TestBed.configureTestingModule({
            providers: [
                PaymentsService,
                { provide: ApiService, useValue: aSpy }
            ]
        });
        service = TestBed.inject(PaymentsService);
        apiSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('getAll should call api get', () => {
        apiSpy.get.and.returnValue(of([]));
        service.getAll().subscribe();
        expect(apiSpy.get).toHaveBeenCalledWith('/payments');
    });

    it('makePayment should call api post', () => {
        apiSpy.post.and.returnValue(of({ id: 1 }));
        service.makePayment({ amount: 100 }).subscribe();
        expect(apiSpy.post).toHaveBeenCalledWith('/payments', { amount: 100 });
    });

    it('getByApplication should call api get with appId', () => {
        apiSpy.get.and.returnValue(of([]));
        service.getByApplication(123).subscribe();
        expect(apiSpy.get).toHaveBeenCalledWith('/payments/application/123');
    });
});
