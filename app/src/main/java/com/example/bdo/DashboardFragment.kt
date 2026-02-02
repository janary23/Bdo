package com.example.bdo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardFragment : Fragment() {

    private var activeLoansCount: TextView? = null
    private var nextPaymentAmount: TextView? = null
    private var nextPaymentDate: TextView? = null
    private var loanBalanceAmount: TextView? = null
    private var pendingAppsCount: TextView? = null
    private var dateText: TextView? = null
    private var welcomeText: TextView? = null
    private var tvViewAllApps: TextView? = null
    private var tvManageAppts: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        initializeViews(view)
        loadDashboardStats()
        return view
    }

    private fun initializeViews(view: View) {
        activeLoansCount = view.findViewById(R.id.activeLoansCount)
        nextPaymentAmount = view.findViewById(R.id.nextPaymentAmount)
        nextPaymentDate = view.findViewById(R.id.nextPaymentDate)
        loanBalanceAmount = view.findViewById(R.id.loanBalanceAmount)
        pendingAppsCount = view.findViewById(R.id.pendingAppsCount)
        dateText = view.findViewById(R.id.dateText)
        welcomeText = view.findViewById(R.id.welcomeText)
        tvViewAllApps = view.findViewById(R.id.tvViewAllApps)
        tvManageAppts = view.findViewById(R.id.tvManageAppts)

        // Set Date
        val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.US)
        dateText?.text = sdf.format(Date())

        // Navigation Actions
        tvViewAllApps?.setOnClickListener {
            (activity as? DashboardActivity)?.loadFragment(LoansFragment())
        }
        
        // Item Clicks
        view.findViewById<View>(R.id.recentAppItem1)?.setOnClickListener {
            (activity as? DashboardActivity)?.loadFragment(LoansFragment())
        }
        view.findViewById<View>(R.id.recentAppItem2)?.setOnClickListener {
             (activity as? DashboardActivity)?.loadFragment(LoansFragment())
        }

        tvManageAppts?.setOnClickListener {
            (activity as? DashboardActivity)?.loadFragment(AppointmentsFragment())
        }
        
        view.findViewById<View>(R.id.apptItem1)?.setOnClickListener {
             (activity as? DashboardActivity)?.loadFragment(AppointmentsFragment())
        }
    }

    private fun loadDashboardStats() {
        val userId = SessionManager.getUserId(requireContext())
        
        if (userId == 0) {
            Toast.makeText(context, "Please login to view dashboard", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getDashboardStats(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val stats = response.body()?.stats
                    
                    if (stats != null) {
                        populateStats(stats)
                    }
                } else {
                    Toast.makeText(context, "Failed to load dashboard data", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun populateStats(stats: DashboardStats) {
        // Formatter for Currency
        val formatCurrency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))

        // Set User Name
        welcomeText?.text = "Welcome back, ${stats.user_name}!"
        
        // Active Loans
        activeLoansCount?.text = stats.active_loans_count.toString()
        
        // Next Payment
        nextPaymentAmount?.text = formatCurrency.format(stats.next_payment_amount)
        nextPaymentDate?.text = if (stats.next_payment_date != null) {
            "Due: ${stats.next_payment_date}"
        } else {
            "No upcoming payments"
        }

        // Loan Balance
        loanBalanceAmount?.text = formatCurrency.format(stats.total_balance)

        // Pending Apps
        pendingAppsCount?.text = stats.pending_applications_count.toString()
    }
}

