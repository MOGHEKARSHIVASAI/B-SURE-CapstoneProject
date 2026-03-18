import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './register.component.html'
})
export class RegisterComponent {
  auth = inject(AuthService);

  form = {
    firstName: '', lastName: '', email: '', password: '', phone: '',
    companyName: '', companyRegNumber: '', industryType: '',
    annualRevenue: null as number | null, numEmployees: null as number | null,
    addressLine1: '', city: '', state: '', postalCode: '', country: 'India', taxId: ''
  };

  register() {
    this.auth.register(this.form);
  }
}
