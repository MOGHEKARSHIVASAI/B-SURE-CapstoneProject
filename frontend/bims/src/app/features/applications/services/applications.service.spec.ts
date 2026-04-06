import { TestBed } from '@angular/core/testing';
import { ApplicationsService } from './applications.service';
import { ApiService } from '../../../core/services/api.service';
import { AuthService } from '../../../core/services/auth.service';
import { of, throwError } from 'rxjs';
import { Application } from '../../../core/models/models';

describe('ApplicationsService', () => {
  let service: ApplicationsService;
  let apiSpy: jasmine.SpyObj<ApiService>;
  let authSpy: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    const aSpy = jasmine.createSpyObj('ApiService', ['get', 'post', 'put', 'delete']);
    const auSpy = jasmine.createSpyObj('AuthService', ['isCustomer']);

    TestBed.configureTestingModule({
      providers: [
        ApplicationsService,
        { provide: ApiService, useValue: aSpy },
        { provide: AuthService, useValue: auSpy }
      ]
    });
    service = TestBed.inject(ApplicationsService);
    apiSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    authSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('load should fetch my applications if user is a customer', () => {
    const mockData: Application[] = [{ id: 1 } as Application];
    authSpy.isCustomer.and.returnValue(true);
    apiSpy.get.and.returnValue(of(mockData));

    service.load().subscribe(data => {
      expect(data).toEqual(mockData);
      expect(service.items()).toEqual(mockData);
      expect(service.loading()).toBeFalse();
    });

    expect(apiSpy.get).toHaveBeenCalledWith('/applications');
  });

  it('load should fetch all applications if user is not a customer', () => {
    const mockData: Application[] = [{ id: 1 } as Application];
    authSpy.isCustomer.and.returnValue(false);
    apiSpy.get.and.returnValue(of(mockData));

    service.load().subscribe(data => {
      expect(data).toEqual(mockData);
      expect(service.items()).toEqual(mockData);
      expect(service.loading()).toBeFalse();
    });

    expect(apiSpy.get).toHaveBeenCalledWith('/applications');
  });

  it('load should set error on failure', () => {
    authSpy.isCustomer.and.returnValue(false);
    apiSpy.get.and.returnValue(throwError(() => new Error('Search Error')));

    service.load().subscribe({
        error: () => {
            expect(service.error()).toBe('Failed to load applications.');
            expect(service.loading()).toBeFalse();
        }
    });

    expect(apiSpy.get).toHaveBeenCalledWith('/applications');
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
    expect(apiSpy.get).toHaveBeenCalledWith('/applications/1');
  });

  it('create should call api post', () => {
    apiSpy.post.and.returnValue(of({ id: 1 }));
    service.create({ name: 'test' }).subscribe();
    expect(apiSpy.post).toHaveBeenCalledWith('/applications', { name: 'test' });
  });

  it('update should call api put', () => {
    apiSpy.put.and.returnValue(of({ id: 1 }));
    service.update(1, { name: 'updated' }).subscribe();
    expect(apiSpy.put).toHaveBeenCalledWith('/applications/1', { name: 'updated' });
  });

  it('delete should call api delete', () => {
    apiSpy.delete.and.returnValue(of(undefined));
    service.delete(1).subscribe();
    expect(apiSpy.delete).toHaveBeenCalledWith('/applications/1');
  });
});
