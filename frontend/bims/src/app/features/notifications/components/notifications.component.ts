import { Component, inject, signal, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NotificationsService } from '../services/notifications.service';
import { AuthService } from '../../../core/services/auth.service';
import { Notification } from '../../../core/models/models';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './notifications.component.html'
})
export class NotificationsComponent implements OnInit {
  private svc = inject(NotificationsService);
  private auth = inject(AuthService);

  notifications = signal<Notification[]>([]);
  loading = signal(true);

  get unread() { return signal(this.notifications().filter(n => !n.isRead)); }

  ngOnInit() {
    this.svc.getAll().subscribe({
      next: (d) => { this.notifications.set(d); this.loading.set(false); this.svc.loadCount(); },
      error: () => this.loading.set(false)
    });
  }

  markRead(n: Notification) {
    if (n.isRead) return;
    this.svc.markRead(n.id).subscribe(() => {
      this.notifications.update(list => list.map(x => x.id === n.id ? { ...x, isRead: true } : x));
      this.svc.unreadCount.update(c => Math.max(0, c - 1));
    });
  }

  markAll() {
    this.svc.markAllRead().subscribe(() => {
      this.notifications.update(list => list.map(n => ({ ...n, isRead: true })));
      this.svc.unreadCount.set(0);
    });
  }
}
