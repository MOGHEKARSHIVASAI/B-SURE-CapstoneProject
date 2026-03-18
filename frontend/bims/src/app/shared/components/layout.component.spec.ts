import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LayoutComponent } from './layout.component';
import { AuthService } from '../../core/services/auth.service';
import { NotificationsService } from '../../features/notifications/services/notifications.service';
import { RouterTestingModule } from '@angular/router/testing';
import { of } from 'rxjs';
import { Router } from '@angular/router';

import { signal } from '@angular/core';

describe('LayoutComponent', () => {
    let component: LayoutComponent;
    let fixture: ComponentFixture<LayoutComponent>;
    let authServiceSpy: any;
    let notifServiceSpy: any;
    let router: Router;

    beforeEach(async () => {
        authServiceSpy = jasmine.createSpyObj('AuthService', ['user'], { user: () => ({ id: 1 }) });
        notifServiceSpy = jasmine.createSpyObj('NotificationsService', ['loadCount', 'getAll', 'markRead']);
        notifServiceSpy.unreadCount = signal(0);

        await TestBed.configureTestingModule({
            imports: [LayoutComponent, RouterTestingModule],
            providers: [
                { provide: AuthService, useValue: authServiceSpy },
                { provide: NotificationsService, useValue: notifServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(LayoutComponent);
        component = fixture.componentInstance;
        router = TestBed.inject(Router);
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('togglePopup should switch showNotifPopup and reload recent notifications', () => {
        notifServiceSpy.getAll.and.returnValue(of([]));
        component.showNotifPopup.set(false);

        component.togglePopup();

        expect(component.showNotifPopup()).toBeTrue();
        expect(notifServiceSpy.getAll).toHaveBeenCalled();
    });

    it('goToNotifications should close popup and navigate', () => {
        component.showNotifPopup.set(true);
        spyOn(router, 'navigate');

        component.goToNotifications();

        expect(component.showNotifPopup()).toBeFalse();
        expect(router.navigate).toHaveBeenCalledWith(['/notifications']);
    });
});
