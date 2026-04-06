import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NotificationsComponent } from './notifications.component';
import { NotificationsService } from '../services/notifications.service';
import { AuthService } from '../../../core/services/auth.service';
import { of } from 'rxjs';

import { signal } from '@angular/core';

describe('NotificationsComponent', () => {
    let component: NotificationsComponent;
    let fixture: ComponentFixture<NotificationsComponent>;
    let notifServiceSpy: any;

    beforeEach(async () => {
        notifServiceSpy = jasmine.createSpyObj('NotificationsService', ['getAll', 'markRead', 'markAllRead', 'loadCount']);
        notifServiceSpy.unreadCount = signal(0);

        await TestBed.configureTestingModule({
            imports: [NotificationsComponent],
            providers: [
                { provide: NotificationsService, useValue: notifServiceSpy },
                { provide: AuthService, useValue: {} }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(NotificationsComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('ngOnInit should fetch notifications and load count', () => {
        const mockData = [{ id: 1, isRead: false } as any];
        notifServiceSpy.getAll.and.returnValue(of(mockData));

        component.ngOnInit();

        expect(notifServiceSpy.getAll).toHaveBeenCalled();
        expect(notifServiceSpy.loadCount).toHaveBeenCalled();
        expect(component.notifications()).toEqual(mockData);
    });

    it('markRead should only call service if not already read', () => {
        const mockNotif = { id: 1, isRead: false } as any;
        notifServiceSpy.markRead.and.returnValue(of('success'));

        component.markRead(mockNotif);

        expect(notifServiceSpy.markRead).toHaveBeenCalledWith(1);
    });

    it('markRead should NOT call service if already read', () => {
        const mockNotif = { id: 1, isRead: true } as any;

        component.markRead(mockNotif);

        expect(notifServiceSpy.markRead).not.toHaveBeenCalled();
    });

    it('markAll should update local state and count', () => {
        notifServiceSpy.markAllRead.and.returnValue(of('success'));
        component.notifications.set([{ id: 1, isRead: false } as any]);

        component.markAll();

        expect(notifServiceSpy.markAllRead).toHaveBeenCalled();
        expect(component.notifications()[0].isRead).toBeTrue();
    });
});
