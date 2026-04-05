import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NotificationService, Notification } from '../services/notification.service';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="notifications-panel">
      <div class="notifications-header">
        <h2>Notifications</h2>
        <div class="header-actions">
          <span class="badge badge-primary">{{ notificationService.unreadCount() }} unread</span>
          @if (notificationService.unreadCount() > 0) {
            <button class="btn btn-sm btn-secondary" (click)="markAllAsRead()">
              Mark all as read
            </button>
          }
        </div>
      </div>

      @if (notificationService.notifications().length === 0) {
        <div class="empty-state">
          <p>No notifications yet</p>
        </div>
      } @else {
        <div class="notifications-list">
          @for (notification of notificationService.notifications(); track notification.id) {
            <div 
              class="notification-item" 
              [class.unread]="!notification.isRead"
              (click)="handleNotificationClick(notification)"
            >
              <div class="notification-icon" [class]="'notification-icon-' + notification.type.toLowerCase()">
                {{ getNotificationIcon(notification.type) }}
              </div>
              
              <div class="notification-content">
                <div class="notification-title">{{ notification.title }}</div>
                <div class="notification-message">{{ notification.message }}</div>
                <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
              </div>

              <div class="notification-actions">
                @if (!notification.isRead) {
                  <button 
                    class="btn-icon" 
                    (click)="markAsRead(notification.id, $event)"
                    title="Mark as read"
                  >
                    ✓
                  </button>
                }
                <button 
                  class="btn-icon" 
                  (click)="deleteNotification(notification.id, $event)"
                  title="Delete"
                >
                  ×
                </button>
              </div>
            </div>
          }
        </div>
      }
    </div>
  `,
  styles: [`
    .notifications-panel {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 8px rgba(0,0,0,0.1);
    }

    .notifications-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-bottom: 1px solid #e2e8f0;
    }

    .notifications-header h2 {
      margin: 0;
      font-size: 1.5rem;
    }

    .header-actions {
      display: flex;
      gap: 1rem;
      align-items: center;
    }

    .notifications-list {
      max-height: 600px;
      overflow-y: auto;
    }

    .notification-item {
      display: flex;
      gap: 1rem;
      padding: 1rem 1.5rem;
      border-bottom: 1px solid #e2e8f0;
      cursor: pointer;
      transition: background 0.2s;
    }

    .notification-item:hover {
      background: #f7fafc;
    }

    .notification-item.unread {
      background: #edf2f7;
    }

    .notification-icon {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.25rem;
      flex-shrink: 0;
    }

    .notification-icon-new_shortage {
      background: #fee;
      color: #c53030;
    }

    .notification-icon-new_tender {
      background: #e6f7ff;
      color: #0369a1;
    }

    .notification-icon-tender_accepted {
      background: #d1fae5;
      color: #065f46;
    }

    .notification-icon-tender_rejected {
      background: #fee;
      color: #c53030;
    }

    .notification-content {
      flex: 1;
    }

    .notification-title {
      font-weight: 600;
      margin-bottom: 0.25rem;
      color: #2d3748;
    }

    .notification-message {
      color: #4a5568;
      font-size: 0.875rem;
      margin-bottom: 0.25rem;
    }

    .notification-time {
      color: #718096;
      font-size: 0.75rem;
    }

    .notification-actions {
      display: flex;
      gap: 0.5rem;
      align-items: center;
    }

    .btn-icon {
      background: none;
      border: none;
      cursor: pointer;
      font-size: 1.5rem;
      padding: 0.25rem 0.5rem;
      color: #718096;
      transition: color 0.2s;
    }

    .btn-icon:hover {
      color: #2d3748;
    }

    .btn-sm {
      padding: 0.5rem 1rem;
      font-size: 0.875rem;
    }

    .empty-state {
      padding: 3rem;
      text-align: center;
      color: #718096;
    }
  `]
})
export class NotificationsComponent implements OnInit {
  notificationService = inject(NotificationService);

  ngOnInit(): void {
    this.notificationService.fetchNotifications();
    
    // Subscribe to real-time notifications
    this.notificationService.notifications$.subscribe(notification => {
      this.notificationService.showBrowserNotification(notification);
    });
  }

  getNotificationIcon(type: string): string {
    const icons: Record<string, string> = {
      'NEW_SHORTAGE': '🏥',
      'NEW_TENDER': '💼',
      'TENDER_ACCEPTED': '✅',
      'TENDER_REJECTED': '❌',
      'DOCUMENT_UPLOADED': '📄',
      'DEADLINE_APPROACHING': '⏰'
    };
    return icons[type] || '📌';
  }

  formatTime(dateString: string): string {
    const date = new Date(dateString);
    const now = new Date();
    const diff = now.getTime() - date.getTime();
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Just now';
    if (minutes < 60) return `${minutes}m ago`;
    if (hours < 24) return `${hours}h ago`;
    if (days < 7) return `${days}d ago`;
    return date.toLocaleDateString();
  }

  handleNotificationClick(notification: Notification): void {
    if (!notification.isRead) {
      this.notificationService.markAsRead(notification.id);
    }
    
    // Navigate to related entity if applicable
    // This would be implemented based on relatedEntityType and relatedEntityId
  }

  markAsRead(id: number, event: Event): void {
    event.stopPropagation();
    this.notificationService.markAsRead(id);
  }

  markAllAsRead(): void {
    this.notificationService.markAllAsRead();
  }

  deleteNotification(id: number, event: Event): void {
    event.stopPropagation();
    this.notificationService.deleteNotification(id);
  }
}
