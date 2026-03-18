import { TestBed } from '@angular/core/testing';
import { UnderwritingService } from './underwriting.service';
import { ApiService } from '../../../core/services/api.service';
import { of } from 'rxjs';

describe('UnderwritingService', () => {
    let service: UnderwritingService;
    let apiSpy: jasmine.SpyObj<ApiService>;

    beforeEach(() => {
        const aSpy = jasmine.createSpyObj('ApiService', ['get', 'post']);

        TestBed.configureTestingModule({
            providers: [
                UnderwritingService,
                { provide: ApiService, useValue: aSpy }
            ]
        });
        service = TestBed.inject(UnderwritingService);
        apiSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('getQueue should call api get', () => {
        apiSpy.get.and.returnValue(of([]));
        service.getQueue().subscribe();
        expect(apiSpy.get).toHaveBeenCalledWith('/underwriting/queue');
    });

    it('submitDecision should call api post with appId and data', () => {
        apiSpy.post.and.returnValue(of({ id: 1 }));
        service.submitDecision(123, { decision: 'APPROVE' }).subscribe();
        expect(apiSpy.post).toHaveBeenCalledWith('/underwriting/application/123/decision', { decision: 'APPROVE' });
    });

    it('getRiskScore should call api get with appId', () => {
        apiSpy.get.and.returnValue(of({ riskScore: 80 }));
        service.getRiskScore(123).subscribe();
        expect(apiSpy.get).toHaveBeenCalledWith('/underwriting/application/123/risk-score');
    });

    it('getBusinessById should call api get with businessId', () => {
        apiSpy.get.and.returnValue(of({ id: 456 }));
        service.getBusinessById(456).subscribe();
        expect(apiSpy.get).toHaveBeenCalledWith('/businesses/456');
    });
});
