import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService, API_BASE } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });
    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call get correctly', () => {
    service.get('/test').subscribe();
    const req = httpMock.expectOne(`${API_BASE}/test`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should call post correctly', () => {
    service.post('/test', { data: 'test' }).subscribe();
    const req = httpMock.expectOne(`${API_BASE}/test`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ data: 'test' });
    req.flush({});
  });

  it('should call put correctly', () => {
    service.put('/test', { data: 'test' }).subscribe();
    const req = httpMock.expectOne(`${API_BASE}/test`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ data: 'test' });
    req.flush({});
  });

  it('should call delete correctly', () => {
    service.delete('/test').subscribe();
    const req = httpMock.expectOne(`${API_BASE}/test`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
  });
});
