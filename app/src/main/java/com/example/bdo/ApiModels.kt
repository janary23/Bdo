package com.example.bdo

import com.google.gson.annotations.SerializedName

// ============================================
// REQUEST MODELS
// ============================================

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val full_name: String,
    val phone: String?,
    val address: String?,
    val date_of_birth: String?
)

data class LoanApplicationRequest(
    val user_id: Int,
    val loan_type: String,
    val amount: Double,
    val term_months: Int,
    val monthly_payment: Double
)

// ============================================
// RESPONSE MODELS
// ============================================

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user: User?
)

data class RegisterResponse(
    val success: Boolean,
    val message: String,
    val user_id: Int?
)

data class LoanApplicationResponse(
    val success: Boolean,
    val message: String,
    val application_id: Int?
)

data class LoansResponse(
    val success: Boolean,
    val loans: List<Loan>?,
    val count: Int?
)

data class ApplicationsResponse(
    val success: Boolean,
    val applications: List<LoanApplication>?,
    val count: Int?
)

data class PaymentsResponse(
    val success: Boolean,
    val payments: List<Payment>?,
    val count: Int?,
    val total_paid: Double?
)

data class PaymentResponse(
    val success: Boolean,
    val message: String,
    val payment_id: Int?
)

data class RequirementsResponse(
    val success: Boolean,
    val requirements: List<Requirement>?,
    val count: Int?
)

data class AppointmentsResponse(
    val success: Boolean,
    val appointments: List<Appointment>?,
    val count: Int?
)

data class NotificationsResponse(
    val success: Boolean,
    val notifications: List<Notification>?,
    val count: Int?,
    val unread_count: Int?
)

data class DashboardStatsResponse(
    val success: Boolean,
    val stats: DashboardStats?
)

data class DashboardStats(
    val active_loans_count: Int,
    val next_payment_amount: Double?,
    val next_payment_date: String?,
    val total_balance: Double,
    val pending_applications_count: Int,
    val user_name: String,
    val credit_limit: Double,
    val available_credit: Double
)

data class MarkNotificationReadResponse(
    val success: Boolean,
    val message: String
)

data class RequirementUploadResponse(
    val success: Boolean,
    val message: String,
    val requirement_id: Int?
)

data class UserProfileResponse(
    val success: Boolean,
    val user: User?
)

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String
)

data class CreateAppointmentResponse(
    val success: Boolean,
    val message: String,
    val appointment_id: Int?
)


data class ScheduleResponse(
    val success: Boolean,
    val message: String?,
    val schedule: List<ScheduleItem>?,
    val loan_id: String?
)

// ============================================
// DATA MODELS
// ============================================

data class ScheduleItem(
    val installment_no: Int,
    val due_date: String,
    val amount: Double,
    val status: String
)

data class User(
    val user_id: Int,
    val email: String,
    val full_name: String,
    val phone: String?,
    val address: String?,
    val date_of_birth: String?,
    val monthly_income: Double? = 0.0,
    val occupation: String? = "Employed",
    val status: String,
    val created_at: String?
)

data class Loan(
    val loan_id: Int,
    val loan_type: String,
    val principal_amount: Double,
    val term_months: Int,
    val monthly_payment: Double,
    val remaining_balance: Double,
    val status: String,
    val created_at: String?,
    val next_due_date: String?,
    val next_payment_amount: Double?,
    val progress: Int? // Computed field from API
)

data class LoanApplication(
    val application_id: Int,
    val loan_type: String,
    val amount: Double,
    val status: String,
    val applied_date: String
)

data class Payment(
    val payment_id: Int,
    val amount: Double,
    val payment_date: String,
    val status: String,
    val loan_type: String?,
    val reference_number: String?
)

data class Requirement(
    @SerializedName("RequirementID")
    val id: Int = 0, // Default to 0 as new API doesn't return ID
    
    @SerializedName(value = "RequirementType", alternate = ["requirementType", "requirement_type", "requirementtype", "name"])
    val name: String?, // Mapped to 'name' from API
    
    @SerializedName(value = "Status", alternate = ["status"])
    val status: String?,
    
    @SerializedName(value = "DateUploaded", alternate = ["dateUploaded", "date_uploaded"])
    val dateUploaded: String? = null,
    
    val feedback: String? = null, // Feedback/Notes from admin
    
    val bonus: String? = null // Optional bonus text
)

data class Appointment(
    @SerializedName("AppointmentID")
    val id: Int,
    
    @SerializedName("DateScheduled")
    val date: String,
    
    @SerializedName("TimeScheduled")
    val time: String,
    
    @SerializedName("Status")
    val status: String,
    
    @SerializedName("Notes")
    val notes: String?
)

data class Notification(
    val NotificationID: Int,
    val UserID: Int,
    val Message: String,
    val IsRead: Int,
    val CreatedAt: String
)

data class OtpResponse(
    val success: Boolean,
    val message: String
)

data class VerifyOtpResponse(
    val success: Boolean,
    val message: String,
    val user_id: Int?
)
