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
    
    // RecyclerViews & Adapters
    private lateinit var recentAppsRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var upcomingApptsRecyclerView: androidx.recyclerview.widget.RecyclerView
    private lateinit var recentAppsAdapter: RecentAppsAdapter
    private lateinit var appointmentsAdapter: DashboardAppointmentsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        initializeViews(view)
        setupRecyclerViews(view)
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

        tvManageAppts?.setOnClickListener {
            (activity as? DashboardActivity)?.loadFragment(AppointmentsFragment())
        }
        
    }
    
    private fun setupRecyclerViews(view: View) {
        // Recent Apps
        recentAppsRecyclerView = view.findViewById(R.id.recentAppsRecyclerView)
        recentAppsAdapter = RecentAppsAdapter(emptyList())
        recentAppsRecyclerView.adapter = recentAppsAdapter
        
        // Upcoming Appointments
        upcomingApptsRecyclerView = view.findViewById(R.id.upcomingApptsRecyclerView)
        appointmentsAdapter = DashboardAppointmentsAdapter(emptyList())
        upcomingApptsRecyclerView.adapter = appointmentsAdapter
    }

    private fun loadDashboardStats() {
        val userId = SessionManager.getUserId(requireContext())
        
        if (userId == 0) {
            Toast.makeText(context, "Please login to view dashboard", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                // 1. Dashboard Stats
                val statsResponse = ApiClient.apiService.getDashboardStats(userId)
                if (statsResponse.isSuccessful && statsResponse.body()?.success == true) {
                    val stats = statsResponse.body()?.stats
                    if (stats != null) {
                        populateStats(stats)
                    }
                }
                
                // 2. Recent Applications (Latest 3)
                val appsResponse = ApiClient.apiService.getUserApplications(userId)
                if (appsResponse.isSuccessful && appsResponse.body()?.success == true) {
                    val allApps = appsResponse.body()?.applications ?: emptyList()
                    val recentApps = allApps.take(3)
                    
                    if (recentApps.isNotEmpty()) {
                        recentAppsAdapter.updateData(recentApps)
                        requireView().findViewById<View>(R.id.recentAppsRecyclerView).visibility = View.VISIBLE
                        requireView().findViewById<View>(R.id.tvNoRecentApps).visibility = View.GONE
                    } else {
                        requireView().findViewById<View>(R.id.recentAppsRecyclerView).visibility = View.GONE
                        requireView().findViewById<View>(R.id.tvNoRecentApps).visibility = View.VISIBLE
                    }
                }

                // 3. Upcoming Appointments (Latest 3)
                val apptsResponse = ApiClient.apiService.getAppointments(userId)
                if (apptsResponse.isSuccessful && apptsResponse.body()?.success == true) {
                    val allAppts = apptsResponse.body()?.appointments ?: emptyList()
                    // Filter for future/pending if needed, for now just take 3
                    val upcoming = allAppts.take(3)
                    
                    if (upcoming.isNotEmpty()) {
                        appointmentsAdapter.updateData(upcoming)
                        requireView().findViewById<View>(R.id.upcomingApptsRecyclerView).visibility = View.VISIBLE
                        requireView().findViewById<View>(R.id.tvNoAppts).visibility = View.GONE
                    } else {
                        requireView().findViewById<View>(R.id.upcomingApptsRecyclerView).visibility = View.GONE
                        requireView().findViewById<View>(R.id.tvNoAppts).visibility = View.VISIBLE
                    }
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

