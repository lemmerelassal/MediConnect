import { Component, OnInit, inject } from '@angular/core';
import { RouterOutlet, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from './services/auth.service';
import { NotificationService } from './services/notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink],
  template: `
    @if (authService.isAuthenticated()) {
      <div class="app">
        <nav class="navbar">
          <div class="nav-container">
            <a routerLink="/" class="nav-logo">
              🏥 Pharma Shortage Marketplace
            </a>
            <ul class="nav-menu">
              <li class="nav-item">
                <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}" class="nav-link">
                  Dashboard
                </a>
              </li>
              <li class="nav-item">
                <a routerLink="/shortages" routerLinkActive="active" class="nav-link">Shortages</a>
              </li>
              <li class="nav-item">
                <a routerLink="/tenders" routerLinkActive="active" class="nav-link">Tenders</a>
              </li>
              <li class="nav-item">
                <a routerLink="/analytics" routerLinkActive="active" class="nav-link">Analytics</a>
              </li>
              @if (authService.hasAnyRole(['ADMIN', 'COUNTRY_ADMIN'])) {
                <li class="nav-item">
                  <a routerLink="/create-shortage" class="nav-link btn-primary">Report Shortage</a>
                </li>
              }
              <li class="nav-item">
                <a routerLink="/notifications" routerLinkActive="active" class="nav-link notification-link">
                  🔔 
                  @if (notificationService.unreadCount() > 0) {
                    <span class="notification-badge">{{ notificationService.unreadCount() }}</span>
                  }
                </a>
              </li>
              <li class="nav-item user-menu">
                <div class="user-info">
                  <span class="user-name">{{ authService.currentUser()?.firstName }} {{ authService.currentUser()?.lastName }}</span>
                  <span class="user-role">{{ authService.currentUser()?.role }}</span>
                </div>
                <button class="btn-logout" (click)="logout()">Logout</button>
              </li>
            </ul>
          </div>
        </nav>

        <div class="main-content">
          <router-outlet></router-outlet>
        </div>

        <footer class="footer">
          <p>© 2026 Pharmaceutical Shortage Marketplace - Connecting Countries to Save Lives</p>
        </footer>
      </div>
    } @else {
      <router-outlet></router-outlet>
    }
  `,
  styles: [`
    .app {
      min-height: 100vh;
      display: flex;
      flex-direction: column;
    }

    .navbar {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 1rem 0;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    }

    .nav-container {
      max-width: 1400px;
      margin: 0 auto;
      padding: 0 2rem;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .nav-logo {
      font-size: 1.5rem;
      font-weight: bold;
      color: white;
      text-decoration: none;
    }

    .nav-menu {
      display: flex;
      list-style: none;
      gap: 1.5rem;
      align-items: center;
      margin: 0;
      padding: 0;
    }

    .nav-link {
      color: white;
      text-decoration: none;
      font-weight: 500;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      transition: all 0.3s;
    }

    .nav-link:hover, .nav-link.active {
      background: rgba(255,255,255,0.1);
    }

    .nav-link.btn-primary {
      background: white;
      color: #667eea;
      font-weight: 600;
    }

    .notification-link {
      position: relative;
    }

    .notification-badge {
      position: absolute;
      top: -5px;
      right: -5px;
      background: #f56565;
      color: white;
      border-radius: 50%;
      padding: 2px 6px;
      font-size: 0.75rem;
      font-weight: bold;
    }

    .user-menu {
      display: flex;
      gap: 1rem;
      align-items: center;
      padding-left: 1rem;
      border-left: 1px solid rgba(255,255,255,0.3);
    }

    .user-info {
      display: flex;
      flex-direction: column;
      align-items: flex-end;
    }

    .user-name {
      font-weight: 600;
      font-size: 0.875rem;
    }

    .user-role {
      font-size: 0.75rem;
      opacity: 0.8;
    }

    .btn-logout {
      background: rgba(255,255,255,0.2);
      color: white;
      border: none;
      padding: 0.5rem 1rem;
      border-radius: 4px;
      cursor: pointer;
      font-weight: 500;
      transition: all 0.3s;
    }

    .btn-logout:hover {
      background: rgba(255,255,255,0.3);
    }

    .main-content {
      flex: 1;
      max-width: 1400px;
      margin: 0 auto;
      width: 100%;
    }

    .footer {
      background: #2d3748;
      color: white;
      text-align: center;
      padding: 2rem;
      margin-top: auto;
    }

    @media (max-width: 1024px) {
      .nav-menu {
        flex-wrap: wrap;
        gap: 0.5rem;
      }
    }
  `]
})
export class AppComponent implements OnInit {
  authService = inject(AuthService);
  notificationService = inject(NotificationService);

  ngOnInit(): void {
    // Request notification permission
    this.notificationService.requestNotificationPermission();
    
    // Start notification polling if authenticated
    if (this.authService.isAuthenticated()) {
      this.notificationService.fetchNotifications();
    }
  }

  logout(): void {
    this.authService.logout();
  }
}
