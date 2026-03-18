import { ComponentFixture, TestBed } from '@angular/core/testing';
import { DocumentListComponent } from './document-list.component';
import { DocumentService } from '../../core/services/document.service';
import { of } from 'rxjs';
import { Doc } from '../../core/models/models';

describe('DocumentListComponent', () => {
    let component: DocumentListComponent;
    let fixture: ComponentFixture<DocumentListComponent>;
    let docServiceSpy: jasmine.SpyObj<DocumentService>;

    beforeEach(async () => {
        const spy = jasmine.createSpyObj('DocumentService', ['getByApplication', 'getByClaim', 'download']);

        await TestBed.configureTestingModule({
            imports: [DocumentListComponent],
            providers: [
                { provide: DocumentService, useValue: spy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(DocumentListComponent);
        component = fixture.componentInstance;
        docServiceSpy = TestBed.inject(DocumentService) as jasmine.SpyObj<DocumentService>;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should refresh docs on init using applicationId', () => {
        const mockDocs: Doc[] = [{ id: 1, fileName: 'test.pdf' } as Doc];
        component.applicationId = 123;
        docServiceSpy.getByApplication.and.returnValue(of(mockDocs));

        component.ngOnInit();

        expect(docServiceSpy.getByApplication).toHaveBeenCalledWith(123);
        expect(component.docs()).toEqual(mockDocs);
        expect(component.loading()).toBeFalse();
    });

    it('should refresh docs using claimId if applicationId is not provided', () => {
        const mockDocs: Doc[] = [{ id: 1, fileName: 'test.pdf' } as Doc];
        component.claimId = 456;
        docServiceSpy.getByClaim.and.returnValue(of(mockDocs));

        component.ngOnInit();

        expect(docServiceSpy.getByClaim).toHaveBeenCalledWith(456);
        expect(component.docs()).toEqual(mockDocs);
    });

    it('onDocUploaded should add new doc and emit count', () => {
        const newDoc = { id: 2, fileName: 'new.pdf' } as Doc;
        spyOn(component.countChanged, 'emit');
        component.docs.set([]);

        component.onDocUploaded(newDoc);

        expect(component.docs()).toContain(newDoc);
        expect(component.countChanged.emit).toHaveBeenCalledWith(1);
    });

    it('viewDoc should call service download and open url', () => {
        const mockDoc = { id: 1, fileName: 'test.pdf' } as Doc;
        const mockBlob = new Blob([''], { type: 'application/pdf' });
        docServiceSpy.download.and.returnValue(of(mockBlob));
        
        spyOn(window.URL, 'createObjectURL').and.returnValue('mock-url');
        spyOn(window, 'open');

        component.viewDoc(mockDoc);

        expect(docServiceSpy.download).toHaveBeenCalledWith(1);
        expect(window.open).toHaveBeenCalledWith('mock-url', '_blank');
    });
});
