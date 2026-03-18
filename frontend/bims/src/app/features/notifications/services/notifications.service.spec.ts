import { TestBed } from '@angular/core/testing';
import { NotificationsService } from './notifications.service';
import { ApiService } from '../../../core/services/api.service';
import { of } from 'rxjs';
import { Notification } from '../../../core/models/models';

describe('NotificationsService', () => {
    let service: NotificationsService;
    let apiSpy: jasmine.SpyObj<ApiService>;

    beforeEach(() => {
        const aSpy = jasmine.createSpyObj('ApiService', ['get', 'postText', 'putText', 'deleteText']);

        TestBed.configureTestingModule({
            providers: [
                NotificationsService,
                { provide: ApiService, useValue: aSpy }
            ]
        });
        service = TestBed.inject(NotificationsService);
        apiSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('getAll should call api get', () => {
        apiSpy.get.and.returnValue(of([]));
        service.getAll().subscribe();
        expect(apiSpy.get).toHaveBeenCalledWith('/notifications/user');
    });

    it('loadCount should set unreadCount', () => {
        apiSpy.get.and.returnValue(of({ unreadCount: 5 }));
        service.loadCount();
        expect(service.unreadCount()).toBe(5);
        expect(apiSpy.get).toHaveBeenCalledWith('/notifications/user/count');
    });

    it('markRead should call api putText', () => {
        apiSpy.putText.and.returnValue(of('success'));
        service.markRead(1).subscribe();
        expect(apiSpy.putText).toHaveBeenCalledWith('/notifications/1/read');
    });

    it('markAllRead should call api putText', () => {
        apiSpy.putText.and.returnValue(of('success'));
        service.markAllRead().subscribe();
        expect(apiSpy.putText).toHaveBeenCalledWith('/notifications/user/read-all');
    });
});
