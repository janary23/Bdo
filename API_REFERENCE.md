# BDO Loan Application - API Reference

Complete API documentation for the BDO Loan Application system.

**Base URL**: `http://localhost/bdo/api/` (local) or `http://10.0.2.2/bdo/api/` (Android emulator)

## Authentication Endpoints

### POST /register.php
Register a new user account.

**Parameters**:
- `email` (string, required) - User email address
- `password` (string, required) - User password
- `full_name` (string, required) - Full name
- `phone` (string, optional) - Phone number
- `address` (string, optional) - Address
- `date_of_birth` (string, optional) - Date of birth (YYYY-MM-DD)

**Response**:
```json
{
  "success": true,
  "message": "Registration successful",
  "user_id": 1
}
```

### POST /login.php
Authenticate user and get user data.

**Parameters**:
- `email` (string, required)
- `password` (string, required)

**Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "user": {
    "user_id": 1,
    "email": "user@example.com",
    "full_name": "John Doe",
    "phone": "09171234567",
    "address": "123 Main St",
    "date_of_birth": "1990-01-01",
    "status": "active",
    "created_at": "2026-02-01 10:00:00"
  }
}
```

## Loan Application Endpoints

### POST /apply_loan.php
Submit a new loan application.

**Parameters**:
- `user_id` (int, required)
- `loan_type` (string, required) - Personal Loan, Home Loan, Multipurpose Loan, Business Loan
- `amount` (decimal, required)
- `term_months` (int, required)
- `monthly_payment` (decimal, required)

**Response**:
```json
{
  "success": true,
  "message": "Loan application submitted successfully",
  "application_id": 1
}
```

### GET /get_applications.php
Get user's loan applications.

**Parameters**:
- `user_id` (int, required)

**Response**:
```json
{
  "success": true,
  "applications": [
    {
      "application_id": 1,
      "user_id": 1,
      "loan_type": "Personal Loan",
      "amount": 50000.00,
      "term_months": 12,
      "monthly_payment": 4500.00,
      "status": "pending",
      "applied_date": "2026-02-01 10:00:00",
      "processed_date": null
    }
  ],
  "count": 1
}
```

## Loan Management Endpoints

### GET /get_user_loans.php
Get user's active loans.

**Parameters**:
- `user_id` (int, required)

**Response**:
```json
{
  "success": true,
  "loans": [
    {
      "loan_id": 1,
      "user_id": 1,
      "loan_type": "Personal Loan",
      "principal_amount": 50000.00,
      "term_months": 12,
      "monthly_payment": 4500.00,
      "total_paid": 9000.00,
      "remaining_balance": 41000.00,
      "status": "Active",
      "disbursement_date": "2026-01-15",
      "maturity_date": "2027-01-15",
      "progress": 18
    }
  ],
  "count": 1
}
```

### GET /get_payments.php
Get payment history.

**Parameters**:
- `user_id` (int, optional) - Get all payments for user
- `loan_id` (int, optional) - Get payments for specific loan

**Response**:
```json
{
  "success": true,
  "payments": [
    {
      "payment_id": 1,
      "loan_id": 1,
      "user_id": 1,
      "amount": 4500.00,
      "payment_date": "2026-02-01 10:00:00",
      "payment_method": "Online",
      "reference_number": "REF-2026-001",
      "status": "completed",
      "loan_type": "Personal Loan"
    }
  ],
  "count": 1,
  "total_paid": 4500.00
}
```

## Dashboard Endpoints

### GET /get_dashboard_stats.php
Get dashboard statistics for user.

**Parameters**:
- `user_id` (int, required)

**Response**:
```json
{
  "success": true,
  "stats": {
    "active_loans_count": 2,
    "next_payment_amount": 4500.00,
    "next_payment_date": "2027-01-15",
    "total_balance": 91000.00,
    "pending_applications_count": 1,
    "user_name": "John Doe"
  }
}
```

## User Profile Endpoints

### GET /get_user_profile.php
Get user profile information.

**Parameters**:
- `user_id` (int, required)

**Response**:
```json
{
  "success": true,
  "user": {
    "user_id": 1,
    "email": "user@example.com",
    "full_name": "John Doe",
    "phone": "09171234567",
    "address": "123 Main St",
    "date_of_birth": "1990-01-01",
    "status": "active",
    "created_at": "2026-02-01 10:00:00"
  }
}
```

### POST /update_user_profile.php
Update user profile information.

**Parameters**:
- `user_id` (int, required)
- `full_name` (string, required)
- `phone` (string, optional)
- `address` (string, optional)
- `date_of_birth` (string, optional)

**Response**:
```json
{
  "success": true,
  "message": "Profile updated successfully"
}
```

## Requirements/Documents Endpoints

### GET /get_user_requirements.php
Get user's document requirements.

**Parameters**:
- `user_id` (int, required)

**Response**:
```json
{
  "success": true,
  "requirements": [
    {
      "RequirementID": 1,
      "ClientID": 1,
      "RequirementType": "Valid ID",
      "FilePath": "/uploads/id_123.jpg",
      "Status": "Verified",
      "DateUploaded": "2026-02-01 10:00:00",
      "VerificationNotes": null
    }
  ],
  "count": 1
}
```

### POST /upload_requirement.php
Upload a document requirement.

**Parameters**:
- `user_id` (int, required)
- `requirement_type` (string, required)
- `file_path` (string, optional)

**Response**:
```json
{
  "success": true,
  "message": "Requirement uploaded successfully",
  "requirement_id": 1
}
```

## Appointment Endpoints

### GET /get_appointments.php
Get user's appointments.

**Parameters**:
- `user_id` (int, required)

**Response**:
```json
{
  "success": true,
  "appointments": [
    {
      "AppointmentID": 1,
      "ClientID": 1,
      "LoanID": null,
      "DateScheduled": "2026-02-08",
      "TimeScheduled": "10:00:00",
      "Status": "Pending",
      "Notes": "Loan consultation",
      "CreatedAt": "2026-02-01 10:00:00"
    }
  ],
  "count": 1
}
```

### POST /create_appointment.php
Create a new appointment.

**Parameters**:
- `user_id` (int, required)
- `loan_id` (int, optional)
- `date_scheduled` (string, required) - YYYY-MM-DD
- `time_scheduled` (string, required) - HH:MM:SS
- `notes` (string, optional)

**Response**:
```json
{
  "success": true,
  "message": "Appointment created successfully",
  "appointment_id": 1
}
```

## Notification Endpoints

### GET /get_notifications.php
Get user's notifications.

**Parameters**:
- `user_id` (int, required)

**Response**:
```json
{
  "success": true,
  "notifications": [
    {
      "NotificationID": 1,
      "UserID": 1,
      "Message": "Your loan application has been approved",
      "IsRead": 0,
      "CreatedAt": "2026-02-01 10:00:00"
    }
  ],
  "count": 1,
  "unread_count": 1
}
```

### POST /mark_notification_read.php
Mark a notification as read.

**Parameters**:
- `notification_id` (int, required)

**Response**:
```json
{
  "success": true,
  "message": "Notification marked as read"
}
```

## Error Responses

All endpoints return error responses in this format:

```json
{
  "success": false,
  "message": "Error description here"
}
```

Common error messages:
- `"User ID is required"` - Missing required parameter
- `"Invalid request method. Use POST."` - Wrong HTTP method
- `"Database connection error"` - Database issue
- `"User not found"` - Invalid user ID

## Testing

Test all endpoints at once:
```
http://localhost/bdo/api/test_all_endpoints.php
```

## Android Integration

In your Android app, use the ApiService interface:

```kotlin
// Example: Get dashboard stats
val response = ApiClient.apiService.getDashboardStats(userId)
if (response.isSuccessful && response.body()?.success == true) {
    val stats = response.body()?.stats
    // Use stats data
}
```

## Notes

- All endpoints use UTF-8 encoding
- All endpoints support CORS (Access-Control-Allow-Origin: *)
- Date format: `YYYY-MM-DD`
- DateTime format: `YYYY-MM-DD HH:MM:SS`
- Currency values are in PHP Peso (₱)
