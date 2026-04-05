import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpEvent } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Document {
  id: number;
  uuid: string;
  fileName: string;
  fileSize: number;
  mimeType: string;
  documentType: string;
  description?: string;
  verified: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class DocumentService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api';

  uploadDocument(
    file: File,
    tenderId: number,
    documentType: string,
    description?: string
  ): Observable<any> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('tenderId', tenderId.toString());
    formData.append('documentType', documentType);
    if (description) {
      formData.append('description', description);
    }

    return this.http.post(`${this.apiUrl}/documents/upload`, formData, {
      reportProgress: true,
      observe: 'events'
    });
  }

  getDocumentsByTender(tenderId: number): Observable<Document[]> {
    return this.http.get<Document[]>(`${this.apiUrl}/documents/tender/${tenderId}`);
  }

  downloadDocument(uuid: string, fileName: string): void {
    this.http.get(`${this.apiUrl}/documents/${uuid}`, {
      responseType: 'blob'
    }).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = fileName;
        link.click();
        window.URL.revokeObjectURL(url);
      }
    });
  }

  deleteDocument(uuid: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/documents/${uuid}`);
  }

  getDocumentTypes(): string[] {
    return ['CERTIFICATE', 'LICENSE', 'APPROVAL', 'QUALITY_REPORT', 'OTHER'];
  }

  formatFileSize(bytes: number): string {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i];
  }
}
