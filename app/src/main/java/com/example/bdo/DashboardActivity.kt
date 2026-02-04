package com.example.bdo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)



        // Initialize custom navigation
        setupNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
            updateNavigationUI(R.id.nav_dashboard)
        }
        
        // Check for welcome walkthrough
        checkWalkthroughStatus()
    }

    private fun setupNavigation() {
        val navItems = listOf(
            R.id.nav_dashboard,
            R.id.nav_applications,
            R.id.nav_payments,
            R.id.nav_requirements,
            R.id.nav_appointments,
            R.id.nav_profile
        )

        for (id in navItems) {
            findViewById<android.view.View>(id).setOnClickListener {
                handleNavigation(id)
            }
        }
    }

    private fun handleNavigation(id: Int) {
        when (id) {
            R.id.nav_dashboard -> {
                loadFragment(DashboardFragment())

            }
            R.id.nav_applications -> {
                loadFragment(ApplyFragment())

            }
            R.id.nav_payments -> {
                loadFragment(PaymentsFragment())

            }
            R.id.nav_requirements -> {
                loadFragment(RequirementsFragment())

            }
            R.id.nav_appointments -> {
                loadFragment(AppointmentsFragment())

            }
            R.id.nav_profile -> {
                loadFragment(ProfileFragment())

            }
        }
        updateNavigationUI(id)
    }

    private fun updateNavigationUI(activeId: Int) {
        val navMap = mapOf(
            R.id.nav_dashboard to Pair(R.id.img_dashboard, R.id.txt_dashboard),
            R.id.nav_applications to Pair(R.id.img_applications, R.id.txt_applications),
            R.id.nav_payments to Pair(R.id.img_payments, R.id.txt_payments),
            R.id.nav_requirements to Pair(R.id.img_requirements, R.id.txt_requirements),
            R.id.nav_appointments to Pair(R.id.img_appointments, R.id.txt_appointments),
            R.id.nav_profile to Pair(R.id.img_profile, R.id.txt_profile)
        )

        val activeColor = androidx.core.content.ContextCompat.getColor(this, R.color.blue_900)
        val inactiveColor = androidx.core.content.ContextCompat.getColor(this, R.color.gray_600)

        for ((navId, views) in navMap) {
            val (imgId, txtId) = views
            val img = findViewById<android.widget.ImageView>(imgId)
            val txt = findViewById<android.widget.TextView>(txtId)
            
            if (navId == activeId) {
                img.setColorFilter(activeColor)
                txt.setTextColor(activeColor)
            } else {
                img.setColorFilter(inactiveColor)
                txt.setTextColor(inactiveColor)
            }
        }
    }

    fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, fragment)
        transaction.commit()
    }
    
    private fun checkWalkthroughStatus() {
        val userId = SessionManager.getUserId(this)
        android.util.Log.d("WalkthroughDebug", "=== CHECKING WALKTHROUGH STATUS ===")
        android.util.Log.d("WalkthroughDebug", "User ID: $userId")
        
        if (userId == 0) {
            android.util.Log.d("WalkthroughDebug", "User ID is 0, returning early")
            return
        }
        
        lifecycleScope.launch {
            try {
                android.util.Log.d("WalkthroughDebug", "Making API call to getUserProfile...")
                val response = ApiClient.apiService.getUserProfile(userId)
                
                android.util.Log.d("WalkthroughDebug", "API Response Code: ${response.code()}")
                android.util.Log.d("WalkthroughDebug", "API Response Successful: ${response.isSuccessful}")
                android.util.Log.d("WalkthroughDebug", "Response Body Success: ${response.body()?.success}")
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    android.util.Log.d("WalkthroughDebug", "User object: $user")
                    android.util.Log.d("WalkthroughDebug", "hasSeenWalkthrough value: ${user?.hasSeenWalkthrough}")
                    android.util.Log.d("WalkthroughDebug", "hasSeenWalkthrough type: ${user?.hasSeenWalkthrough?.javaClass?.simpleName}")
                    
                    // Check if user has NOT seen walkthrough (0 or null)
                    if (user?.hasSeenWalkthrough == 0 || user?.hasSeenWalkthrough == null) {
                        android.util.Log.d("WalkthroughDebug", "✓ SHOWING WALKTHROUGH DIALOG")
                        try {
                            if (!supportFragmentManager.isStateSaved) {
                                WelcomeWalkthroughDialog().show(
                                    supportFragmentManager, 
                                    WelcomeWalkthroughDialog.TAG
                                )
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("WalkthroughDebug", "Failed to show dialog", e)
                        }
                    } else {
                        android.util.Log.d("WalkthroughDebug", "✗ NOT showing walkthrough (user has seen it)")
                    }
                } else {
                    android.util.Log.e("WalkthroughDebug", "API call failed or success=false")
                    android.util.Log.e("WalkthroughDebug", "Response body: ${response.body()}")
                }
            } catch (e: Exception) {
                android.util.Log.e("WalkthroughDebug", "Exception in checkWalkthroughStatus", e)
                e.printStackTrace()
            }
        }
    }
}
