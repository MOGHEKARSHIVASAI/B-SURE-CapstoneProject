import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FileUploadComponent } from './file-upload.component';
import { DocumentService } from '../../core/services/document.service';
import { of, throwError } from 'rxjs';
import { DocumentType } from '../../core/models/models';

describe('FileUploadComponent', () => {
    let component: FileUploadComponent;
    let fixture: ComponentFixture<FileUploadComponent>;
    let docServiceSpy: jasmine.SpyObj<DocumentService>;

    beforeEach(async () => {
        const spy = jasmine.createSpyObj('DocumentService', ['upload']);

        await TestBed.configureTestingModule({
            imports: [FileUploadComponent],
            providers: [
                { provide: DocumentService, useValue: spy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(FileUploadComponent);
        component = fixture.componentInstance;
        docServiceSpy = TestBed.inject(DocumentService) as jasmine.SpyObj<DocumentService>;
        
        component.documentType = 'BUSINESS_PROOF' as any;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('onFileSelected should call upload if file is selected', () => {
        const file = new File([''], 'test.png');
        const event = { target: { files: [file] } } as any;
        docServiceSpy.upload.and.returnValue(of({ id: 1 } as any));

        component.onFileSelected(event);

        expect(docServiceSpy.upload).toHaveBeenCalled();
        expect(component.isUploading()).toBeFalse();
    });

    it('onDrop should call upload', () => {
        const file = new File([''], 'test.png');
        const event = {
            preventDefault: () => {},
            dataTransfer: { files: [file] }
        } as any;
        docServiceSpy.upload.and.returnValue(of({ id: 1 } as any));

        component.onDrop(event);

        expect(docServiceSpy.upload).toHaveBeenCalled();
    });

    it('upload failure should set error', () => {
        spyOn(console, 'error');
        const file = new File([''], 'test.png');
        docServiceSpy.upload.and.returnValue(throwError(() => new Error('Upload Failed')));

        (component as any).upload(file);

        expect(component.error()).toBe('Upload failed. Check file size/type.');
        expect(component.isUploading()).toBeFalse();
    });
});
