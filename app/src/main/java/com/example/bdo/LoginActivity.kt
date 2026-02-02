package com.example.bdo

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var registerLink: TextView
    private lateinit var rememberMeCheckBox: CheckBox
    private lateinit var forgotPasswordText: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if already logged in
        if (SessionManager.isLoggedIn(this)) {
            navigateToDashboard()
            return
        }
        
        setContentView(R.layout.activity_login)

        // Initialize Views
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerLink = findViewById(R.id.registerLink)
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        
        // Create ProgressBar programmatically if not in layout
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        // Set Click Listeners
        loginButton.setOnClickListener { attemptLogin() }

        registerLink.setOnClickListener {
            // Navigate to Registration Activity
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }
    }
    
    // ============================================
    // FORGOT PASSWORD FLOW
    // ============================================
    
    private fun showForgotPasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_email, null)
        val emailInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.emailInput)
        val btnAction = dialogView.findViewById<android.widget.Button>(R.id.btnAction)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
            
        dialog.apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        btnAction.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                sendResetOtp(email)
            } else {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun sendResetOtp(email: String) {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Sending OTP...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.sendResetOtp(email)
                progressDialog.dismiss()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@LoginActivity, "OTP Sent!", Toast.LENGTH_SHORT).show()
                    showOtpDialog(email)
                } else {
                    val msg = response.body()?.message ?: "Failed to send OTP"
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showOtpDialog(email: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input_otp, null)
        val otpInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.otpInput)
        val resendText = dialogView.findViewById<TextView>(R.id.resendCodeText)
        val btnAction = dialogView.findViewById<android.widget.Button>(R.id.btnAction)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)

        // Customize message for Reset Password context
        val messageText = dialogView.findViewById<TextView>(R.id.dialogMessage)
        messageText.text = "Enter the code sent to $email to reset your password."

        resendText.setOnClickListener {
             Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show()
             lifecycleScope.launch {
                 ApiClient.apiService.sendResetOtp(email)
                 Toast.makeText(this@LoginActivity, "OTP Resent!", Toast.LENGTH_SHORT).show()
             }
        }
        
        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        btnAction.setOnClickListener {
            val otp = otpInput.text.toString().trim()
            if (otp.length == 6) {
                dialog.dismiss()
                verifyOtp(email, otp)
            } else {
                Toast.makeText(this, "Please enter valid 6-digit OTP", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun verifyOtp(email: String, otp: String) {
        val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Verifying OTP...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.verifyResetOtp(email, otp)
                progressDialog.dismiss()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(this@LoginActivity, "OTP Verified!", Toast.LENGTH_SHORT).show()
                    showNewPasswordDialog(email, otp)
                } else {
                    val msg = response.body()?.message ?: "Invalid OTP"
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showNewPasswordDialog(email: String, otp: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_reset_password, null)
        val newPassInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.newPasswordInput)
        val confirmPassInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.confirmNewPasswordInput)
        val btnAction = dialogView.findViewById<android.widget.Button>(R.id.btnAction)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)

        val dialog = android.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.apply {
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        btnAction.setOnClickListener {
            val newPass = newPassInput.text.toString().trim()
            val confirmPass = confirmPassInput.text.toString().trim()
            
            if (newPass.isEmpty()) {
                 Toast.makeText(this, "Password required", Toast.LENGTH_SHORT).show()
                 return@setOnClickListener
            }
            
            if (newPass != confirmPass) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPass.length >= 6) {
                dialog.dismiss()
                resetPassword(email, otp, newPass)
            } else {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun resetPassword(email: String, otp: String, newPass: String) {
         val progressDialog = android.app.ProgressDialog(this).apply {
            setMessage("Updating Password...")
            setCancelable(false)
            show()
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.resetPassword(email, otp, newPass)
                progressDialog.dismiss()
                
                if (response.isSuccessful && response.body()?.success == true) {
                    android.app.AlertDialog.Builder(this@LoginActivity)
                        .setTitle("Success")
                        .setMessage("Your password has been reset successfully! You can now login.")
                        .setPositiveButton("OK", null)
                        .show()
                } else {
                    val msg = response.body()?.message ?: "Failed to reset password"
                    Toast.makeText(this@LoginActivity, msg, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                progressDialog.dismiss()
                Toast.makeText(this@LoginActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun attemptLogin() {
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // Basic Validation
        if (TextUtils.isEmpty(email)) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return
        }

        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            return
        }

        // Show loading
        loginButton.isEnabled = false
        loginButton.text = "Logging in..."

        // Make API call
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.login(email, password)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    
                    if (user != null) {
                        // Save user session
                        SessionManager.saveUserSession(this@LoginActivity, user)
                        
                        Toast.makeText(
                            this@LoginActivity,
                            "Welcome, ${user.full_name}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        
                        // Navigate to Dashboard
                        navigateToDashboard()
                    } else {
                        showError("Login failed: Invalid response")
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Login failed"
                    showError(errorMessage)
                }
            } catch (e: Exception) {
                showError("Network error: ${e.message}")
                e.printStackTrace()
            } finally {
                loginButton.isEnabled = true
                loginButton.text = "Login"
            }
        }
    }
    
    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
    
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
