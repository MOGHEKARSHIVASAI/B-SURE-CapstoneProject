import { Routes } from '@angular/router';
import { authGuard, roleGuard, guestGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  { path: '', loadComponent: () => import('./features/home/home.component').then(m => m.HomeComponent), canActivate: [guestGuard] },
  { path: 'login', loadComponent: () => import('./features/auth/components/login.component').then(m => m.LoginComponent), canActivate: [guestGuard] },
  { path: 'register', loadComponent: () => import('./features/auth/components/register.component').then(m => m.RegisterComponent), canActivate: [guestGuard] },
  {
    path: '',
    loadComponent: () => import('./shared/components/layout.component').then(m => m.LayoutComponent),
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'profile', loadComponent: () => import('./features/customers/components/profile.component').then(m => m.ProfileComponent) },
      { path: 'businesses', canActivate: [roleGuard(['CUSTOMER'])], loadComponent: () => import('./features/customers/components/businesses.component').then(m => m.BusinessesComponent) },
      { path: 'products', loadComponent: () => import('./features/products/components/products.component').then(m => m.ProductsComponent) },
      { path: 'applications', loadComponent: () => import('./features/applications/components/applications.component').then(m => m.ApplicationsComponent) },
      { path: 'underwriting', canActivate: [roleGuard(['UNDERWRITER'])], loadComponent: () => import('./features/underwriting/components/underwriting.component').then(m => m.UnderwritingComponent) },
      { path: 'policies', loadComponent: () => import('./features/policies/components/policies.component').then(m => m.PoliciesComponent) },
      { path: 'claims', loadComponent: () => import('./features/claims/components/claims.component').then(m => m.ClaimsComponent) },
      { path: 'payments', loadComponent: () => import('./features/payments/components/payments.component').then(m => m.PaymentsComponent) },
      { path: 'notifications', loadComponent: () => import('./features/notifications/components/notifications.component').then(m => m.NotificationsComponent) },
      { path: 'users', canActivate: [roleGuard(['ADMIN'])], loadComponent: () => import('./features/users/components/users.component').then(m => m.UsersComponent) },
      { path: 'customers', canActivate: [roleGuard(['ADMIN', 'UNDERWRITER'])], loadComponent: () => import('./features/customers/components/customers.component').then(m => m.CustomersComponent) },
    ]
  },
  { path: '**', redirectTo: '' }
];
