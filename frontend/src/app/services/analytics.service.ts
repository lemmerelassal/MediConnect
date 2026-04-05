import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ShortageAnalytics {
  total_shortages: number;
  active_shortages: number;
  fulfilled_shortages: number;
  avg_fulfillment_time_hours: number;
  shortages_by_urgency: Record<string, number>;
  shortages_by_country: Record<string, number>;
  time_series: TimeSeriesPoint[];
  top_medications: TopMedication[];
}

export interface TenderAnalytics {
  total_tenders: number;
  accepted_tenders: number;
  rejected_tenders: number;
  avg_response_time_hours: number;
  avg_tender_value: number;
  tenders_by_status: Record<string, number>;
  time_series: TenderTimeSeriesPoint[];
  top_suppliers: TopSupplier[];
}

export interface CountryAnalytics {
  country_id: number;
  country_name: string;
  total_shortages_reported: number;
  total_tenders_submitted: number;
  total_tenders_received: number;
  avg_response_time_hours: number;
  fulfillment_rate: number;
  total_value_traded: number;
}

export interface TimeSeriesPoint {
  date: string;
  count: number;
  urgency: string;
}

export interface TenderTimeSeriesPoint {
  date: string;
  count: number;
  avg_value: number;
}

export interface TopMedication {
  medication_id: number;
  medication_name: string;
  shortage_count: number;
  total_quantity_needed: number;
}

export interface TopSupplier {
  country_id: number;
  country_name: string;
  tender_count: number;
  total_value: number;
  acceptance_rate: number;
}

@Injectable({
  providedIn: 'root'
})
export class AnalyticsService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api';

  getShortageAnalytics(params: {
    period?: string;
    startDate?: string;
    endDate?: string;
  }): Observable<ShortageAnalytics> {
    return this.http.get<ShortageAnalytics>(`${this.apiUrl}/analytics/shortages`, { params: params as any });
  }

  getTenderAnalytics(params: {
    period?: string;
    startDate?: string;
    endDate?: string;
  }): Observable<TenderAnalytics> {
    return this.http.get<TenderAnalytics>(`${this.apiUrl}/analytics/tenders`, { params: params as any });
  }

  getCountryAnalytics(countryId: number, params: {
    period?: string;
    startDate?: string;
    endDate?: string;
  }): Observable<CountryAnalytics> {
    return this.http.get<CountryAnalytics>(`${this.apiUrl}/analytics/country/${countryId}`, { params: params as any });
  }

  exportReport(data: {
    reportType: string;
    format: string;
    period: string;
    startDate?: string;
    endDate?: string;
    countryIds?: number[];
  }): Observable<Blob> {
    return this.http.post(`${this.apiUrl}/analytics/export`, data, {
      responseType: 'blob'
    });
  }

  downloadReport(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = filename;
    link.click();
    window.URL.revokeObjectURL(url);
  }

  getReportTypes(): string[] {
    return ['SHORTAGE_SUMMARY', 'TENDER_SUMMARY', 'COUNTRY_PERFORMANCE'];
  }

  getReportFormats(): string[] {
    return ['PDF', 'EXCEL', 'CSV'];
  }

  getPeriods(): string[] {
    return ['WEEK', 'MONTH', 'QUARTER', 'YEAR'];
  }
}
