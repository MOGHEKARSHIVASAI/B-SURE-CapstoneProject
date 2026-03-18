import { ComponentFixture, TestBed } from '@angular/core/testing';
import { UsersComponent } from './users.component';
import { UsersService } from '../services/users.service';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';

describe('UsersComponent', () => {
    let component: UsersComponent;
    let fixture: ComponentFixture<UsersComponent>;
    let userSvcSpy: jasmine.SpyObj<UsersService>;

    beforeEach(async () => {
        userSvcSpy = jasmine.createSpyObj('UsersService', ['getAll', 'create', 'deactivate', 'activate', 'resetPassword']);

        await TestBed.configureTestingModule({
            imports: [UsersComponent, FormsModule],
            providers: [{ provide: UsersService, useValue: userSvcSpy }]
        }).compileComponents();

        fixture = TestBed.createComponent(UsersComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('load should fetch non-customer users', () => {
        const mockUsers = [
            { id: 1, role: 'UNDERWRITER', active: true },
            { id: 2, role: 'CUSTOMER', active: true }
        ] as any[];
        userSvcSpy.getAll.and.returnValue(of(mockUsers));

        component.load();

        expect(component.users().length).toBe(1);
        expect(component.users()[0].role).toBe('UNDERWRITER');
    });

    it('deactivate should call service and reload if confirmed', () => {
        spyOn(window, 'confirm').and.returnValue(true);
        userSvcSpy.deactivate.and.returnValue(of('success'));
        userSvcSpy.getAll.and.returnValue(of([]));

        component.deactivate(1);

        expect(userSvcSpy.deactivate).toHaveBeenCalledWith(1);
        expect(userSvcSpy.getAll).toHaveBeenCalled();
    });

    it('resetPassword should call service and set resetMsg', () => {
        component.resetUser.set({ id: 1 } as any);
        component.newPassword = 'newPassword123';
        userSvcSpy.resetPassword.and.returnValue(of('success'));

        component.resetPassword();

        expect(userSvcSpy.resetPassword).toHaveBeenCalledWith(1, 'newPassword123');
        expect(component.resetMsg()).toBe('Password reset successfully!');
    });
});
