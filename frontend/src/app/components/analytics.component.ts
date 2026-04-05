import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { AnalyticsService, ShortageAnalytics, TenderAnalytics } from '../services/analytics.service';

@Component({
  selector: 'app-analytics',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  template: `
    <div class="analytics-dashboard">
      <h1 class="page-title">Advanced Analytics & Reporting</h1>

      <div class="filters-card card">
        <form [formGroup]="filtersForm">
          <div class="grid grid-3">
            <div class="form-group">
              <label class="form-label">Period</label>
              <select class="form-select" formControlName="period" (change)="loadAnalytics()">
                <option value="WEEK">Last Week</option>
                <option value="MONTH">Last Month</option>
                <option value="QUARTER">Last Quarter</option>
                <option value="YEAR">Last Year</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Start Date</label>
              <input type="date" class="form-input" formControlName="startDate" (change)="loadAnalytics()">
            </div>

            <div class="form-group">
              <label class="form-label">End Date</label>
              <input type="date" class="form-input" formControlName="endDate" (change)="loadAnalytics()">
            </div>
          </div>

          <div class="form-actions">
            <button class="btn btn-primary" (click)="exportReport('PDF')" type="button">
              📄 Export PDF Report
            </button>
            <button class="btn btn-secondary" (click)="exportReport('EXCEL')" type="button">
              📊 Export Excel
            </button>
            <button class="btn btn-secondary" (click)="exportReport('CSV')" type="button">
              📋 Export CSV
            </button>
          </div>
        </form>
      </div>

      @if (loading()) {
        <div class="loading">Loading analytics...</div>
      } @else {
        <!-- Shortage Analytics -->
        <div class="section">
          <h2 class="section-title">Shortage Analytics</h2>
          
          @if (shortageAnalytics()) {
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-value">{{ shortageAnalytics()!.total_shortages }}</div>
                <div class="stat-label">Total Shortages</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ shortageAnalytics()!.active_shortages }}</div>
                <div class="stat-label">Active</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ shortageAnalytics()!.fulfilled_shortages }}</div>
                <div class="stat-label">Fulfilled</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ shortageAnalytics()!.avg_fulfillment_time_hours.toFixed(1) }}h</div>
                <div class="stat-label">Avg Fulfillment Time</div>
              </div>
            </div>

            <div class="grid grid-2">
              <div class="card">
                <h3>By Urgency Level</h3>
                <div class="chart-placeholder">
                  @for (item of getUrgencyData(); track item.key) {
                    <div class="bar-chart-row">
                      <span class="bar-label badge badge-{{ item.key.toLowerCase() }}">{{ item.key }}</span>
                      <div class="bar-container">
                        <div class="bar" [style.width.%]="item.percentage">{{ item.value }}</div>
                      </div>
                    </div>
                  }
                </div>
              </div>

              <div class="card">
                <h3>Top Medications</h3>
                <div class="list-data">
                  @for (med of shortageAnalytics()!.top_medications.slice(0, 5); track med.medication_id) {
                    <div class="list-item">
                      <span class="list-label">{{ med.medication_name }}</span>
                      <span class="list-value">{{ med.shortage_count }} shortages</span>
                    </div>
                  }
                </div>
              </div>
            </div>
          }
        </div>

        <!-- Tender Analytics -->
        <div class="section">
          <h2 class="section-title">Tender Analytics</h2>
          
          @if (tenderAnalytics()) {
            <div class="stats-grid">
              <div class="stat-card">
                <div class="stat-value">{{ tenderAnalytics()!.total_tenders }}</div>
                <div class="stat-label">Total Tenders</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ tenderAnalytics()!.accepted_tenders }}</div>
                <div class="stat-label">Accepted</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">{{ tenderAnalytics()!.rejected_tenders }}</div>
                <div class="stat-label">Rejected</div>
              </div>
              <div class="stat-card">
                <div class="stat-value">${{ tenderAnalytics()!.avg_tender_value.toFixed(0) }}</div>
                <div class="stat-label">Avg Value</div>
              </div>
            </div>

            <div class="grid grid-2">
              <div class="card">
                <h3>By Status</h3>
                <div class="chart-placeholder">
                  @for (item of getStatusData(); track item.key) {
                    <div class="bar-chart-row">
                      <span class="bar-label badge badge-{{ item.key.toLowerCase() }}">{{ item.key }}</span>
                      <div class="bar-container">
                        <div class="bar" [style.width.%]="item.percentage">{{ item.value }}</div>
                      </div>
                    </div>
                  }
                </div>
              </div>

              <div class="card">
                <h3>Top Suppliers</h3>
                <div class="list-data">
                  @for (supplier of tenderAnalytics()!.top_suppliers.slice(0, 5); track supplier.country_id) {
                    <div class="list-item">
                      <span class="list-label">{{ supplier.country_name }}</span>
                      <span class="list-value">{{ supplier.acceptance_rate.toFixed(1) }}% accepted</span>
                    </div>
                  }
                </div>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .analytics-dashboard {
      padding: 2rem;
    }

    .page-title {
      font-size: 2rem;
      margin-bottom: 2rem;
    }

    .section {
      margin-bottom: 3rem;
    }

    .section-title {
      font-size: 1.5rem;
      margin-bottom: 1.5rem;
      color: #2d3748;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }

    .stat-card {
      background: white;
      padding: 1.5rem;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
      text-align: center;
    }

    .stat-value {
      font-size: 2.5rem;
      font-weight: bold;
      color: #667eea;
      margin-bottom: 0.5rem;
    }

    .stat-label {
      color: #718096;
      font-size: 0.875rem;
      text-transform: uppercase;
    }

    .form-actions {
      display: flex;
      gap: 1rem;
      margin-top: 1rem;
    }

    .bar-chart-row {
      display: flex;
      align-items: center;
      gap: 1rem;
      margin-bottom: 1rem;
    }

    .bar-label {
      min-width: 100px;
    }

    .bar-container {
      flex: 1;
      background: #e2e8f0;
      border-radius: 4px;
      height: 30px;
      position: relative;
    }

    .bar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      height: 100%;
      border-radius: 4px;
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding-right: 0.5rem;
      color: white;
      font-weight: 600;
      transition: width 0.3s;
    }

    .list-data {
      padding: 1rem 0;
    }

    .list-item {
      display: flex;
      justify-content: space-between;
      padding: 0.75rem 0;
      border-bottom: 1px solid #e2e8f0;
    }

    .list-item:last-child {
      border-bottom: none;
    }

    .list-label {
      font-weight: 500;
    }

    .list-value {
      color: #667eea;
      font-weight: 600;
    }

    .grid {
      display: grid;
      gap: 1.5rem;
    }

    .grid-2 {
      grid-template-columns: repeat(2, 1fr);
    }

    .grid-3 {
      grid-template-columns: repeat(3, 1fr);
    }

    @media (max-width: 768px) {
      .grid-2, .grid-3 {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class AnalyticsComponent implements OnInit {
  private analyticsService = inject(AnalyticsService);
  private fb = inject(FormBuilder);

  loading = signal(false);
  shortageAnalytics = signal<ShortageAnalytics | null>(null);
  tenderAnalytics = signal<TenderAnalytics | null>(null);

  filtersForm = this.fb.group({
    period: ['MONTH'],
    startDate: [''],
    endDate: ['']
  });

  ngOnInit(): void {
    this.setDefaultDates();
    this.loadAnalytics();
  }

  setDefaultDates(): void {
    const endDate = new Date();
    const startDate = new Date();
    startDate.setMonth(startDate.getMonth() - 1);

    this.filtersForm.patchValue({
      startDate: startDate.toISOString().split('T')[0],
      endDate: endDate.toISOString().split('T')[0]
    });
  }

  loadAnalytics(): void {
    this.loading.set(true);
    const params = this.filtersForm.value;

    this.analyticsService.getShortageAnalytics(params as any).subscribe({
      next: (data) => {
        this.shortageAnalytics.set(data);
      }
    });

    this.analyticsService.getTenderAnalytics(params as any).subscribe({
      next: (data) => {
        this.tenderAnalytics.set(data);
        this.loading.set(false);
      }
    });
  }

  getUrgencyData(): Array<{key: string, value: number, percentage: number}> {
    const data = this.shortageAnalytics();
    if (!data) return [];
    
    const total = Object.values(data.shortages_by_urgency).reduce((a, b) => a + b, 0);
    return Object.entries(data.shortages_by_urgency).map(([key, value]) => ({
      key,
      value,
      percentage: (value / total) * 100
    }));
  }

  getStatusData(): Array<{key: string, value: number, percentage: number}> {
    const data = this.tenderAnalytics();
    if (!data) return [];
    
    const total = Object.values(data.tenders_by_status).reduce((a, b) => a + b, 0);
    return Object.entries(data.tenders_by_status).map(([key, value]) => ({
      key,
      value,
      percentage: (value / total) * 100
    }));
  }

  exportReport(format: string): void {
    const formValue = this.filtersForm.value;
    
    this.analyticsService.exportReport({
      reportType: 'SHORTAGE_SUMMARY',
      format,
      period: formValue.period!,
      startDate: formValue.startDate!,
      endDate: formValue.endDate!
    }).subscribe({
      next: (blob) => {
        const filename = `analytics-report-${new Date().toISOString().split('T')[0]}.${format.toLowerCase()}`;
        this.analyticsService.downloadReport(blob, filename);
      }
    });
  }
}
