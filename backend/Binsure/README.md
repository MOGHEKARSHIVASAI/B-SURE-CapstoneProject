# Business Insurance Management System (BIMS)

A complete full-stack enterprise application for streamlining insurance policy management, claims processing, and underwriting workflows.

## 🏗️ Architecture Overview

**Backend:** Spring Boot 3.x (Java 17)  
**Frontend:** Angular 17+ (SPA)  
**Database:** PostgreSQL / H2 (development)  
**Authentication:** Spring Security + JWT  
**API Documentation:** Springdoc OpenAPI (Swagger UI)

## 📋 System Features

### User Roles & Permissions
- **Business Customer** - View policies, submit claims, upload documents
- **Admin** - Manage users, system configuration, reports
- **Underwriter** - Review applications, approve/reject policies, set premiums
- **Claims Officer** - Review claims, approve/reject/settle claims

## 🗂️ Project Structure

```
src/main/java/org/hartford/binsure/
├── controller/          # REST API endpoints
├── service/             # Business logic & operations
├── repository/          # Data access layer (JPA)
├── entity/              # JPA entity models
├── dto/                 # Data transfer objects
├── enums/               # Application enumerations
├── security/            # JWT & Security config
├── config/              # Application configuration
├── exception/           # Exception handling
└── BinsureApplication.java # Main Spring Boot class
```

## 📚 Database Schema

### Core Entities
- **User** - Central authentication & identity table (UUID PK → Long ID)
- **BusinessCustomer** - Extended profile for CUSTOMER role users
- **InsuranceProduct** - Master list of insurance products offered
- **PolicyApplication** - Track applications pending underwriter review
- **Policy** - Active and historical insurance policies
- **Claim** - Insurance claims filed against policies
- **UnderwriterDecision** - Audit trail of underwriting decisions

## 🔑 Key Design Decisions

1. **Long Integer IDs** - All entities use `Long` (auto-increment) for primary keys
2. **BigDecimal for Money** - Monetary fields use `BigDecimal` for precision
3. **JPA/Hibernate** - ORM for database abstraction
4. **JWT Authentication** - Stateless REST API security
5. **Role-Based Access Control** - @PreAuthorize annotations for authorization
6. **Pagination** - All list endpoints support pagination
7. **Soft Deletes** - `isActive` boolean for safe deletion

## 🔌 API Endpoints

### Authentication (/api/v1/auth)
- `POST /register` - Register new business customer
- `POST /login` - Authenticate & receive JWT
- `POST /refresh-token` - Refresh access token
- `POST /logout` - Invalidate token

### Business Customers (/api/v1/customers)
- `GET /` - List all customers (ADMIN, UNDERWRITER)
- `GET /{id}` - Get customer details
- `GET /me` - Get own profile (CUSTOMER)
- `PUT /me` - Update own profile (CUSTOMER)
- `GET /search` - Search customers

### Insurance Products (/api/v1/products)
- `GET /` - Get all active products
- `GET /{id}` - Get product details
- `GET /category/{category}` - Filter by category
- `POST /` - Create product (ADMIN)
- `PUT /{id}` - Update product (ADMIN)
- `DELETE /{id}` - Deactivate product (ADMIN)

### Policy Applications (/api/v1/applications)
- `POST /` - Submit new application (CUSTOMER)
- `GET /` - Get all applications (ADMIN, UNDERWRITER)
- `GET /{id}` - Get application details
- `GET /my` - Get own applications (CUSTOMER)
- `PUT /{id}` - Update draft application (CUSTOMER)
- `POST /{id}/submit` - Submit for review (CUSTOMER)
- `PUT /{id}/assign` - Assign underwriter (ADMIN)

### Policies (/api/v1/policies)
- `GET /` - Get all policies (ADMIN)
- `GET /{id}` - Get policy details
- `GET /my` - Get own policies (CUSTOMER)
- `GET /number/{policyNumber}` - Lookup by number
- `POST /{applicationId}/issue` - Create policy (UNDERWRITER)
- `PUT /{id}/suspend` - Suspend policy (ADMIN)
- `PUT /{id}/reactivate` - Reactivate policy (ADMIN)
- `POST /{id}/cancel` - Cancel policy

### Claims (/api/v1/claims)
- `POST /` - File new claim (CUSTOMER)
- `GET /` - Get all claims (ADMIN, CLAIMS_OFFICER)
- `GET /{id}` - Get claim details
- `GET /my` - Get own claims (CUSTOMER)
- `PUT /{id}` - Update claim (CUSTOMER)
- `PUT /{id}/assign` - Assign officer (ADMIN)
- `POST /{id}/investigate` - Mark under investigation
- `POST /{id}/approve` - Approve claim
- `POST /{id}/reject` - Reject claim
- `POST /{id}/settle` - Settle claim
- `POST /{id}/appeal` - Appeal rejection

### Underwriting (/api/v1/underwriting)
- `GET /queue` - Get pending review queue
- `POST /{applicationId}/decision` - Submit decision
- `GET /decisions` - Get all decisions
- `GET /risk-score/{applicationId}` - Calculate risk score

## 🔐 Security Features

- **JWT Bearer Authentication** - All endpoints except `/auth` require valid JWT
- **Role-Based Access Control** - @PreAuthorize annotations enforce permissions
- **Password Hashing** - BCrypt for secure password storage
- **CORS Support** - Configured in SecurityConfig
- **SQL Injection Prevention** - JPA parameterized queries
- **CSRF Protection** - Spring Security default (disabled for stateless API)

## 🛠️ Technologies & Dependencies

```xml
<!-- Core Spring Boot -->
<spring-boot-starter-web>
<spring-boot-starter-data-jpa>
<spring-boot-starter-security>
<spring-boot-starter-validation>

<!-- JWT -->
<jjwt-api>0.12.5
<jjwt-impl>0.12.5
<jjwt-jackson>0.12.5

<!-- Database -->
<postgresql>
<h2-database> (development)

<!-- ORM -->
<spring-boot-starter-data-jpa>
<hibernate> (via Spring Data)

<!-- API Documentation -->
<springdoc-openapi-starter-webmvc-ui>3.0.1

<!-- Utils -->
<lombok>1.18.40
<modelmapper>3.2.0

<!-- Email (Optional) -->
<spring-boot-starter-mail>

<!-- AWS S3 (Optional) -->
<aws-java-sdk-s3>2.25.13
```

## 📦 Data Models

### User (All users)
```java
@Entity
@Table(name = "users")
public class User {
    Long id (PK)
    String email (UNIQUE)
    String passwordHash
    UserRole role
    String firstName, lastName, phone
    boolean isActive
    LocalDateTime createdAt, updatedAt
    OneToOne businessCustomer (LAZY)
}
```

### BusinessCustomer (CUSTOMER role)
```java
@Entity
@Table(name = "business_customers")
public class BusinessCustomer {
    Long id (PK, FK to User)
    User user (OneToOne)
    String companyName, companyRegNumber
    String industryType
    BigDecimal annualRevenue
    Integer numEmployees
    // Address fields
    String taxId (UNIQUE)
}
```

### PolicyApplication
```java
@Entity
@Table(name = "policy_applications")
public class PolicyApplication {
    Long id (PK)
    BusinessCustomer customer
    InsuranceProduct product
    BigDecimal coverageAmount
    LocalDate coverageStartDate, coverageEndDate
    ApplicationStatus status
    String riskNotes
    User assignedUnderwriter
    LocalDateTime submittedAt, reviewedAt
}
```

### Policy
```java
@Entity
@Table(name = "policies")
public class Policy {
    Long id (PK)
    String policyNumber (UNIQUE)
    PolicyApplication application
    BusinessCustomer customer
    InsuranceProduct product
    User underwriter
    BigDecimal coverageAmount, annualPremium, deductible
    LocalDate startDate, endDate
    PolicyStatus status
    String policyDocumentUrl
    LocalDateTime issuedAt, cancelledAt
}
```

### Claim
```java
@Entity
@Table(name = "claims")
public class Claim {
    Long id (PK)
    String claimNumber (UNIQUE)
    Policy policy
    BusinessCustomer customer
    User assignedOfficer
    LocalDate incidentDate, claimDate, settlementDate
    BigDecimal claimedAmount, approvedAmount, settledAmount
    String incidentDescription, rejectionReason
    ClaimStatus status
}
```

## 🔄 Workflow Examples

### Policy Issuance Workflow
1. Customer submits PolicyApplication (DRAFT status)
2. Customer submits application for review (SUBMITTED)
3. Admin assigns Underwriter
4. Underwriter reviews and submits decision
5. If APPROVED → Policy created and issued (ACTIVE)
6. If REJECTED → Application status updated

### Claim Processing Workflow
1. Customer files Claim (SUBMITTED status)
2. Admin assigns Claims Officer
3. Officer marks claim UNDER_INVESTIGATION
4. Officer reviews documents and evidence
5. Officer APPROVES/REJECTS claim
6. If APPROVED → Customer can accept settlement
7. Officer marks claim SETTLED

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+ (for production)
- Node.js 18+ (for Angular frontend)

### Build & Run

```bash
# Clone repository
git clone https://github.com/your-org/bims.git
cd bims/Binsure

# Build with Maven
mvn clean install

# Run application
mvn spring-boot:run

# Access Swagger UI
http://localhost:8080/swagger-ui.html

# Access H2 Console (development)
http://localhost:8080/h2-console
```

### Configuration

Update `application.properties`:
```properties
# Database (change from H2 to PostgreSQL)
spring.datasource.url=jdbc:postgresql://localhost:5432/binsure
spring.datasource.username=postgres
spring.datasource.password=your_password

# JWT Secret (change for production)
app.jwtSecret=your_very_long_secret_key_here

# Mail Configuration
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```

## 📝 Enums

- **UserRole** - CUSTOMER, ADMIN, UNDERWRITER, CLAIMS_OFFICER
- **ProductCategory** - LIABILITY, PROPERTY, CYBER, HEALTH, VEHICLE, MARINE
- **ApplicationStatus** - DRAFT, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED
- **PolicyStatus** - ACTIVE, EXPIRED, CANCELLED, SUSPENDED
- **ClaimStatus** - SUBMITTED, ASSIGNED, UNDER_INVESTIGATION, APPROVED, REJECTED, SETTLED, APPEALED
- **DecisionType** - APPROVED, REJECTED, REFER_TO_SENIOR

## 📖 API Documentation

Full Swagger/OpenAPI documentation available at:
```
http://localhost:8080/swagger-ui.html
```

## 🧪 Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn clean test jacoco:report
```

## 📄 License

Licensed under Apache License 2.0. See LICENSE file for details.

## 👥 Team

- **Hartford Insurance Solutions**
- Contact: support@hartford-bims.com

## 🔗 Links

- API Docs: `/swagger-ui.html`
- Database Console: `/h2-console` (dev only)
- Health Check: `/actuator/health`

---

**Version:** 1.0.0  
**Last Updated:** February 2026

