import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Subject, interval } from 'rxjs';

export interface Notification {
  id: number;
  type: string;
  title: string;
  message: string;
  relatedEntityType?: string;
  relatedEntityId?: number;
  isRead: boolean;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api';
  
  notifications = signal<Notification[]>([]);
  unreadCount = signal<number>(0);
  
  // Observable for real-time notifications
  private notificationStream = new Subject<Notification>();
  notifications$ = this.notificationStream.asObservable();

  constructor() {
    this.startPolling();
  }

  // In production, this would use gRPC-Web streaming
  // For now, we'll poll the REST API
  private startPolling(): void {
    interval(10000).subscribe(() => {
      this.fetchNotifications();
    });
  }

  fetchNotifications(): void {
    this.http.get<Notification[]>(`${this.apiUrl}/notifications`).subscribe({
      next: (notifications) => {
        this.notifications.set(notifications);
        this.unreadCount.set(notifications.filter(n => !n.isRead).length);
        
        // Emit new notifications
        const currentIds = this.notifications().map(n => n.id);
        notifications.forEach(n => {
          if (!currentIds.includes(n.id)) {
            this.notificationStream.next(n);
          }
        });
      }
    });
  }

  markAsRead(id: number): void {
    this.http.put(`${this.apiUrl}/notifications/${id}/read`, {}).subscribe({
      next: () => {
        const current = this.notifications();
        const updated = current.map(n => 
          n.id === id ? { ...n, isRead: true } : n
        );
        this.notifications.set(updated);
        this.unreadCount.set(updated.filter(n => !n.isRead).length);
      }
    });
  }

  markAllAsRead(): void {
    this.http.put(`${this.apiUrl}/notifications/mark-all-read`, {}).subscribe({
      next: () => {
        const updated = this.notifications().map(n => ({ ...n, isRead: true }));
        this.notifications.set(updated);
        this.unreadCount.set(0);
      }
    });
  }

  deleteNotification(id: number): void {
    this.http.delete(`${this.apiUrl}/notifications/${id}`).subscribe({
      next: () => {
        const updated = this.notifications().filter(n => n.id !== id);
        this.notifications.set(updated);
        this.unreadCount.set(updated.filter(n => !n.isRead).length);
      }
    });
  }

  // Helper to show browser notification (if permitted)
  showBrowserNotification(notification: Notification): void {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(notification.title, {
        body: notification.message,
        icon: '/assets/icon.png'
      });
    }
  }

  // Request browser notification permission
  requestNotificationPermission(): void {
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }
  }
}
