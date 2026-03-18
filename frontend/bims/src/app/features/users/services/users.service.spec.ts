import { TestBed } from '@angular/core/testing';
import { UsersService } from './users.service';
import { ApiService } from '../../../core/services/api.service';
import { of } from 'rxjs';

describe('UsersService', () => {
    let service: UsersService;
    let apiSpy: jasmine.SpyObj<ApiService>;

    beforeEach(() => {
        const aSpy = jasmine.createSpyObj('ApiService', ['get', 'post', 'put', 'deleteText', 'putText']);

        TestBed.configureTestingModule({
            providers: [
                UsersService,
                { provide: ApiService, useValue: aSpy }
            ]
        });
        service = TestBed.inject(UsersService);
        apiSpy = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('getAll should call api get', () => {
        apiSpy.get.and.returnValue(of([]));
        service.getAll().subscribe();
        expect(apiSpy.get).toHaveBeenCalledWith('/users');
    });

    it('getById should call api get with id', () => {
        apiSpy.get.and.returnValue(of({ id: 1 }));
        service.getById(1).subscribe();
        expect(apiSpy.get).toHaveBeenCalledWith('/users/1');
    });

    it('create should call api post', () => {
        apiSpy.post.and.returnValue(of({ id: 1 }));
        service.create({ email: 'test@test.com' }).subscribe();
        expect(apiSpy.post).toHaveBeenCalledWith('/users', { email: 'test@test.com' });
    });

    it('deactivate should call api deleteText', () => {
        apiSpy.deleteText.and.returnValue(of('deactivated'));
        service.deactivate(1).subscribe();
        expect(apiSpy.deleteText).toHaveBeenCalledWith('/users/1');
    });

    it('activate should call api putText', () => {
        apiSpy.putText.and.returnValue(of('activated'));
        service.activate(1).subscribe();
        expect(apiSpy.putText).toHaveBeenCalledWith('/users/1/activate');
    });

    it('resetPassword should call api putText with data', () => {
        apiSpy.putText.and.returnValue(of('reset'));
        service.resetPassword(1, 'newPass123').subscribe();
        expect(apiSpy.putText).toHaveBeenCalledWith('/users/1/reset-password', { newPassword: 'newPass123' });
    });
});
