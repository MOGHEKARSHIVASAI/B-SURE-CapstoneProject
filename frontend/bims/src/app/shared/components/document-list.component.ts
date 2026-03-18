import { Component, EventEmitter, Input, OnInit, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DocumentService } from '../../core/services/document.service';
import { Doc, DocumentType } from '../../core/models/models';
import { FileUploadComponent } from './file-upload.component';

@Component({
    selector: 'app-document-list',
    standalone: true,
    imports: [CommonModule, FileUploadComponent],
    template: `
    <div class="doc-list-container">
      @if (loading()) {
        <div class="flex justify-center p-8">
          <div class="spinner"></div>
        </div>
      } @else {
        <div class="card p-0 overflow-hidden shadow-none border border-stone-200">
          @if (!docs().length) {
            <div class="p-8 text-center text-stone-400 text-sm">
              <span class="material-symbols-outlined block text-3xl mb-2">inventory_2</span>
              No documents uploaded yet.
            </div>
          } @else {
            <div class="table-wrap">
              <table class="w-full text-sm">
                <thead class="bg-stone-50">
                  <tr>
                    <th class="p-3 text-left">Type</th>
                    <th class="p-3 text-left">File Name</th>
                    <th class="p-3 text-right">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  @for (doc of docs(); track doc.id) {
                    <tr class="border-t border-stone-100 hover:bg-stone-50 transition-colors">
                      <td class="p-3"><span class="badge badge-gray text-[10px]">{{ doc.documentType }}</span></td>
                      <td class="p-3">
                        <div class="font-medium text-stone-700 truncate max-w-[200px]" [title]="doc.fileName">
                          {{ doc.fileName }}
                        </div>
                        <div class="text-[10px] text-stone-400">{{ doc.uploadedAt | date:'medium' }}</div>
                      </td>
                      <td class="p-3 text-right">
                        <button class="btn btn-ghost btn-sm" (click)="viewDoc(doc)" [disabled]="viewingId() === doc.id">
                          @if (viewingId() === doc.id) {
                            <div class="spinner-xs mr-1"></div>
                          } @else {
                            <span class="material-symbols-outlined text-xs">visibility</span>
                          }
                          View
                        </button>
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>
          }
        </div>

        <!-- Checklist of Requirements -->
        @if (canUpload && requiredTypes.length > 0) {
          <div class="requirements-checklist mt-4 p-4 rounded-xl border border-stone-200 bg-stone-50/50">
            <h4 class="text-xs font-bold uppercase tracking-wider mb-3 text-stone-500">Document Checklist</h4>
            <div class="flex flex-wrap gap-3">
              @for (type of requiredTypes; track type.type) {
                <div class="flex items-center gap-2 px-3 py-1.5 rounded-lg border shadow-sm" 
                     [style.borderColor]="hasUploaded(type.type) ? 'var(--green-400)' : 'var(--burg-200)'"
                     [style.background]="hasUploaded(type.type) ? 'var(--green-50)' : 'white'">
                  <span class="material-symbols-outlined text-sm" 
                        [style.color]="hasUploaded(type.type) ? 'var(--green-600)' : 'var(--burg-400)'">
                    {{ hasUploaded(type.type) ? 'check_circle' : 'pending' }}
                  </span>
                  <span class="text-xs font-medium" [style.color]="hasUploaded(type.type) ? 'var(--green-700)' : 'var(--text-1)'">
                    {{ type.label }}
                  </span>
                </div>
              }
            </div>
          </div>
        }

        @if (canUpload) {
          <div class="mt-6 pt-6 border-t border-dashed border-stone-200">
            <h3 class="text-sm font-bold text-stone-800 mb-2 flex items-center gap-2">
              <span class="material-symbols-outlined text-stone-400">upload_file</span>
              Upload New Document
            </h3>
            <p class="text-xs text-stone-400 mb-4">Select the correct document type and upload your file.</p>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              @for (type of uploadTypes; track type.type) {
                <app-file-upload 
                  [label]="type.label + (type.required ? ' *' : '')" 
                  [documentType]="type.type" 
                  [applicationId]="applicationId"
                  [claimId]="claimId"
                  (uploaded)="onDocUploaded($event)">
                </app-file-upload>
              }
            </div>
          </div>
        }
      }
    </div>
  `,
    styles: [`
    .doc-list-container { width: 100%; animation: fadeIn 0.4s ease-out; }
    @keyframes fadeIn { from { opacity: 0; transform: translateY(10px); } to { opacity: 1; transform: translateY(0); } }
    
    .table-wrap { border-radius: 12px; overflow: hidden; }
    .badge-gray { 
      background: var(--surface-2); 
      color: var(--text-2); 
      border: 1px solid var(--border);
      font-size: 10px;
      font-weight: 600;
      padding: 4px 10px;
      border-radius: 6px;
    }
    
    .spinner-xs { 
      width: 14px; 
      height: 14px; 
      border: 2.5px solid rgba(109, 33, 40, 0.1); 
      border-top-color: var(--burg-600); 
      border-radius: 50%; 
      animation: spin 0.8s cubic-bezier(0.5, 0.1, 0.4, 0.9) infinite; 
    }
    @keyframes spin { to { transform: rotate(360deg); } }

    .requirements-checklist {
      box-shadow: inset 0 2px 4px rgba(0,0,0,0.02);
    }
  `]
})
export class DocumentListComponent implements OnInit {
    @Input() applicationId?: number;
    @Input() claimId?: number;
    @Input() canUpload = false;
    @Input() uploadTypes: { label: string, type: DocumentType, required?: boolean }[] = [];

    @Output() countChanged = new EventEmitter<number>();

    private docService = inject(DocumentService);

    docs = signal<Doc[]>([]);
    loading = signal(true);
    viewingId = signal<number | null>(null);

    ngOnInit() {
        this.refresh();
    }

    refresh() {
        this.loading.set(true);
        const obs$ = this.applicationId
            ? this.docService.getByApplication(this.applicationId)
            : this.docService.getByClaim(this.claimId!);

        obs$.subscribe({
            next: (d) => {
                this.docs.set(d);
                this.loading.set(false);
                this.countChanged.emit(d.length);
            },
            error: () => this.loading.set(false)
        });
    }

    onDocUploaded(newDoc: Doc) {
        this.docs.update(d => [...d, newDoc]);
        this.countChanged.emit(this.docs().length);
    }

    get requiredTypes() {
        return this.uploadTypes.filter(t => t.required);
    }

    hasUploaded(type: DocumentType): boolean {
        return this.docs().some(d => d.documentType === type);
    }

    viewDoc(doc: Doc) {
        this.viewingId.set(doc.id);
        this.docService.download(doc.id).subscribe({
            next: (blob) => {
                const url = window.URL.createObjectURL(blob);
                window.open(url, '_blank');
                // Optional: Clean up URL after some time
                setTimeout(() => window.URL.revokeObjectURL(url), 1000);
                this.viewingId.set(null);
            },
            error: () => this.viewingId.set(null)
        });
    }
}
