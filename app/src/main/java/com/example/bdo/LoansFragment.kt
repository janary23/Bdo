package com.example.bdo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class LoansFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_loans, container, false)
        
        recyclerView = view.findViewById(R.id.loansRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Load loans from API
        loadLoans()
        
        return view
    }
    
    private fun loadLoans() {
        val userId = SessionManager.getUserId(requireContext())
        
        if (userId == 0) {
            Toast.makeText(context, "Please login to view your loans", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getUserLoans(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loans = response.body()?.loans ?: emptyList()
                    
                    if (loans.isEmpty()) {
                        Toast.makeText(context, "No active loans yet", Toast.LENGTH_SHORT).show()
                        recyclerView.visibility = View.GONE
                    } else {
                        recyclerView.visibility = View.VISIBLE
                        
                        // Convert API Loan to UI Loan model
                        val uiLoans = loans.map { apiLoan ->
                            LoanUI(
                                id = apiLoan.loan_id.toString(),
                                type = apiLoan.loan_type,
                                status = apiLoan.status,
                                amount = apiLoan.principal_amount,
                                term = apiLoan.term_months,
                                monthlyPayment = apiLoan.monthly_payment,
                                remaining = apiLoan.remaining_balance,
                                progress = apiLoan.progress ?: 0
                            )
                        }
                        
                        recyclerView.adapter = LoansAdapter(uiLoans) { loan ->
                            Toast.makeText(context, "Pay feature for ${loan.id} coming soon!", Toast.LENGTH_SHORT).show()
                        }
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
    
    // UI Data Model
    data class LoanUI(
        val id: String,
        val type: String,
        val status: String,
        val amount: Double,
        val term: Int,
        val monthlyPayment: Double,
        val remaining: Double,
        val progress: Int
    )
    
    // Adapter
    class LoansAdapter(
        private val loans: List<LoanUI>,
        private val onPayClick: (LoanUI) -> Unit
    ) : RecyclerView.Adapter<LoansAdapter.LoanViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_loan, parent, false)
            return LoanViewHolder(view)
        }

        override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
            holder.bind(loans[position], onPayClick)
        }

        override fun getItemCount() = loans.size

        class LoanViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val loanType: TextView = itemView.findViewById(R.id.loanType)
            private val loanId: TextView = itemView.findViewById(R.id.loanId)
            private val loanStatus: TextView = itemView.findViewById(R.id.loanStatus)
            private val loanAmount: TextView = itemView.findViewById(R.id.loanAmount)
            private val loanTerm: TextView = itemView.findViewById(R.id.loanTerm)
            private val monthlyPayment: TextView = itemView.findViewById(R.id.monthlyPayment)
            private val loanBalance: TextView = itemView.findViewById(R.id.loanBalance)
            private val percentPaid: TextView = itemView.findViewById(R.id.percentPaid)
            private val progressBar: ProgressBar = itemView.findViewById(R.id.loanProgress)
            private val btnPay: Button = itemView.findViewById(R.id.btnPay)
            private val btnSchedule: Button = itemView.findViewById(R.id.btnSchedule)

            fun bind(loan: LoanUI, onPayClick: (LoanUI) -> Unit) {
                val formatCurrency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
                
                loanType.text = loan.type
                loanId.text = "Loan #${loan.id}"
                loanStatus.text = loan.status
                
                loanAmount.text = formatCurrency.format(loan.amount)
                loanTerm.text = "${loan.term} months"
                monthlyPayment.text = formatCurrency.format(loan.monthlyPayment)
                loanBalance.text = formatCurrency.format(loan.remaining)
                
                progressBar.progress = loan.progress
                percentPaid.text = "${loan.progress}% paid"

                // Style Status
                if (loan.status == "Active") {
                    loanStatus.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7")) // Green-100
                    loanStatus.setTextColor(android.graphics.Color.parseColor("#166534")) // Green-800
                    btnPay.isEnabled = true
                    btnPay.alpha = 1.0f
                } else {
                    loanStatus.setBackgroundColor(android.graphics.Color.parseColor("#F3F4F6")) // Gray-100
                    loanStatus.setTextColor(android.graphics.Color.parseColor("#374151")) // Gray-700
                    btnPay.isEnabled = false
                    btnPay.alpha = 0.5f
                }

                btnPay.setOnClickListener { onPayClick(loan) }
                btnSchedule.setOnClickListener { 
                    Toast.makeText(itemView.context, "Schedule view coming soon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
