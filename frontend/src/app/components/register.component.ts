import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { ApiService, Country } from '../services/api.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <h1>Create Account</h1>
          <p>Join the Pharmaceutical Shortage Marketplace</p>
        </div>

        <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
          <div class="grid grid-2">
            <div class="form-group">
              <label class="form-label">First Name *</label>
              <input type="text" class="form-input" formControlName="firstName">
            </div>

            <div class="form-group">
              <label class="form-label">Last Name *</label>
              <input type="text" class="form-input" formControlName="lastName">
            </div>
          </div>

          <div class="form-group">
            <label class="form-label">Email *</label>
            <input type="email" class="form-input" formControlName="email">
          </div>

          <div class="form-group">
            <label class="form-label">Password *</label>
            <input type="password" class="form-input" formControlName="password">
            <small class="form-hint">Minimum 8 characters</small>
          </div>

          <div class="form-group">
            <label class="form-label">Role *</label>
            <select class="form-select" formControlName="role">
              <option value="VIEWER">Viewer</option>
              <option value="SUPPLIER">Supplier</option>
              <option value="COUNTRY_ADMIN">Country Admin</option>
            </select>
          </div>

          <div class="form-group">
            <label class="form-label">Country</label>
            <select class="form-select" formControlName="countryId">
              <option value="">Select Country</option>
              @for (country of countries(); track country.id) {
                <option [value]="country.id">{{ country.name }}</option>
              }
            </select>
          </div>

          @if (errorMessage()) {
            <div class="error">{{ errorMessage() }}</div>
          }

          <button 
            type="submit" 
            class="btn btn-primary btn-block"
            [disabled]="registerForm.invalid || loading()"
          >
            {{ loading() ? 'Creating Account...' : 'Create Account' }}
          </button>
        </form>

        <div class="register-footer">
          <p>Already have an account? <a routerLink="/login">Sign in</a></p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .register-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 2rem;
    }

    .register-card {
      background: white;
      border-radius: 12px;
      padding: 3rem;
      max-width: 600px;
      width: 100%;
      box-shadow: 0 10px 40px rgba(0,0,0,0.2);
    }

    .register-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .register-header h1 {
      font-size: 1.75rem;
      margin-bottom: 0.5rem;
      color: #2d3748;
    }

    .register-header p {
      color: #718096;
    }

    .btn-block {
      width: 100%;
      margin-top: 1rem;
    }

    .register-footer {
      text-align: center;
      margin-top: 2rem;
      padding-top: 2rem;
      border-top: 1px solid #e2e8f0;
    }

    .register-footer a {
      color: #667eea;
      text-decoration: none;
      font-weight: 600;
    }

    .register-footer a:hover {
      text-decoration: underline;
    }

    .form-hint {
      color: #718096;
      font-size: 0.875rem;
      display: block;
      margin-top: 0.25rem;
    }

    .grid {
      display: grid;
      gap: 1rem;
    }

    .grid-2 {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (max-width: 768px) {
      .grid-2 {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class RegisterComponent implements OnInit {
  private authService = inject(AuthService);
  private apiService = inject(ApiService);
  private fb = inject(FormBuilder);
  private router = inject(Router);

  loading = signal(false);
  errorMessage = signal<string | null>(null);
  countries = signal<Country[]>([]);

  registerForm = this.fb.group({
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    role: ['VIEWER', Validators.required],
    countryId: ['']
  });

  ngOnInit(): void {
    this.loadCountries();
  }

  loadCountries(): void {
    this.apiService.getCountries().subscribe({
      next: (data) => this.countries.set(data)
    });
  }

  onSubmit(): void {
    if (this.registerForm.valid) {
      this.loading.set(true);
      this.errorMessage.set(null);

      const formValue = this.registerForm.value;
      const data = {
        ...formValue,
        countryId: formValue.countryId ? Number(formValue.countryId) : undefined
      };

      this.authService.register(data as any).subscribe({
        next: () => {
          this.router.navigate(['/']);
        },
        error: (err) => {
          this.errorMessage.set(err.error?.error || 'Registration failed');
          this.loading.set(false);
        }
      });
    }
  }
}
