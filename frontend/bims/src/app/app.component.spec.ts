import { TestBed } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { RouterTestingModule } from '@angular/router/testing';
import { AuthService } from './core/services/auth.service';
import { NotificationsService } from './features/notifications/services/notifications.service';
import { signal } from '@angular/core';

describe('AppComponent', () => {
  let authServiceSpy: any;
  let notifServiceSpy: any;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj('AuthService', ['user']);
    authServiceSpy.user.and.returnValue({ id: 1 });
    notifServiceSpy = jasmine.createSpyObj('NotificationsService', ['loadCount', 'getAll']);
    notifServiceSpy.unreadCount = signal(0);

    await TestBed.configureTestingModule({
      imports: [AppComponent, RouterTestingModule],
      providers: [
        { provide: AuthService, useValue: authServiceSpy },
        { provide: NotificationsService, useValue: notifServiceSpy }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });
});
