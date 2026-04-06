import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Doc, DocumentType } from '../models/models';
import { API_BASE } from './api.service';

@Injectable({ providedIn: 'root' })
export class DocumentService {
    private http = inject(HttpClient);
    private readonly baseUrl = `${API_BASE}/documents`;

    upload(file: File, type: DocumentType, appId?: number, claimId?: number) {
        const formData = new FormData();
        formData.append('file', file);
        formData.append('documentType', type);
        if (appId) formData.append('applicationId', appId.toString());
        if (claimId) formData.append('claimId', claimId.toString());

        return this.http.post<Doc>(`${this.baseUrl}/upload`, formData);
    }

    getByApplication(appId: number) {
        return this.http.get<Doc[]>(`${this.baseUrl}/application/${appId}`);
    }

    getByClaim(claimId: number) {
        return this.http.get<Doc[]>(`${this.baseUrl}/claim/${claimId}`);
    }

    download(docId: number) {
        return this.http.get(`${this.baseUrl}/view/${docId}`, {
            responseType: 'blob'
        });
    }
}
