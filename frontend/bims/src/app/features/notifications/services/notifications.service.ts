import { Injectable, inject, signal } from '@angular/core';
import { ApiService } from '../../../core/services/api.service';
import { Notification } from '../../../core/models/models';

@Injectable({ providedIn: 'root' })
export class NotificationsService {
  private api = inject(ApiService);
  unreadCount = signal(0);

  getAll() { return this.api.get<Notification[]>(`/notifications/user`); }
  getUnread() { return this.api.get<Notification[]>(`/notifications/user/unread`); }
  loadCount() {
    this.api.get<{ unreadCount: number }>(`/notifications/user/count`).subscribe(r => this.unreadCount.set(r.unreadCount));
  }
  markRead(notifId: number) { return this.api.putText(`/notifications/${notifId}/read`); }
  markAllRead() { return this.api.putText(`/notifications/user/read-all`); }
}
