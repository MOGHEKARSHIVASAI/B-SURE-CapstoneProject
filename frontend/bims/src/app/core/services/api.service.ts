import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export const API_BASE = 'http://localhost:8050/api/v1';

@Injectable({ providedIn: 'root' })
export class ApiService {
  readonly base = API_BASE;
  private http = inject(HttpClient);
  get<T>(path: string) { return this.http.get<T>(`${API_BASE}${path}`); }
  post<T>(path: string, body: any) { return this.http.post<T>(`${API_BASE}${path}`, body); }
  put<T>(path: string, body: any = {}) { return this.http.put<T>(`${API_BASE}${path}`, body); }
  delete<T>(path: string) { return this.http.delete<T>(`${API_BASE}${path}`); }
  // Text-response variants for endpoints that return plain strings
  postText(path: string, body: any) { return this.http.post(`${API_BASE}${path}`, body, { responseType: 'text' }); }
  putText(path: string, body: any = {}) { return this.http.put(`${API_BASE}${path}`, body, { responseType: 'text' }); }
  deleteText(path: string) { return this.http.delete(`${API_BASE}${path}`, { responseType: 'text' }); }
}
