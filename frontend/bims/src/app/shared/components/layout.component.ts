import { Component, inject, signal, OnInit, HostListener } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive, Router } from '@angular/router';
import { CommonModule, DatePipe } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { NotificationsService } from '../../features/notifications/services/notifications.service';
import { Notification } from '../../core/models/models';
import { ChatbotComponent } from './chatbot.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CommonModule, DatePipe, ChatbotComponent],
  templateUrl: './layout.component.html'
})
export class LayoutComponent implements OnInit {
  auth = inject(AuthService);
  private notifSvc = inject(NotificationsService);
  private router = inject(Router);

  unreadCount = this.notifSvc.unreadCount;
  showNotifPopup = signal(false);
  recentNotifs = signal<Notification[]>([]);

  ngOnInit() {
    if (this.auth.user()?.id) {
      this.notifSvc.loadCount();
      this.loadRecent();
    }
  }

  loadRecent() {
    this.notifSvc.getAll().subscribe({
      next: (all) => this.recentNotifs.set(all.slice(0, 5)),
      error: () => this.recentNotifs.set([])
    });
  }

  togglePopup() {
    const next = !this.showNotifPopup();
    this.showNotifPopup.set(next);
    if (next) this.loadRecent();
  }

  closePopup() {
    this.showNotifPopup.set(false);
  }

  goToNotifications() {
    this.closePopup();
    this.router.navigate(['/notifications']);
  }

  markReadAndGo(n: Notification) {
    if (!n.isRead) {
      this.notifSvc.markRead(n.id).subscribe(() => {
        this.notifSvc.loadCount();
        this.loadRecent();
      });
    }
    this.closePopup();
    this.router.navigate(['/notifications']);
  }
}
