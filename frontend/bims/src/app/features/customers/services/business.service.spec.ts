import { TestBed } from '@angular/core/testing';
import { BusinessService } from './business.service';
import { ApiService } from '../../../core/services/api.service';
import { of, throwError } from 'rxjs';
import { Business } from '../../../core/models/models';

describe('BusinessService', () => {
  let service: BusinessService;
  let apiSpy: jasmine.SpyObj<ApiService>;

  beforeEach(() => {
    const aSpy = jasmine.createSpyObj('ApiService', ['get', 'post', 'put', 'delete']);

    TestBed.configureTestingModule({
      providers: [
        BusinessService,
        { provide: ApiService, useValue: aSpy }
      ]
    });
    service = TestBed.inject(BusinessService);
    apiSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('load should fetch businesses', () => {
    const mockData: Business[] = [{ id: 1 } as Business];
    apiSpy.get.and.returnValue(of(mockData));

    service.load();
    expect(service.items()).toEqual(mockData);
    expect(apiSpy.get).toHaveBeenCalledWith('/businesses');
  });

  it('load should set error on failure', () => {
    apiSpy.get.and.returnValue(throwError(() => new Error('Search Error')));

    service.load();
    expect(service.error()).toBe('Failed to load businesses.');
    expect(service.loading()).toBeFalse();
  });

  it('executeAction should set success message and reload', () => {
    const obs$ = of({ success: true });
    spyOn(service, 'load');

    service.executeAction(obs$, 'Success Message');

    expect(service.success()).toBe('Success Message');
    expect(service.load).toHaveBeenCalled();
  });

  it('getById should call api get', () => {
    apiSpy.get.and.returnValue(of({ id: 1 }));
    service.getById(1).subscribe();
    expect(apiSpy.get).toHaveBeenCalledWith('/businesses/1');
  });

  it('createBusiness should call api post', () => {
    apiSpy.post.and.returnValue(of({ id: 1 }));
    service.createBusiness({ name: 'test' }).subscribe();
    expect(apiSpy.post).toHaveBeenCalledWith('/businesses', { name: 'test' });
  });

  it('update should call api put', () => {
    apiSpy.put.and.returnValue(of({ id: 1 }));
    service.update(1, { name: 'updated' }).subscribe();
    expect(apiSpy.put).toHaveBeenCalledWith('/businesses/1', { name: 'updated' });
  });

  it('delete should call api delete', () => {
    apiSpy.delete.and.returnValue(of(undefined));
    service.delete(1).subscribe();
    expect(apiSpy.delete).toHaveBeenCalledWith('/businesses/1');
  });

  it('getMyProfile should return the first business in the list', () => {
    const mockData: Business[] = [{ id: 1, name: 'B1' } as any, { id: 2, name: 'B2' } as any];
    apiSpy.get.and.returnValue(of(mockData));

    service.getMyProfile().subscribe(res => {
        expect(res).toEqual(mockData[0]);
    });
  });
});
