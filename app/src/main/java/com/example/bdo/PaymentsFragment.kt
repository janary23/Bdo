package com.example.bdo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.android.material.bottomsheet.BottomSheetDialog

class PaymentsFragment : Fragment() {

    private lateinit var activeLoansRecycler: RecyclerView
    private lateinit var paymentHistoryRecycler: RecyclerView
    private lateinit var emptyStateActiveLoans: View
    private lateinit var emptyStatePayments: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payments, container, false)

        activeLoansRecycler = view.findViewById(R.id.activeLoansRecycler)
        paymentHistoryRecycler = view.findViewById(R.id.paymentHistoryRecycler)
        emptyStateActiveLoans = view.findViewById(R.id.emptyStateActiveLoans)
        emptyStatePayments = view.findViewById(R.id.emptyStatePayments)

        loadActiveLoans()
        loadPaymentHistory()

        return view
    }

    private fun loadActiveLoans() {
        val userId = SessionManager.getUserId(requireContext())
        
        if (userId == 0) {
            Toast.makeText(context, "Please login to view loans", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getUserLoans(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loans = response.body()?.loans ?: emptyList()
                    val activeLoansList = loans.filter { it.status == "Active" } // Only show active
                    
                    if (activeLoansList.isEmpty()) {
                        activeLoansRecycler.visibility = View.GONE
                        emptyStateActiveLoans.visibility = View.VISIBLE
                        
                        val tvTitle = emptyStateActiveLoans.findViewById<TextView>(R.id.tvEmptyTitle)
                        val tvSubtitle = emptyStateActiveLoans.findViewById<TextView>(R.id.tvEmptySubtitle)
                        tvTitle.text = "No Active Loans"
                        tvSubtitle.text = "You don't have any active loans at the moment."
                    } else {
                        activeLoansRecycler.visibility = View.VISIBLE
                        emptyStateActiveLoans.visibility = View.GONE
                        
                        // Convert to ActiveLoan format
                        val activeLoans = activeLoansList.map { loan ->
                            val formatCurrency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                            val progress = loan.progress ?: 0
                            
                            ActiveLoan(
                                loanId = loan.loan_id,
                                userId = userId,
                                type = loan.loan_type,
                                idDisplay = "Loan #${loan.loan_id}",
                                amount = formatCurrency.format(loan.principal_amount),
                                rate = "5.00%", // Default rate
                                monthly = formatCurrency.format(loan.monthly_payment),
                                due = loan.next_due_date ?: "Due soon", 
                                balance = formatCurrency.format(loan.remaining_balance),
                                progress = progress,
                                rawMonthly = loan.monthly_payment
                            )
                        }
                        
                        activeLoansRecycler.layoutManager = LinearLayoutManager(context)
                        activeLoansRecycler.adapter = ActiveLoanAdapter(activeLoans)
                    }
                } else {
                    activeLoansRecycler.visibility = View.GONE
                    emptyStateActiveLoans.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                // Keep empty state visible on error
                 activeLoansRecycler.visibility = View.GONE
                 emptyStateActiveLoans.visibility = View.VISIBLE
            }
        }
    }

    private fun loadPaymentHistory() {
        val userId = SessionManager.getUserId(requireContext())
        
        if (userId == 0) {
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getUserPayments(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val payments = response.body()?.payments ?: emptyList()
                    
                    if (payments.isEmpty()) {
                        paymentHistoryRecycler.visibility = View.GONE
                        emptyStatePayments.visibility = View.VISIBLE
                        
                        val tvTitle = emptyStatePayments.findViewById<TextView>(R.id.tvEmptyTitle)
                        val tvSubtitle = emptyStatePayments.findViewById<TextView>(R.id.tvEmptySubtitle)
                        tvTitle.text = "No Payment History"
                        tvSubtitle.text = "Your recent payments will appear here."
                    } else {
                        paymentHistoryRecycler.visibility = View.VISIBLE
                        emptyStatePayments.visibility = View.GONE
                        
                        // Convert to PaymentItem format
                        val formatCurrency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                        
                        val paymentItems = payments.map { payment ->
                            PaymentItem(
                                id = payment.payment_id,
                                type = payment.loan_type ?: "Loan Payment",
                                date = try {
                                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(payment.payment_date)
                                    sdf.format(date!!)
                                } catch (e: Exception) {
                                    payment.payment_date
                                },
                                amount = formatCurrency.format(payment.amount),
                                status = payment.status,
                                orNumber = payment.orNumber
                            )
                        }
                        
                        paymentHistoryRecycler.layoutManager = LinearLayoutManager(context)
                        paymentHistoryRecycler.adapter = PaymentHistoryAdapter(paymentItems)
                    }
                } else {
                   paymentHistoryRecycler.visibility = View.GONE
                   emptyStatePayments.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
               paymentHistoryRecycler.visibility = View.GONE
               emptyStatePayments.visibility = View.VISIBLE
            }
        }
    }

    // Data Classes
    data class ActiveLoan(
        val loanId: Int,
        val userId: Int,
        val type: String, 
        val idDisplay: String, 
        val amount: String, 
        val rate: String, 
        val monthly: String, 
        val due: String, 
        val balance: String, 
        val progress: Int,
        val rawMonthly: Double 
    )
    
    data class PaymentItem(val id: Int, val type: String, val date: String, val amount: String, val status: String, val orNumber: String?)

    // Adapters
    inner class ActiveLoanAdapter(private val items: List<ActiveLoan>) : RecyclerView.Adapter<ActiveLoanAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val type: TextView = view.findViewById(R.id.loanType)
            val id: TextView = view.findViewById(R.id.loanId)
            val amount: TextView = view.findViewById(R.id.loanAmount)
            val rate: TextView = view.findViewById(R.id.loanRate)
            val monthly: TextView = view.findViewById(R.id.loanMonthly)
            val due: TextView = view.findViewById(R.id.loanDueDate)
            val balance: TextView = view.findViewById(R.id.loanBalance)
            val progress: ProgressBar = view.findViewById(R.id.loanProgress)
            val percent: TextView = view.findViewById(R.id.loanPercent)
            val btnPay: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnPayNow)
            val btnSchedule: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnSchedule)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_active_loan, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.type.text = item.type
            holder.id.text = item.idDisplay
            holder.amount.text = item.amount
            holder.rate.text = item.rate
            holder.monthly.text = item.monthly
            holder.due.text = item.due
            holder.balance.text = item.balance
            holder.progress.progress = item.progress
            holder.percent.text = "${item.progress}% paid"
            
            holder.btnPay.setOnClickListener {
                showPaymentDialog(item)
            }
            
            holder.btnSchedule.setOnClickListener {
                showScheduleDialog(item)
            }
        }

        override fun getItemCount() = items.size
    }

    private fun showPaymentDialog(loan: ActiveLoan) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_payment_method_modern, null)
        
        // Find views
        val payBillName = dialogView.findViewById<TextView>(R.id.payBillName)
        val payAmount = dialogView.findViewById<TextView>(R.id.payAmount)
        val payDueDate = dialogView.findViewById<TextView>(R.id.payDueDate)
        val radioGroup = dialogView.findViewById<android.widget.RadioGroup>(R.id.radioGroupPayment)
        val btnClose = dialogView.findViewById<View>(R.id.btnClosePayment)
        val btnProceed = dialogView.findViewById<android.widget.Button>(R.id.btnProceedPay)
        
        // Set data
        payBillName.text = "${loan.type} #${loan.loanId}"
        payAmount.text = loan.monthly
        payDueDate.text = "Due on ${loan.due}"
        
        // Use BottomSheetDialog for modern feel
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        dialog.behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED

        btnClose.setOnClickListener {
            dialog.dismiss()
        }

        btnProceed.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(context, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val selectedRb = dialogView.findViewById<android.widget.RadioButton>(selectedId)
            val method = selectedRb.text.toString()
            
            dialog.dismiss()
            
            // Proceed to Confirmation
            showPaymentConfirmationDialog(loan, method)
        }
        
        dialog.show()
    }
    
    private fun showPaymentConfirmationDialog(loan: ActiveLoan, method: String) {
        val context = context ?: return
        
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_payment_confirmation, null)
        val confirmAmount = dialogView.findViewById<TextView>(R.id.confirmAmount)
        val confirmBillName = dialogView.findViewById<TextView>(R.id.confirmBillName)
        val confirmMethod = dialogView.findViewById<TextView>(R.id.confirmMethod)
        val btnConfirm = dialogView.findViewById<android.widget.Button>(R.id.btnConfirmPayment)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancelPayment)
        
        confirmAmount.text = loan.monthly
        confirmBillName.text = "${loan.type} #${loan.loanId}"
        confirmMethod.text = method
        
        val dialog = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        btnConfirm.setOnClickListener {
            dialog.dismiss()
            processPayment(loan, method)
        }
        
        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun processPayment(loan: ActiveLoan, method: String) {
        lifecycleScope.launch {
            try {
                // Generate random reference number
                val ref = "REF-${System.currentTimeMillis()}"
                
                val response = ApiClient.apiService.processPayment(
                    userId = loan.userId,
                    loanId = loan.loanId,
                    amount = loan.rawMonthly,
                    method = method,
                    ref = ref
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, response.body()?.message ?: "Payment successful!", Toast.LENGTH_LONG).show()
                    loadPaymentHistory() // Refresh history
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Payment failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showScheduleDialog(loan: ActiveLoan) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loan_schedule, null)
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        
        val recycler = dialogView.findViewById<RecyclerView>(R.id.recyclerSchedule)
        val btnClose = dialogView.findViewById<View>(R.id.btnCloseSchedule)
        
        recycler.layoutManager = LinearLayoutManager(context)
        
        // Show loading state or fetch data
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getLoanSchedule(loan.loanId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val schedule = response.body()?.schedule ?: emptyList()
                    recycler.adapter = ScheduleAdapter(schedule)
                } else {
                    Toast.makeText(context, response.body()?.message ?: "Failed to load schedule", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun scheduleAppointment(loan: ActiveLoan, date: String, time: String) {
        // Deprecated or kept for future use
    }

    private fun showWebViewDialog(url: String, title: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_webview, null)
        
        // Find views
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvWebViewTitle)
        val btnClose = dialogView.findViewById<View>(R.id.btnCloseWebView)
        val webView = dialogView.findViewById<android.webkit.WebView>(R.id.webview)
        val progressBar = dialogView.findViewById<ProgressBar>(R.id.webViewProgressBar)
        
        tvTitle.text = title
        
        // Configure WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.builtInZoomControls = true
        webView.settings.displayZoomControls = false
        
        // Handle page loading
        webView.webViewClient = object : android.webkit.WebViewClient() {
            override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                super.onPageStarted(view, url, favicon)
                progressBar.visibility = View.VISIBLE
            }
            
            override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = View.GONE
            }
            
            override fun shouldOverrideUrlLoading(view: android.webkit.WebView?, request: android.webkit.WebResourceRequest?): Boolean {
                return false // Keep links inside WebView
            }
        }
        
        webView.loadUrl(url)
        
        // Setup Dialog
        val dialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireContext())
        dialog.setContentView(dialogView)
        
        // Make it full height
        val parentLayout = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        parentLayout?.let { bottomSheet ->
            val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
            behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
            behavior.skipCollapsed = true
            
            // Set height to match parent
            val layoutParams = bottomSheet.layoutParams
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
            bottomSheet.layoutParams = layoutParams
        }
        
        btnClose.setOnClickListener {
            dialog.dismiss()
        }
        
        dialog.show()
    }

    inner class PaymentHistoryAdapter(private val items: List<PaymentItem>) : RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val type: TextView = view.findViewById(R.id.payType)
            val date: TextView = view.findViewById(R.id.payDate)
            val amount: TextView = view.findViewById(R.id.payAmount)
            val status: TextView = view.findViewById(R.id.payStatus)
            val or: TextView? = view.findViewById(R.id.payOr)
            val btnReceipt: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnViewReceipt)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            holder.type.text = item.type
            holder.date.text = item.date
            holder.amount.text = item.amount
            holder.status.text = item.status.uppercase()
            
            if (!item.orNumber.isNullOrEmpty()) {
                holder.or?.text = "OR: ${item.orNumber}"
                holder.or?.visibility = View.VISIBLE
            } else {
                holder.or?.visibility = View.GONE
            }
            
            // Show Receipt Button if Verified and has OR
            if (item.status.equals("verified", ignoreCase = true) && !item.orNumber.isNullOrEmpty()) {
                holder.btnReceipt.visibility = View.VISIBLE
                holder.btnReceipt.setOnClickListener {
                     val url = "http://10.0.2.2/bdo/admin/receipt.php?payment_id=${item.id}"
                     showWebViewDialog(url, "Payment Receipt")
                }
            } else {
                holder.btnReceipt.visibility = View.GONE
            }
        }

        override fun getItemCount() = items.size
    }
    
    inner class ScheduleAdapter(private val items: List<ScheduleItem>) : RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val date: TextView = view.findViewById(R.id.schedDate)
            val install: TextView = view.findViewById(R.id.schedInstallment)
            val amount: TextView = view.findViewById(R.id.schedAmount)
            val status: TextView = view.findViewById(R.id.schedStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            val formatCurrency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
            
            holder.date.text = item.due_date
            holder.install.text = "Installment #${item.installment_no}"
            holder.amount.text = formatCurrency.format(item.amount)
            holder.status.text = item.status
            
            // Status Styling
            when (item.status) {
                "PAID" -> {
                    holder.status.setTextColor(resources.getColor(R.color.green_700, null))
                    holder.status.background.setTint(resources.getColor(R.color.green_50, null))
                }
                "OVERDUE" -> {
                    holder.status.setTextColor(resources.getColor(R.color.red_700, null))
                    holder.status.background.setTint(resources.getColor(R.color.red_50, null))
                }
                else -> { // UPCOMING
                    holder.status.setTextColor(resources.getColor(R.color.gray_700, null))
                    holder.status.background.setTint(resources.getColor(R.color.gray_200, null))
                }
            }
        }

        override fun getItemCount() = items.size
    }
}
