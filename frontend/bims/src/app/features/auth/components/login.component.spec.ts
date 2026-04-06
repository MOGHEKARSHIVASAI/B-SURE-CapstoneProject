import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';

describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        const spy = jasmine.createSpyObj('AuthService', ['login', 'error'], { error: { set: () => {} } as any });

        await TestBed.configureTestingModule({
            imports: [LoginComponent, FormsModule, RouterTestingModule],
            providers: [{ provide: AuthService, useValue: spy }]
        }).compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('login call with empty fields should set error', () => {
        component.email = '';
        component.password = '';
        const errorSpy = spyOn(authServiceSpy.error, 'set');

        component.login();

        expect(errorSpy).toHaveBeenCalledWith('Email and password are required.');
        expect(authServiceSpy.login).not.toHaveBeenCalled();
    });

    it('login call with valid fields should call auth login', () => {
        component.email = 'test@test.com';
        component.password = 'password';

        component.login();

        expect(authServiceSpy.login).toHaveBeenCalledWith('test@test.com', 'password');
    });
});
