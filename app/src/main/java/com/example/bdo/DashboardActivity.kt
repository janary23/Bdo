package com.example.bdo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dashboard"

        // Initialize custom navigation
        setupNavigation()

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(DashboardFragment())
            updateNavigationUI(R.id.nav_dashboard)
        }
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
                supportActionBar?.title = "Dashboard"
            }
            R.id.nav_applications -> {
                loadFragment(ApplyFragment())
                supportActionBar?.title = "Apply Loan"
            }
            R.id.nav_payments -> {
                loadFragment(PaymentsFragment())
                supportActionBar?.title = "Payments"
            }
            R.id.nav_requirements -> {
                loadFragment(RequirementsFragment())
                supportActionBar?.title = "Requirements"
            }
            R.id.nav_appointments -> {
                loadFragment(AppointmentsFragment())
                supportActionBar?.title = "Appointments"
            }
            R.id.nav_profile -> {
                loadFragment(ProfileFragment())
                supportActionBar?.title = "Profile"
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
}
