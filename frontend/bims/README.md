# BIMS — Business Insurance Management System
## Angular 17 Frontend

### Setup

```bash
npm install
ng serve
```
Open: http://localhost:4200

### Architecture
```
src/app/
  core/
    guards/       auth.guard.ts, roleGuard
    interceptors/ auth.interceptor.ts (JWT)
    models/       models.ts (all interfaces)
    services/     auth.service.ts, api.service.ts
  features/
    auth/         login, register
    dashboard/    role-based stats
    customers/    profile (customer), list (admin/uw)
    products/     browse + CRUD (admin)
    applications/ apply, submit, assign underwriter
    underwriting/ queue, risk score, submit decision
    policies/     issue, cancel, suspend, reactivate
    claims/       file, assign, investigate, approve/reject/settle
    payments/     make payment, history
    notifications/ list, mark read
    users/        staff management (admin)
  shared/
    components/   layout.component (sidebar)
```

### Roles
| Role | Access |
|---|---|
| CUSTOMER | Register, apply, pay premiums, file claims, accept/reject decisions |
| ADMIN | Full access — manage users, products, assign UW/officers, issue policies |
| UNDERWRITER | View queue, submit decisions, calculate risk scores |
| CLAIMS_OFFICER | Manage assigned claims — investigate, approve, reject, settle |

### Backend
- URL: `http://localhost:8050/api/v1`
- Auth: JWT (24h access, 7d refresh)
- Spring Boot 3.x / MySQL

### Features Used
- Angular 17 Signals (`signal()`, `computed()`, `effect()`)
- Standalone components (no NgModules)
- Functional guards & interceptors
- `@if` / `@for` control flow syntax
- Lazy-loaded routes
