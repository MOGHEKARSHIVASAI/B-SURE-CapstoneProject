import { ComponentFixture, TestBed } from '@angular/core/testing';
import { RegisterComponent } from './register.component';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';

describe('RegisterComponent', () => {
    let component: RegisterComponent;
    let fixture: ComponentFixture<RegisterComponent>;
    let authServiceSpy: jasmine.SpyObj<AuthService>;

    beforeEach(async () => {
        const spy = jasmine.createSpyObj('AuthService', ['register']);

        await TestBed.configureTestingModule({
            imports: [RegisterComponent, FormsModule, RouterTestingModule],
            providers: [{ provide: AuthService, useValue: spy }]
        }).compileComponents();

        fixture = TestBed.createComponent(RegisterComponent);
        component = fixture.componentInstance;
        authServiceSpy = TestBed.inject(AuthService) as jasmine.SpyObj<AuthService>;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('register should call auth register with form data', () => {
        component.form.firstName = 'John';
        component.form.email = 'john@example.com';

        component.register();

        expect(authServiceSpy.register).toHaveBeenCalledWith(component.form);
    });
});
