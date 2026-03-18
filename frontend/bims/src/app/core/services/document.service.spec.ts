import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { DocumentService } from './document.service';
import { API_BASE } from './api.service';
import { Doc, DocumentType } from '../models/models';

describe('DocumentService', () => {
    let service: DocumentService;
    let httpMock: HttpTestingController;
    const baseUrl = `${API_BASE}/documents`;

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [DocumentService]
        });
        service = TestBed.inject(DocumentService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should upload a file correctly', () => {
        const file = new File(['test content'], 'test.txt', { type: 'text/plain' });
        const type: DocumentType = 'BUSINESS_PROOF' as any;
        const mockDoc: Doc = { id: 1, fileName: 'test.txt', fileType: 'text/plain', documentType: type, uploadedAt: new Date().toISOString() } as Doc;

        service.upload(file, type, 123).subscribe(res => {
            expect(res).toEqual(mockDoc);
        });

        const req = httpMock.expectOne(`${baseUrl}/upload`);
        expect(req.request.method).toBe('POST');
        expect(req.request.body instanceof FormData).toBeTrue();
        req.flush(mockDoc);
    });

    it('should get documents by application ID', () => {
        const mockDocs: Doc[] = [{ id: 1 } as Doc];
        service.getByApplication(123).subscribe(res => {
            expect(res).toEqual(mockDocs);
        });

        const req = httpMock.expectOne(`${baseUrl}/application/123`);
        expect(req.request.method).toBe('GET');
        req.flush(mockDocs);
    });

    it('should get documents by claim ID', () => {
        const mockDocs: Doc[] = [{ id: 1 } as Doc];
        service.getByClaim(123).subscribe(res => {
            expect(res).toEqual(mockDocs);
        });

        const req = httpMock.expectOne(`${baseUrl}/claim/123`);
        expect(req.request.method).toBe('GET');
        req.flush(mockDocs);
    });

    it('should download a document', () => {
        const blob = new Blob(['test content']);
        service.download(1).subscribe(res => {
            expect(res).toEqual(blob);
        });

        const req = httpMock.expectOne(`${baseUrl}/view/1`);
        expect(req.request.method).toBe('GET');
        expect(req.request.responseType).toBe('blob');
        req.flush(blob);
    });
});
