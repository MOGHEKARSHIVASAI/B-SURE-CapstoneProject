import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProfileComponent } from './profile.component';
import { BusinessService } from '../services/business.service';
import { AuthService } from '../../../core/services/auth.service';
import { FormsModule } from '@angular/forms';
import { of, throwError } from 'rxjs';

describe('ProfileComponent', () => {
    let component: ProfileComponent;
    let fixture: ComponentFixture<ProfileComponent>;
    let businessSvcSpy: jasmine.SpyObj<BusinessService>;

    beforeEach(async () => {
        businessSvcSpy = jasmine.createSpyObj('BusinessService', ['getMyProfile', 'updateMyProfile', 'createBusiness']);

        await TestBed.configureTestingModule({
            imports: [ProfileComponent, FormsModule],
            providers: [
                { provide: BusinessService, useValue: businessSvcSpy },
                { provide: AuthService, useValue: {} }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(ProfileComponent);
        component = fixture.componentInstance;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('loadProfile should set noProfile to true if profile is not found', () => {
        businessSvcSpy.getMyProfile.and.returnValue(of(null as any));
        component.ngOnInit();
        expect(component.noProfile()).toBeTrue();
        expect(component.loading()).toBeFalse();
    });

    it('save should call updateMyProfile and set success', () => {
        const mockRes = { id: 1 } as any;
        businessSvcSpy.updateMyProfile.and.returnValue(of(mockRes));
        component.form = { id: 1, name: 'test' };

        component.save();

        expect(businessSvcSpy.updateMyProfile).toHaveBeenCalledWith(component.form);
        expect(component.success()).toBeTrue();
        expect(component.saving()).toBeFalse();
    });
});
