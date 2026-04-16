import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  auth:AuthService = inject(AuthService);

  email = '';
  password = '';

  login() {
    this.auth.error.set('');
    if (!this.email.trim() && !this.password.trim()) {
      this.auth.error.set('Email and password are required.');
      setTimeout(() => this.auth.error.set(''), 5000);
      return;
    }
    if (!this.email.trim()) {
      this.auth.error.set('Email is required.');
      setTimeout(() => this.auth.error.set(''), 5000);
      return;
    }
    if (!this.password.trim()) {
      this.auth.error.set('Password is required.');
      setTimeout(() => this.auth.error.set(''), 5000);
      return;
    }
    this.auth.login(this.email, this.password);
  }
}
