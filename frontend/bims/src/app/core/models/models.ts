// models/user.model.ts
export type Role = 'CUSTOMER' | 'ADMIN' | 'UNDERWRITER' | 'CLAIMS_OFFICER';

export interface User {
  id: number;
  businessId?: number;
  underwriterId?: number;
  email: string;
  firstName: string;
  lastName: string;
  phone?: string;
  role: Role;
  isActive?: boolean; // from auth endpoints
  active?: boolean;   // from user management endpoints (Spring serializes isActive → active)
  createdAt: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
}

export interface Business {
  id: number;
  email: string;
  userId?: number;
  firstName: string;
  lastName: string;
  phone?: string;
  companyName: string;
  companyRegNumber?: string;
  industryType?: string;
  annualRevenue?: number;
  numEmployees?: number;
  addressLine1?: string;
  addressLine2?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  taxId?: string;
  createdAt: string;
}

export type Customer = Business; // Alias for backward compatibility if needed during transition

export interface Product {
  id: number;
  productName: string;
  productCode: string;
  category: 'LIABILITY' | 'PROPERTY' | 'CYBER' | 'HEALTH' | 'VEHICLE' | 'MARINE';
  description?: string;
  basePremiumRate: number;
  minCoverageAmount?: number;
  maxCoverageAmount?: number;
  isActive: boolean;
  active?: boolean;
  createdAt: string;
}

export type AppStatus = 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'CUSTOMER_ACCEPTED' | 'CUSTOMER_REJECTED' | 'POLICY_ISSUED';

export interface Application {
  id: number;
  businessId: number;
  companyName: string;
  productId: number;
  productName: string;
  coverageAmount: number;
  coverageStartDate: string;
  coverageEndDate: string;
  status: AppStatus;
  riskNotes?: string;
  assignedUnderwriterId?: number;
  assignedUnderwriterName?: string;
  submittedAt?: string;
  reviewedAt?: string;
  annualPremium?: number;
  premiumAdjustmentPct?: number;
  documentCount: number;
  createdAt: string;
}

export interface Decision {
  id: number;
  applicationId: number;
  underwriterId: number;
  underwriterName: string;
  decision: 'APPROVED' | 'REJECTED' | 'REFER_TO_SENIOR';
  riskScore?: number;
  premiumAdjustmentPct?: number;
  comments?: string;
  decidedAt: string;
}

export type PolicyStatus = 'ACTIVE' | 'EXPIRED' | 'CANCELLED' | 'SUSPENDED';

export interface Policy {
  id: number;
  policyNumber: string;
  applicationId?: number;
  businessId: number;
  companyName: string;
  productId: number;
  productName: string;
  underwriterId: number;
  underwriterName: string;
  coverageAmount: number;
  annualPremium: number;
  deductible?: number;
  startDate: string;
  endDate: string;
  status: PolicyStatus;
  policyDocumentUrl?: string;
  issuedAt: string;
  cancelledAt?: string;
  cancellationReason?: string;
}

export type ClaimStatus = 'DRAFT' | 'SUBMITTED' | 'ASSIGNED' | 'UNDER_INVESTIGATION' | 'APPROVED' | 'REJECTED' | 'SETTLED' | 'APPEALED';

export interface Claim {
  id: number;
  claimNumber: string;
  policyId: number;
  policyNumber: string;
  productName?: string;
  businessId: number;
  companyName: string;
  assignedOfficerId?: number;
  assignedOfficerName?: string;
  incidentDate: string;
  claimDate: string;
  claimedAmount: number;
  approvedAmount?: number;
  settledAmount?: number;
  incidentDescription: string;
  rejectionReason?: string;
  status: ClaimStatus;
  documentCount: number;
  settlementDate?: string;
  createdAt: string;
}

export interface Payment {
  id: number;
  paymentReference: string;
  applicationId: number;
  businessId: number;
  companyName: string;
  amount: number;
  status: 'PENDING' | 'PAID' | 'FAILED' | 'REFUNDED';
  remarks?: string;
  paidAt?: string;
  createdAt: string;
}

export interface Notification {
  id: number;
  type: string;
  title: string;
  message: string;
  referenceId?: number;
  isRead: boolean;
  createdAt: string;
}

export type DocumentType =
  | 'BUSINESS_PROOF'
  | 'GST_CERTIFICATE'
  | 'ASSET_PROOF'
  | 'PREVIOUS_INSURANCE'
  | 'RISK_PHOTO'
  | 'CLAIM_PHOTO'
  | 'POLICE_REPORT'
  | 'REPAIR_BILL'
  | 'OTHER';

export interface Doc {
  id: number;
  fileName: string;
  fileType: string;
  documentType: DocumentType;
  uploadedAt: string;
}

export interface ApiError {
  status: number;
  error: string;
  message: string;
  timestamp: string;
  path: string;
}

