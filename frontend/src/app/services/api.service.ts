import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Country {
  id: number;
  name: string;
  country_code: string;
  contact_email: string;
  contact_phone?: string;
  timezone: string;
}

export interface Medication {
  id: number;
  generic_name: string;
  brand_name?: string;
  dosage_form: string;
  strength: string;
  description?: string;
  atc_code?: string;
  therapeutic_category?: string;
}

export interface Shortage {
  id: number;
  country?: Country;
  medication?: Medication;
  quantity_needed: number;
  unit: string;
  urgency_level: string;
  reason?: string;
  status: string;
  deadline?: string;
  estimated_value?: number;
  currency: string;
  created_at: string;
  tender_count?: number;
}

export interface Tender {
  id: number;
  shortage_id: number;
  shortage?: Shortage;
  supplier_country?: Country;
  quantity_offered: number;
  unit: string;
  price_per_unit: number;
  currency: string;
  delivery_time_days: number;
  manufacturer_name?: string;
  batch_number?: string;
  expiry_date?: string;
  regulatory_approval_info?: string;
  status: string;
  notes?: string;
  created_at: string;
  reviewed_at?: string;
}

export interface Statistics {
  total_shortages: number;
  active_shortages: number;
  total_tenders: number;
  pending_tenders: number;
  shortages_by_urgency: Record<string, number>;
  shortages_by_country: Record<string, number>;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api';

  // Countries
  getCountries(): Observable<Country[]> {
    return this.http.get<Country[]>(`${this.apiUrl}/countries`);
  }

  getCountry(id: number): Observable<Country> {
    return this.http.get<Country>(`${this.apiUrl}/countries/${id}`);
  }

  createCountry(data: Partial<Country>): Observable<Country> {
    return this.http.post<Country>(`${this.apiUrl}/countries`, data);
  }

  // Medications
  getMedications(): Observable<Medication[]> {
    return this.http.get<Medication[]>(`${this.apiUrl}/medications`);
  }

  getMedication(id: number): Observable<Medication> {
    return this.http.get<Medication>(`${this.apiUrl}/medications/${id}`);
  }

  createMedication(data: Partial<Medication>): Observable<Medication> {
    return this.http.post<Medication>(`${this.apiUrl}/medications`, data);
  }

  // Shortages
  getShortages(params?: any): Observable<Shortage[]> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key]) {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.http.get<Shortage[]>(`${this.apiUrl}/shortages`, { params: httpParams });
  }

  getShortage(id: number): Observable<Shortage> {
    return this.http.get<Shortage>(`${this.apiUrl}/shortages/${id}`);
  }

  createShortage(data: any): Observable<Shortage> {
    return this.http.post<Shortage>(`${this.apiUrl}/shortages`, data);
  }

  updateShortage(id: number, data: Partial<Shortage>): Observable<Shortage> {
    return this.http.put<Shortage>(`${this.apiUrl}/shortages/${id}`, data);
  }

  deleteShortage(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/shortages/${id}`);
  }

  // Tenders
  getTenders(params?: any): Observable<Tender[]> {
    let httpParams = new HttpParams();
    if (params) {
      Object.keys(params).forEach(key => {
        if (params[key]) {
          httpParams = httpParams.set(key, params[key]);
        }
      });
    }
    return this.http.get<Tender[]>(`${this.apiUrl}/tenders`, { params: httpParams });
  }

  getTender(id: number): Observable<Tender> {
    return this.http.get<Tender>(`${this.apiUrl}/tenders/${id}`);
  }

  createTender(data: any): Observable<Tender> {
    return this.http.post<Tender>(`${this.apiUrl}/tenders`, data);
  }

  acceptTender(id: number): Observable<Tender> {
    return this.http.put<Tender>(`${this.apiUrl}/tenders/${id}/accept`, {});
  }

  rejectTender(id: number): Observable<Tender> {
    return this.http.put<Tender>(`${this.apiUrl}/tenders/${id}/reject`, {});
  }

  // Statistics
  getStatistics(): Observable<Statistics> {
    return this.http.get<Statistics>(`${this.apiUrl}/statistics`);
  }
}
