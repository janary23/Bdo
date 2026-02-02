package com.example.bdo

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

/**
 * API Service Interface
 * Defines all API endpoints for the BDO Loan App
 */
interface ApiService {
    
    // ============================================
    // AUTHENTICATION
    // ============================================
    
    @FormUrlEncoded
    @POST("login.php")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<LoginResponse>
    
    @FormUrlEncoded
    @POST("register.php")
    suspend fun register(
        @Field("email") email: String,
        @Field("password") password: String,
        @Field("full_name") fullName: String,
        @Field("phone") phone: String?,
        @Field("address") address: String?,
        @Field("date_of_birth") dateOfBirth: String?,
        @Field("occupation") occupation: String?,
        @Field("monthly_income") monthlyIncome: Double?,
        @Field("otp") otp: String
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST("send_verification_otp.php")
    suspend fun sendVerificationOtp(
        @Field("email") email: String
    ): Response<OtpResponse>
    
    // ============================================
    // LOAN APPLICATIONS
    // ============================================
    
    @FormUrlEncoded
    @POST("apply_loan.php")
    suspend fun applyLoan(
        @Field("user_id") userId: Int,
        @Field("loan_type") loanType: String,
        @Field("amount") amount: Double,
        @Field("term_months") termMonths: Int,
        @Field("monthly_payment") monthlyPayment: Double
    ): Response<LoanApplicationResponse>
    
    @GET("get_applications.php")
    suspend fun getUserApplications(
        @Query("user_id") userId: Int
    ): Response<ApplicationsResponse>
    
    // ============================================
    // LOANS
    // ============================================
    
    @GET("get_user_loans.php")
    suspend fun getUserLoans(
        @Query("user_id") userId: Int
    ): Response<LoansResponse>
    
    @GET("get_loan_schedule.php")
    suspend fun getLoanSchedule(
        @Query("loan_id") loanId: Int
    ): Response<ScheduleResponse>
    
    // ============================================
    // PAYMENTS
    // ============================================
    
    @GET("get_payments.php")
    suspend fun getUserPayments(
        @Query("user_id") userId: Int
    ): Response<PaymentsResponse>
    
    @GET("get_payments.php")
    suspend fun getLoanPayments(
        @Query("loan_id") loanId: Int
    ): Response<PaymentsResponse>
    
    @FormUrlEncoded
    @POST("process_payment.php")
    suspend fun processPayment(
        @Field("user_id") userId: Int,
        @Field("loan_id") loanId: Int,
        @Field("amount") amount: Double,
        @Field("payment_method") method: String,
        @Field("reference_number") ref: String
    ): Response<PaymentResponse>
    
    // ============================================
    // DASHBOARD
    // ============================================
    
    @GET("get_dashboard_stats.php")
    suspend fun getDashboardStats(
        @Query("user_id") userId: Int
    ): Response<DashboardStatsResponse>
    
    // ============================================
    // REQUIREMENTS
    // ============================================
    
    @GET("get_user_requirements.php")
    suspend fun getUserRequirements(
        @Query("user_id") userId: Int
    ): Response<RequirementsResponse>
    
    // ============================================
    // USER PROFILE
    // ============================================
    
    @GET("get_user_profile.php")
    suspend fun getUserProfile(
        @Query("user_id") userId: Int
    ): Response<UserProfileResponse>
    
    @FormUrlEncoded
    @POST("update_user_profile.php")
    suspend fun updateUserProfile(
        @Field("user_id") userId: Int,
        @Field("full_name") fullName: String,
        @Field("phone") phone: String?,
        @Field("address") address: String?,
        @Field("date_of_birth") dateOfBirth: String?,
        @Field("occupation") occupation: String?,
        @Field("monthly_income") monthlyIncome: Double?
    ): Response<UpdateProfileResponse>
    
    // ============================================
    // APPOINTMENTS
    // ============================================
    
    @GET("get_appointments.php")
    suspend fun getAppointments(
        @Query("user_id") userId: Int
    ): Response<AppointmentsResponse>
    
    @FormUrlEncoded
    @POST("create_appointment.php")
    suspend fun createAppointment(
        @Field("user_id") userId: Int,
        @Field("loan_id") loanId: Int?,
        @Field("date_scheduled") dateScheduled: String,
        @Field("time_scheduled") timeScheduled: String,
        @Field("notes") notes: String?
    ): Response<CreateAppointmentResponse>
    
    // ============================================
    // NOTIFICATIONS
    // ============================================
    
    @GET("get_notifications.php")
    suspend fun getNotifications(
        @Query("user_id") userId: Int
    ): Response<NotificationsResponse>
    
    @FormUrlEncoded
    @POST("mark_notification_read.php")
    suspend fun markNotificationRead(
        @Field("notification_id") notificationId: Int
    ): Response<MarkNotificationReadResponse>
    
    // ============================================
    // REQUIREMENTS UPLOAD
    // ============================================
    
    @FormUrlEncoded
    @POST("upload_requirement.php")
    suspend fun uploadRequirement(
        @Field("user_id") userId: Int,
        @Field("requirement_type") requirementType: String,
        @Field("file_path") filePath: String?
    ): Response<RequirementUploadResponse>
    
    // ============================================
    // FORGOT PASSWORD
    // ============================================
    
    @FormUrlEncoded
    @POST("send_reset_otp.php")
    suspend fun sendResetOtp(
        @Field("email") email: String
    ): Response<OtpResponse>
    
    @FormUrlEncoded
    @POST("verify_reset_otp.php")
    suspend fun verifyResetOtp(
        @Field("email") email: String,
        @Field("otp") otp: String
    ): Response<VerifyOtpResponse>
    
    @FormUrlEncoded
    @POST("reset_password.php")
    suspend fun resetPassword(
        @Field("email") email: String,
        @Field("otp") otp: String,
        @Field("new_password") newPassword: String
    ): Response<OtpResponse>
}


/**
 * API Client Singleton
 * Provides configured Retrofit instance
 */
object ApiClient {
    
    // XAMPP Local Development
    // 10.0.2.2 is how Android Emulator accesses your PC's localhost
    // Change back to https://adminbdo.ct.ws/api/ when deploying to production
    private const val BASE_URL = "http://10.0.2.2/bdo/api/"
    
    // For physical device on same WiFi, use your PC's IP:
    // private const val BASE_URL = "http://192.168.1.XXX/bdo/api/"
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
