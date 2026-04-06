import { Component, EventEmitter, Input, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentService } from '../../core/services/document.service';
import { DocumentType } from '../../core/models/models';

@Component({
    selector: 'app-file-upload',
    standalone: true,
    imports: [CommonModule],
    template: `
    <div class="upload-wrapper" [class.is-uploading]="isUploading()" [class.has-error]="error()" [class.dropping]="isDropping"
      (dragover)="onDragOver($event)" 
      (dragleave)="onDragLeave($event)" 
      (drop)="onDrop($event)">
      
      <div class="upload-inner" (click)="fileInput.click()">
        <div class="icon-container">
          @if (isUploading()) {
            <div class="upload-spinner"></div>
          } @else if (selectedFile) {
             <span class="material-symbols-outlined success-icon">task</span>
          } @else {
            <span class="material-symbols-outlined main-icon">cloud_upload</span>
          }
        </div>

        <div class="info-container">
          @if (selectedFile) {
            <div class="file-info-preview">
              <span class="file-name">{{ selectedFile.name }}</span>
              <span class="file-size">{{ (selectedFile.size / 1024).toFixed(1) }} KB</span>
            </div>
          } @else {
            <div class="prompt-text">
              <span class="primary-text">Upload {{ label }}</span>
              <span class="secondary-text">Drag & drop or <span class="browse">browse</span></span>
            </div>
          }
        </div>
        
        <input #fileInput type="file" [accept]="accept" (change)="onFileSelected($event)" hidden>
      </div>

      @if (isUploading()) {
        <div class="progress-container">
          <div class="progress-bar-fill"></div>
        </div>
      }

      @if (error()) {
        <div class="error-strip">
          <span class="material-symbols-outlined">error</span>
          <span>{{ error() }}</span>
        </div>
      }
    </div>
  `,
    styles: [`
    .upload-wrapper {
      position: relative;
      border: 1.5px dashed var(--border);
      border-radius: 16px;
      padding: 16px;
      background: var(--surface-2);
      transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      cursor: pointer;
      overflow: hidden;
      min-height: 80px;
      display: flex;
      flex-direction: column;
      justify-content: center;
    }
    .upload-wrapper:hover {
      border-color: var(--burg-400);
      background: var(--burg-50);
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(109, 33, 40, 0.08);
    }
    .upload-wrapper.dropping {
      border-color: var(--burg-600);
      background: var(--burg-100);
      border-style: solid;
      transform: scale(1.02);
    }
    .upload-wrapper.is-uploading {
      pointer-events: none;
      border-color: var(--burg-200);
    }
    .upload-wrapper.has-error {
      border-color: var(--ruby-400);
      background: var(--ruby-50);
    }

    .upload-inner {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .icon-container {
      width: 48px;
      height: 48px;
      border-radius: 12px;
      background: white;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 6px rgba(0,0,0,0.05);
      flex-shrink: 0;
    }
    .main-icon { font-size: 24px; color: var(--burg-500); }
    .success-icon { font-size: 24px; color: #10b981; }

    .info-container { flex: 1; min-width: 0; }
    
    .prompt-text { display: flex; flex-direction: column; }
    .primary-text { font-size: 0.85rem; font-weight: 700; color: var(--text-1); }
    .secondary-text { font-size: 0.75rem; color: var(--text-3); }
    .browse { color: var(--burg-600); text-decoration: underline; font-weight: 500; }

    .file-info-preview { display: flex; flex-direction: column; }
    .file-name { font-size: 0.8rem; font-weight: 600; color: var(--text-1); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .file-size { font-size: 0.7rem; color: var(--text-3); }

    .progress-container {
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: var(--border);
    }
    .progress-bar-fill {
      height: 100%;
      background: linear-gradient(90deg, var(--burg-400), var(--burg-700));
      width: 30%;
      animation: progress-slide 1s infinite alternate eas-in-out;
    }
    @keyframes progress-slide {
      from { left: 0%; width: 20%; }
      to { left: 80%; width: 20%; }
    }

    .upload-spinner {
      width: 20px;
      height: 20px;
      border: 2px solid var(--burg-100);
      border-top-color: var(--burg-600);
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .error-strip {
      margin-top: 8px;
      display: flex;
      align-items: center;
      gap: 6px;
      color: var(--ruby-700);
      font-size: 0.7rem;
      font-weight: 600;
      background: white;
      padding: 4px 8px;
      border-radius: 6px;
    }
    .error-strip span:first-child { font-size: 14px; }
  `]
})
export class FileUploadComponent {
    @Input() label = 'Document';
    @Input() accept = '*/*';
    @Input() documentType!: DocumentType;
    @Input() applicationId?: number;
    @Input() claimId?: number;

    @Output() uploaded = new EventEmitter<any>();

    private docService = inject(DocumentService);

    isUploading = signal(false);
    error = signal('');
    isDropping = false;
    selectedFile: File | null = null;

    onFileSelected(event: Event) {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0];
        if (file) {
            this.selectedFile = file;
            this.upload(file);
        }
    }

    onDragOver(event: DragEvent) {
        event.preventDefault();
        this.isDropping = true;
    }

    onDragLeave(event: DragEvent) {
        this.isDropping = false;
    }

    onDrop(event: DragEvent) {
        event.preventDefault();
        this.isDropping = false;
        const file = event.dataTransfer?.files[0];
        if (file) {
            this.selectedFile = file;
            this.upload(file);
        }
    }

    private upload(file: File) {
        this.isUploading.set(true);
        this.error.set('');

        this.docService.upload(file, this.documentType, this.applicationId, this.claimId).subscribe({
            next: (res: any) => {
                this.isUploading.set(false);
                this.uploaded.emit(res);
                this.selectedFile = null; // Reset for next potential upload (though usually modal closes)
            },
            error: (err: any) => {
                this.isUploading.set(false);
                this.error.set('Upload failed. Check file size/type.');
                this.selectedFile = null;
                console.error('File upload error:', err);
            }
        });
    }
}
