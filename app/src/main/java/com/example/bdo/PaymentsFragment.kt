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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payments, container, false)

        activeLoansRecycler = view.findViewById(R.id.activeLoansRecycler)
        paymentHistoryRecycler = view.findViewById(R.id.paymentHistoryRecycler)

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
                    
                    if (loans.isEmpty()) {
                        activeLoansRecycler.visibility = View.GONE
                    } else {
                        activeLoansRecycler.visibility = View.VISIBLE
                        
                        // Convert to ActiveLoan format
                        val activeLoans = loans.filter { it.status == "Active" }.map { loan ->
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
                    Toast.makeText(context, "Failed to load loans", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
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
                    } else {
                        paymentHistoryRecycler.visibility = View.VISIBLE
                        
                        // Convert to PaymentItem format
                        val formatCurrency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
                        
                        val paymentItems = payments.map { payment ->
                            PaymentItem(
                                type = payment.loan_type ?: "Loan Payment",
                                date = try {
                                    val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).parse(payment.payment_date)
                                    sdf.format(date!!)
                                } catch (e: Exception) {
                                    payment.payment_date
                                },
                                amount = formatCurrency.format(payment.amount),
                                status = payment.status
                            )
                        }
                        
                        paymentHistoryRecycler.layoutManager = LinearLayoutManager(context)
                        paymentHistoryRecycler.adapter = PaymentHistoryAdapter(paymentItems)
                    }
                } else {
                    Toast.makeText(context, "Failed to load payment history", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
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
    
    data class PaymentItem(val type: String, val date: String, val amount: String, val status: String)

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
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_payment_method, null)
        
        // Find views
        val textAmount = dialogView.findViewById<TextView>(R.id.textAmount)
        val radioGroup = dialogView.findViewById<android.widget.RadioGroup>(R.id.radioGroupPayment)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<android.widget.Button>(R.id.btnConfirm)
        
        // Set data
        textAmount.text = "Amount: ${loan.monthly}"
        
        val dialog = android.app.AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()
            
        dialog.apply {
           window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        btnConfirm.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            if (selectedId == -1) {
                Toast.makeText(context, "Please select a payment method", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val selectedRb = dialogView.findViewById<android.widget.RadioButton>(selectedId)
            val method = selectedRb.text.toString()
            
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

    inner class PaymentHistoryAdapter(private val items: List<PaymentItem>) : RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val type: TextView = view.findViewById(R.id.payType)
            val date: TextView = view.findViewById(R.id.payDate)
            val amount: TextView = view.findViewById(R.id.payAmount)
            val status: TextView = view.findViewById(R.id.payStatus)
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
