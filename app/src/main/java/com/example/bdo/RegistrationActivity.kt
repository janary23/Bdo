package com.example.bdo

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegistrationActivity : AppCompatActivity() {

    private var currentStep = 1
    private val totalSteps = 3

    // Views
    private lateinit var stepIndicator: TextView
    private lateinit var stepProgressBar: ProgressBar
    private lateinit var step1Layout: View
    private lateinit var step2Layout: View
    private lateinit var step3Layout: View
    private lateinit var backButton: Button
    private lateinit var nextButton: Button

    // Step 1 Fields
    private lateinit var usernameInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var firstNameInput: TextInputEditText
    private lateinit var lastNameInput: TextInputEditText
    private lateinit var dobInput: TextInputEditText
    private lateinit var nationalityDropdown: AutoCompleteTextView
    private lateinit var mobileInput: TextInputEditText
    private lateinit var altMobileInput: TextInputEditText
    private lateinit var addressInput: TextInputEditText
    private lateinit var employmentStatusDropdown: AutoCompleteTextView
    private lateinit var dynamicFieldsLayout: LinearLayout
    private lateinit var employerInput: TextInputEditText
    private lateinit var incomeInput: TextInputEditText
    private lateinit var yearsEmployedInput: TextInputEditText
    private lateinit var termsCheckbox: CheckBox
    private lateinit var termsTextView: TextView
    private lateinit var ageValidationText: TextView
    
    // Step 2 Fields
    private lateinit var documentAcknowledgeBox: CheckBox
    
    // Step 3 Fields
    private lateinit var reviewFirstName: TextView
    private lateinit var reviewLastName: TextView
    private lateinit var reviewDob: TextView
    private lateinit var reviewNationality: TextView
    private lateinit var reviewEmail: TextView
    private lateinit var reviewMobile: TextView
    private lateinit var reviewAddress: TextView
    private lateinit var reviewEmploymentStatus: TextView
    private lateinit var reviewEmployer: TextView
    private lateinit var reviewIncome: TextView
    private lateinit var reviewYears: TextView
    private lateinit var finalConfirmBox: CheckBox
    private lateinit var contactConsentBox: CheckBox

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        initializeViews()
        setupListeners()
        updateStepUI()
    }

    private fun initializeViews() {
        stepIndicator = findViewById(R.id.stepIndicator)
        stepProgressBar = findViewById(R.id.stepProgressBar)
        step1Layout = findViewById(R.id.step1Layout)
        step2Layout = findViewById(R.id.step2Layout)
        step3Layout = findViewById(R.id.step3Layout)
        backButton = findViewById(R.id.backButton)
        nextButton = findViewById(R.id.nextButton)

        // Step 1
        usernameInput = findViewById(R.id.usernameEditText)
        emailInput = findViewById(R.id.regEmailEditText)
        passwordInput = findViewById(R.id.regPasswordEditText)
        confirmPasswordInput = findViewById(R.id.confirmPasswordEditText)
        firstNameInput = findViewById(R.id.firstNameEditText)
        lastNameInput = findViewById(R.id.lastNameEditText)
        dobInput = findViewById(R.id.dobEditText)
        nationalityDropdown = findViewById(R.id.nationalityDropdown)
        mobileInput = findViewById(R.id.mobileEditText)
        altMobileInput = findViewById(R.id.altMobileEditText)
        addressInput = findViewById(R.id.addressEditText)
        employmentStatusDropdown = findViewById(R.id.employmentStatusDropdown)
        // Dynamic Fields Container
        dynamicFieldsLayout = findViewById<LinearLayout>(R.id.dynamicEmploymentFields)
        employerInput = findViewById(R.id.employerEditText)
        incomeInput = findViewById(R.id.incomeEditText)
        yearsEmployedInput = findViewById(R.id.yearsEmployedEditText)
        termsCheckbox = findViewById(R.id.termsCheckbox)
        termsTextView = findViewById(R.id.termsTextView)
        ageValidationText = findViewById(R.id.ageValidationText)
        
        // Step 2
        documentAcknowledgeBox = findViewById(R.id.documentAcknowledgeBox)
        
        // Step 3
        reviewFirstName = findViewById(R.id.reviewFirstName)
        reviewLastName = findViewById(R.id.reviewLastName)
        reviewDob = findViewById(R.id.reviewDob)
        reviewNationality = findViewById(R.id.reviewNationality)
        reviewEmail = findViewById(R.id.reviewEmail)
        reviewMobile = findViewById(R.id.reviewMobile)
        reviewAddress = findViewById(R.id.reviewAddress)
        
        reviewEmploymentStatus = findViewById(R.id.reviewEmploymentStatus)
        reviewEmployer = findViewById(R.id.reviewEmployer)
        reviewIncome = findViewById(R.id.reviewIncome)
        reviewYears = findViewById(R.id.reviewYears)
        
        finalConfirmBox = findViewById(R.id.finalConfirmBox)
        contactConsentBox = findViewById(R.id.contactConsentBox)

        // Setup Dropdowns
        val nationalities = resources.getStringArray(R.array.nationalities)
        val adapterNationality = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, nationalities)
        nationalityDropdown.setAdapter(adapterNationality)

        val statuses = resources.getStringArray(R.array.employment_status)
        val adapterStatus = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statuses)
        employmentStatusDropdown.setAdapter(adapterStatus)
    }

    private fun setupListeners() {
        nextButton.setOnClickListener {
            if (validateCurrentStep()) {
                if (currentStep < totalSteps) {
                    currentStep++
                    updateStepUI()
                } else {
                    submitRegistration()
                }
            }
        }

        backButton.setOnClickListener {
            if (currentStep > 1) {
                currentStep--
                updateStepUI()
            } else {
                // If on Step 1, go back to Login (finish registration activity)
                finish()
            }
        }

        dobInput.setOnClickListener {
            showDatePicker()
        }
        
        // Employment Status Listener to toggle visibility of Employer/Income fields
        employmentStatusDropdown.setOnItemClickListener { _, _, position, _ ->
            val status = employmentStatusDropdown.adapter.getItem(position).toString()
            val isEmployed = status.contains("Employed") || status == "Freelancer"
            
            dynamicFieldsLayout.visibility = if (isEmployed) View.VISIBLE else View.GONE
        }
        
        // Terms Interaction
        val termsContainer = findViewById<LinearLayout>(R.id.termsContainer)
        
        termsContainer.setOnClickListener {
            if (!termsCheckbox.isChecked) {
                showTermsDialog {
                    termsCheckbox.isChecked = true
                    Toast.makeText(this, "Terms Accepted.", Toast.LENGTH_SHORT).show()
                }
            } else {
                termsCheckbox.isChecked = false
            }
        }
    }
    
    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            
            val format = "yyyy-MM-dd"
            val sdf = SimpleDateFormat(format, Locale.US)
            dobInput.setText(sdf.format(calendar.time))
            
            // Validate Age
            val today = Calendar.getInstance()
            var age = today.get(Calendar.YEAR) - calendar.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < calendar.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            
            if (age < 18) {
                ageValidationText.text = "✗ Must be 18+ (Current: $age)"
                ageValidationText.setTextColor(getColor(R.color.red_600))
                dobInput.error = "Must be 18+"
            } else {
                ageValidationText.text = "✓ Age Verified: $age years"
                ageValidationText.setTextColor(getColor(R.color.black)) // or green
                dobInput.error = null
            }
        }

        DatePickerDialog(this, dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun validateCurrentStep(): Boolean {
        if (currentStep == 1) {
            // Validate Step 1
            if (usernameInput.text.isNullOrEmpty()) { 
                usernameInput.error = "Required"
                usernameInput.requestFocus()
                return false 
            }
            
            val email = emailInput.text.toString()
            if (email.isEmpty()) { 
                emailInput.error = "Required"
                emailInput.requestFocus()
                return false 
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Invalid email address"
                emailInput.requestFocus()
                return false
            }

            val password = passwordInput.text.toString()
            val confirmPass = confirmPasswordInput.text.toString()
            
            if (password.isEmpty()) { 
                passwordInput.error = "Required"
                passwordInput.requestFocus()
                return false 
            }
            if (password.length < 8) {
                passwordInput.error = "Must be at least 8 characters"
                passwordInput.requestFocus()
                return false
            }
            // Relaxed Password Validation (Common issue)
            if (!password.matches(Regex(".*[A-Z].*"))) {
                passwordInput.error = "Must contain at least one uppercase letter"
                passwordInput.requestFocus()
                return false
            }
            if (!password.matches(Regex(".*[0-9].*"))) {
                passwordInput.error = "Must contain at least one number"
                passwordInput.requestFocus()
                return false
            }
            
            if (password != confirmPass) {
                confirmPasswordInput.error = "Passwords do not match"
                confirmPasswordInput.requestFocus()
                return false
            }

            if (firstNameInput.text.isNullOrEmpty()) { 
                firstNameInput.error = "Required"
                firstNameInput.requestFocus()
                return false 
            }
            if (lastNameInput.text.isNullOrEmpty()) { 
                lastNameInput.error = "Required"
                lastNameInput.requestFocus()
                return false 
            }
            
            if (dobInput.text.isNullOrEmpty()) { 
                dobInput.error = "Required"
                dobInput.requestFocus() // Will trigger date picker usually
                Toast.makeText(this, "Please select your Date of Birth", Toast.LENGTH_SHORT).show()
                return false 
            }
            if (ageValidationText.text.contains("Must be 18+")) {
                Toast.makeText(this, "You must be 18 years or older.", Toast.LENGTH_SHORT).show()
                return false
            }

            val mobile = mobileInput.text.toString()
            if (mobile.isEmpty()) { 
                mobileInput.error = "Required"
                mobileInput.requestFocus()
                return false 
            } else if (!mobile.matches(Regex("^09\\d{9}$"))) {
                mobileInput.error = "Must start with 09 and be 11 digits"
                mobileInput.requestFocus()
                return false
            }

            if (addressInput.text.isNullOrEmpty()) { 
                addressInput.error = "Required"
                addressInput.requestFocus()
                return false 
            }
            
            // Employment Validation - CHECK VISIBILITY OF CONTAINER
            if (dynamicFieldsLayout.visibility == View.VISIBLE) {
                if (employerInput.text.isNullOrEmpty()) { 
                    employerInput.error = "Required"
                    employerInput.requestFocus()
                    return false 
                }
                
                val incomeStr = incomeInput.text.toString()
                if (incomeStr.isEmpty()) {
                    incomeInput.error = "Required"
                    incomeInput.requestFocus()
                    return false
                }
                val income = incomeStr.toDoubleOrNull() ?: 0.0
                if (income < 15000) {
                     incomeInput.error = "Minimum income is ₱15,000.00"
                     incomeInput.requestFocus()
                     return false
                }
                
                if (yearsEmployedInput.text.isNullOrEmpty()) { 
                    yearsEmployedInput.error = "Required"
                    yearsEmployedInput.requestFocus()
                    return false 
                }
            }

            if (!termsCheckbox.isChecked) {
                Toast.makeText(this, "Please agree to the Terms", Toast.LENGTH_SHORT).show()
                return false
            }
        } else if (currentStep == 2) {
             if (!documentAcknowledgeBox.isChecked) {
                Toast.makeText(this, "Please acknowledge the document requirements", Toast.LENGTH_SHORT).show()
                return false
            }
        } else if (currentStep == 3) {
            if (!finalConfirmBox.isChecked || !contactConsentBox.isChecked) {
                 Toast.makeText(this, "Please confirm all checkboxes to submit", Toast.LENGTH_SHORT).show()
                 return false
            }
        }
        return true
    }

    private fun updateStepUI() {
        // Reset Progress
        stepProgressBar.progress = (currentStep * 100) / totalSteps
        
        // Visibility
        step1Layout.visibility = if (currentStep == 1) View.VISIBLE else View.GONE
        step2Layout.visibility = if (currentStep == 2) View.VISIBLE else View.GONE
        step3Layout.visibility = if (currentStep == 3) View.VISIBLE else View.GONE

        // Buttons
        // Buttons
        // Always show back button (Step 1 -> Back to Login, Step > 1 -> Prev Step)
        backButton.visibility = View.VISIBLE
        nextButton.text = if (currentStep == totalSteps) "Submit Application" else "Continue"
        
        // Headers
        when (currentStep) {
            1 -> stepIndicator.text = "Step 1 of 3: Personal Info"
            2 -> stepIndicator.text = "Step 2 of 3: Documents"
            3 -> {
                stepIndicator.text = "Step 3 of 3: Review"
                populateReview()
            }
        }
    }
    
    private fun populateReview() {
        reviewFirstName.text = firstNameInput.text
        reviewLastName.text = lastNameInput.text
        reviewNationality.text = nationalityDropdown.text
        reviewEmail.text = emailInput.text
        reviewMobile.text = mobileInput.text
        reviewAddress.text = addressInput.text
        
        // Employment & Financials
        val empStatus = employmentStatusDropdown.text.toString()
        reviewEmploymentStatus.text = empStatus
        
        if (dynamicFieldsLayout.visibility == View.VISIBLE) {
             reviewEmployer.text = employerInput.text
             
             val income = incomeInput.text.toString().toDoubleOrNull() ?: 0.0
             reviewIncome.text = "₱%,.2f".format(income)
             
             reviewYears.text = "${yearsEmployedInput.text} years"
        } else {
             reviewEmployer.text = "N/A"
             reviewIncome.text = "₱0"
             reviewYears.text = "0 years"
        }
        
        // DOB & Age
        val dob = dobInput.text.toString()
        val today = Calendar.getInstance()
        // We know calendar has the set date if user picked it, but safe to parse or use calendar
        var age = today.get(Calendar.YEAR) - calendar.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < calendar.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        reviewDob.text = "$dob\n($age years old)"
    }

    private fun submitRegistration() {
        nextButton.isEnabled = false
        nextButton.text = "Sending OTP..."
        
        val email = emailInput.text.toString().trim()
        
        lifecycleScope.launch {
            try {
                // 1. Send OTP
                val response = ApiClient.apiService.sendVerificationOtp(email)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@RegistrationActivity, "OTP Sent to $email", Toast.LENGTH_SHORT).show()
                    showOtpVerificationDialog()
                } else {
                    val errorMessage = response.body()?.message ?: "Failed to send OTP"
                    Toast.makeText(this@RegistrationActivity, errorMessage, Toast.LENGTH_LONG).show()
                    nextButton.isEnabled = true
                    nextButton.text = "Submit Application"
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegistrationActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                nextButton.isEnabled = true
                nextButton.text = "Submit Application"
            }
        }
    }

    private fun showOtpVerificationDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_otp, null)
        val otpInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.otpInput)
        val resendText = dialogView.findViewById<android.widget.TextView>(R.id.resendCodeText)
        val btnAction = dialogView.findViewById<android.widget.Button>(R.id.btnAction)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)

        resendText.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    try {
                        val response = ApiClient.apiService.sendVerificationOtp(email)
                        if (response.isSuccessful && response.body()?.success == true) {
                            Toast.makeText(this@RegistrationActivity, "OTP Resent!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@RegistrationActivity, "Failed to resend OTP", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RegistrationActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        dialog.apply {
           window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        btnAction.setOnClickListener {
            val otp = otpInput.text.toString().trim()
            if (otp.length == 6) {
                dialog.dismiss()
                completeRegistration(otp)
            } else {
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            nextButton.isEnabled = true
            nextButton.text = "Submit Application"
        }
        
        dialog.show()
    }

    private fun completeRegistration(otp: String) {
        nextButton.text = "Creating Account..."
        
        // Prepare data
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val fullName = "${firstNameInput.text} ${lastNameInput.text}"
        val phone = mobileInput.text.toString()
        val address = addressInput.text.toString()
        val dob = dobInput.text.toString()
        
        // Get employment status (occupation)
        val occupation = employmentStatusDropdown.text.toString().ifEmpty { "Employed" }
        
        // Get monthly income if employed
        val monthlyIncome = if (dynamicFieldsLayout.visibility == View.VISIBLE) {
            incomeInput.text.toString().toDoubleOrNull() ?: 0.0
        } else {
            0.0
        }
        
        // Make API call
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.register(
                    email = email,
                    password = password,
                    fullName = fullName,
                    phone = phone,
                    address = address,
                    dateOfBirth = dob,
                    occupation = occupation,
                    monthlyIncome = monthlyIncome,
                    otp = otp
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(
                        this@RegistrationActivity,
                        "Registration Successful! Please login.",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    val intent = Intent(this@RegistrationActivity, LoginActivity::class.java)
                    intent.putExtra("email", email) // Pre-fill email
                    startActivity(intent)
                    finish()
                } else {
                    val errorMessage = response.body()?.message ?: "Registration failed"
                    Toast.makeText(this@RegistrationActivity, errorMessage, Toast.LENGTH_LONG).show()
                    nextButton.isEnabled = true
                    nextButton.text = "Submit Application"
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@RegistrationActivity,
                    "Network error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                e.printStackTrace()
                nextButton.isEnabled = true
                nextButton.text = "Submit Application"
            }
        }
    }
    private fun showTermsDialog(onAccepted: () -> Unit) {
        val dialog = android.app.AlertDialog.Builder(this)
        val view = layoutInflater.inflate(R.layout.dialog_terms_scroll, null)
        dialog.setView(view)
        
        val alertDialog = dialog.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val scrollView = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.termsScrollView)
        val btnAccept = view.findViewById<Button>(R.id.btnAcceptTerms)
        
        // Scroll Listener
        scrollView.setOnScrollChangeListener(androidx.core.widget.NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val height = v.getChildAt(0).measuredHeight
            val diff = height - v.height
            
            if (scrollY >= diff - 20) {
                if (!btnAccept.isEnabled) {
                    btnAccept.isEnabled = true
                    btnAccept.alpha = 1.0f
                }
            }
        })
        
        btnAccept.setOnClickListener {
            alertDialog.dismiss()
            onAccepted()
        }
        
        alertDialog.show()
    }
}
