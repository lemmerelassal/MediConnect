import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  template: `
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h1>🏥 Pharma Shortage Marketplace</h1>
          <p>Sign in to your account</p>
        </div>

        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
          <div class="form-group">
            <label class="form-label">Email</label>
            <input 
              type="email" 
              class="form-input" 
              formControlName="email"
              placeholder="your.email@example.com"
            >
            @if (loginForm.get('email')?.invalid && loginForm.get('email')?.touched) {
              <span class="error-text">Valid email is required</span>
            }
          </div>

          <div class="form-group">
            <label class="form-label">Password</label>
            <input 
              type="password" 
              class="form-input" 
              formControlName="password"
              placeholder="••••••••"
            >
            @if (loginForm.get('password')?.invalid && loginForm.get('password')?.touched) {
              <span class="error-text">Password is required</span>
            }
          </div>

          @if (errorMessage()) {
            <div class="error">{{ errorMessage() }}</div>
          }

          <button 
            type="submit" 
            class="btn btn-primary btn-block"
            [disabled]="loginForm.invalid || loading()"
          >
            {{ loading() ? 'Signing in...' : 'Sign In' }}
          </button>
        </form>

        <div class="login-footer">
          <p>Don't have an account? <a routerLink="/register">Register here</a></p>
        </div>

        <div class="demo-credentials">
          <p><strong>Demo Credentials:</strong></p>
          <p>Email: admin@pharma.org</p>
          <p>Password: Admin123!</p>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 2rem;
    }

    .login-card {
      background: white;
      border-radius: 12px;
      padding: 3rem;
      max-width: 450px;
      width: 100%;
      box-shadow: 0 10px 40px rgba(0,0,0,0.2);
    }

    .login-header {
      text-align: center;
      margin-bottom: 2rem;
    }

    .login-header h1 {
      font-size: 1.75rem;
      margin-bottom: 0.5rem;
      color: #2d3748;
    }

    .login-header p {
      color: #718096;
    }

    .btn-block {
      width: 100%;
      margin-top: 1rem;
    }

    .login-footer {
      text-align: center;
      margin-top: 2rem;
      padding-top: 2rem;
      border-top: 1px solid #e2e8f0;
    }

    .login-footer a {
      color: #667eea;
      text-decoration: none;
      font-weight: 600;
    }

    .login-footer a:hover {
      text-decoration: underline;
    }

    .demo-credentials {
      margin-top: 2rem;
      padding: 1rem;
      background: #f7fafc;
      border-radius: 6px;
      font-size: 0.875rem;
      text-align: center;
    }

    .demo-credentials p {
      margin: 0.25rem 0;
      color: #4a5568;
    }

    .error-text {
      color: #c53030;
      font-size: 0.875rem;
      display: block;
      margin-top: 0.25rem;
    }
  `]
})
export class LoginComponent {
  private authService = inject(AuthService);
  private fb = inject(FormBuilder);
  private router = inject(Router);

  loading = signal(false);
  errorMessage = signal<string | null>(null);

  loginForm = this.fb.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  onSubmit(): void {
    if (this.loginForm.valid) {
      this.loading.set(true);
      this.errorMessage.set(null);

      const { email, password } = this.loginForm.value;

      this.authService.login(email!, password!).subscribe({
        next: () => {
          this.router.navigate(['/']);
        },
        error: (err) => {
          this.errorMessage.set(err.error?.error || 'Invalid email or password');
          this.loading.set(false);
        }
      });
    }
  }
}
