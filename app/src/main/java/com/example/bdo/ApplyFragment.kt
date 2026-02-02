package com.example.bdo

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ApplyFragment : Fragment() {

    private lateinit var amountSlider: Slider
    private lateinit var amountDisplay: TextView
    private lateinit var termSlider: Slider
    private lateinit var termDisplay: TextView
    private lateinit var cardPersonal: LinearLayout
    private lateinit var cardHome: LinearLayout
    private lateinit var cardMulti: LinearLayout
    private lateinit var overlayPersonal: FrameLayout
    private lateinit var overlayHome: FrameLayout
    private lateinit var overlayMulti: FrameLayout
    private lateinit var pendingWarningBanner: LinearLayout
    private lateinit var btnSubmit: Button
    
    // Credit Limit Display Views
    private lateinit var creditLimitAmount: TextView
    private lateinit var usedCreditText: TextView
    private lateinit var creditPercentageText: TextView
    private lateinit var creditLimitProgress: ProgressBar
    private lateinit var maxAmountText: TextView
    
    // Booster & Requirements
    private lateinit var boosterHeader: LinearLayout
    private lateinit var boosterContent: LinearLayout
    private lateinit var boosterChevron: ImageView
    private lateinit var reqTitle: TextView
    private lateinit var reqList: LinearLayout
    
    // Pending Applications Views
    private lateinit var pendingApplicationsCard: CardView
    private lateinit var pendingApplicationsList: LinearLayout
    private lateinit var pendingAppsCount: TextView

    private var selectedType = "Personal Loan"
    
    // Real data from API
    private var pendingLoanTypes = listOf<String>()
    
    // Credit Limit Data
    private var creditLimit = 0.0
    private var availableCredit = 0.0
    private var totalBalance = 0.0
    
    // Store full requirements objects to check status
    private var userRequirementsMap = mapOf<String, String>() // Name -> Status
    
    // Exact Requirements Logic (business logic - stays hardcoded)
    private val loanRequirements = mapOf(
        "Personal Loan" to listOf("Valid ID", "Proof of Income", "Employment Certificate", "Billing Statement"),
        "Home Loan" to listOf("Valid ID", "Proof of Income", "Bank Statement", "Tax Return", "Billing Statement"),
        "Multipurpose Loan" to listOf("Valid ID", "Proof of Income", "Bank Statement", "Billing Statement")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            val view = inflater.inflate(R.layout.fragment_apply, container, false)
            
            initializeViews(view)
            setupSliders()
            setupSubmit(view)
            setupBooster()
            
            // Load real data from API
            loadPendingApplications()
            loadUserRequirements()
            loadCreditLimit()
            
            view
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error loading Apply screen: ${e.message}", Toast.LENGTH_LONG).show()
            // Return a simple error view or empty view
            inflater.inflate(R.layout.fragment_apply, container, false)
        }
    }
    
    private fun initializeViews(view: View) {
        try {
            amountSlider = view.findViewById(R.id.amountSlider)
            amountDisplay = view.findViewById(R.id.amountDisplay)
            termSlider = view.findViewById(R.id.termSlider)
            termDisplay = view.findViewById(R.id.termDisplay)
            
            // Cards
            cardPersonal = view.findViewById(R.id.cardPersonal)
            cardHome = view.findViewById(R.id.cardHome)
            cardMulti = view.findViewById(R.id.cardMulti)
            
            // Overlays
            overlayPersonal = view.findViewById(R.id.overlayPersonal)
            overlayHome = view.findViewById(R.id.overlayHome)
            overlayMulti = view.findViewById(R.id.overlayMulti)
            
            pendingWarningBanner = view.findViewById(R.id.pendingWarningBanner)
            btnSubmit = view.findViewById(R.id.btnSubmitApplication)
            
            // Credit Limit Display
            creditLimitAmount = view.findViewById(R.id.creditLimitAmount)
            usedCreditText = view.findViewById(R.id.usedCreditText)
            creditPercentageText = view.findViewById(R.id.creditPercentageText)
            creditLimitProgress = view.findViewById(R.id.creditLimitProgress)
            maxAmountText = view.findViewById(R.id.maxAmountText)
            
            // Booster
            boosterHeader = view.findViewById(R.id.boosterHeader)
            boosterContent = view.findViewById(R.id.boosterContent)
            boosterChevron = view.findViewById(R.id.boosterChevron)
            
            reqTitle = view.findViewById(R.id.reqTitle)
            reqList = view.findViewById(R.id.reqList)
            
            // Pending Applications
            pendingApplicationsCard = view.findViewById(R.id.pendingApplicationsCard)
            pendingApplicationsList = view.findViewById(R.id.pendingApplicationsList)
            pendingAppsCount = view.findViewById(R.id.pendingAppsCount)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error initializing views: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupPendingState() {
        if (pendingLoanTypes.isNotEmpty()) {
            pendingWarningBanner.visibility = View.VISIBLE
            val text = view?.findViewById<TextView>(R.id.pendingWarningText)
            text?.text = "You have pending applications or active loans for: ${pendingLoanTypes.joinToString(", ")}. You cannot apply for these loan types until they are completed or paid off."
        } else {
            // Hide the warning banner if there are no pending applications
            pendingWarningBanner.visibility = View.GONE
        }
        
        if (pendingLoanTypes.contains("Personal Loan")) {
            overlayPersonal.visibility = View.VISIBLE
            cardPersonal.alpha = 0.5f
            cardPersonal.isEnabled = false
        }
        if (pendingLoanTypes.contains("Home Loan")) {
            overlayHome.visibility = View.VISIBLE
            cardHome.alpha = 0.5f
            cardHome.isEnabled = false
        }
        if (pendingLoanTypes.contains("Multipurpose Loan")) {
            overlayMulti.visibility = View.VISIBLE
            cardMulti.alpha = 0.5f
            cardMulti.isEnabled = false
        }
    }
    
    private fun setupBooster() {
        boosterHeader.setOnClickListener {
            if (boosterContent.visibility == View.VISIBLE) {
                boosterContent.visibility = View.GONE
                boosterChevron.animate().rotation(0f).start()
            } else {
                boosterContent.visibility = View.VISIBLE
                boosterChevron.animate().rotation(180f).start()
            }
        }
    }
    
    // Internal class for booster items
    data class BoosterItem(val name: String, val bonus: String, val status: String, val iconRes: Int)
    
    private fun loadPendingApplications() {
        val context = context ?: return
        val userId = SessionManager.getUserId(context)
        if (userId == 0) return
        
        lifecycleScope.launch {
            try {
                // Get pending applications
                val appsResponse = ApiClient.apiService.getUserApplications(userId)
                val pendingApplications = if (appsResponse.isSuccessful && appsResponse.body()?.success == true) {
                    appsResponse.body()?.applications?.filter { it.status == "Pending" } ?: emptyList()
                } else {
                    emptyList()
                }
                
                val pendingApps = pendingApplications.map { it.loan_type }
                
                // Get active loans
                val loansResponse = ApiClient.apiService.getUserLoans(userId)
                val activeLoans = if (loansResponse.isSuccessful && loansResponse.body()?.success == true) {
                    loansResponse.body()?.loans?.filter { it.status == "Active" }?.map { it.loan_type } ?: emptyList()
                } else {
                    emptyList()
                }
                
                // Lifecycle check before updating UI
                if (!isAdded || view == null) return@launch
                
                // Combine both - can't apply if you have pending OR active
                pendingLoanTypes = (pendingApps + activeLoans).distinct()
                
                // Populate pending applications list
                populatePendingApplications(pendingApplications)
                
                setupPendingState()
                setupTypeSelection()
                // Auto-select first available
                autoSelectLoanType()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun populatePendingApplications(applications: List<LoanApplication>) {
        if (context == null || !::pendingApplicationsList.isInitialized) return
        
        pendingApplicationsList.removeAllViews()
        
        if (applications.isEmpty()) {
            pendingApplicationsCard.visibility = View.GONE
            return
        }
        
        pendingApplicationsCard.visibility = View.VISIBLE
        pendingAppsCount.text = applications.size.toString()
        
        val inflater = LayoutInflater.from(context)
        
        for (app in applications) {
            val itemView = inflater.inflate(R.layout.item_pending_application, pendingApplicationsList, false)
            
            val loanType = itemView.findViewById<TextView>(R.id.pendingAppLoanType)
            val amount = itemView.findViewById<TextView>(R.id.pendingAppAmount)
            val date = itemView.findViewById<TextView>(R.id.pendingAppDate)
            
            loanType.text = app.loan_type
            amount.text = "₱${String.format("%,.2f", app.amount)}"
            
            // Format date
            try {
                val dateStr = app.applied_date ?: ""
                if (dateStr.isNotEmpty()) {
                    date.text = "Applied: $dateStr"
                } else {
                    date.text = "Applied: Recently"
                }
            } catch (e: Exception) {
                date.text = "Applied: Recently"
            }
            
            pendingApplicationsList.addView(itemView)
        }
    }
    
    private fun loadUserRequirements() {
        val context = context ?: return
        val userId = SessionManager.getUserId(context)
        if (userId == 0) return
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getUserRequirements(userId)
                
                // Lifecycle check before updating UI
                if (!isAdded || view == null) return@launch
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val requirements = response.body()?.requirements ?: emptyList()
                    // Map Requirement Name to its Status (safely)
                    userRequirementsMap = requirements.associate { (it.name ?: "") to (it.status ?: "Missing") }
                    populateBooster(requirements)
                    
                    // Refresh current view if type selected
                    if (selectedType.isNotEmpty()) {
                        updateRequirementsList(selectedType)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun loadCreditLimit() {
        val context = context ?: return
        val userId = SessionManager.getUserId(context)
        if (userId == 0) return
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getDashboardStats(userId)
                
                // Lifecycle check before updating UI
                if (!isAdded || view == null) return@launch
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val stats = response.body()?.stats
                    creditLimit = stats?.credit_limit ?: 50000.0
                    availableCredit = stats?.available_credit ?: 50000.0
                    totalBalance = stats?.total_balance ?: 0.0
                    
                    // Update UI to show credit limit
                    updateCreditLimitDisplay()
                } else {
                    // API call failed, use minimum defaults
                    creditLimit = 50000.0
                    availableCredit = 50000.0
                    totalBalance = 0.0
                    updateCreditLimitDisplay()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded && view != null) {
                    Toast.makeText(context, "Failed to load credit limit: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                // Use minimum defaults on error
                creditLimit = 50000.0
                availableCredit = 50000.0
                totalBalance = 0.0
                updateCreditLimitDisplay()
            }
        }
    }
    
    private fun updateCreditLimitDisplay() {
        try {
            // Lifecycle and view initialization checks
            if (!isAdded || view == null) return
            if (!::creditLimitAmount.isInitialized || !::creditLimitProgress.isInitialized || !::amountSlider.isInitialized) {
                return
            }
            
            // The main display should show TOTAL CREDIT LIMIT, not available
            creditLimitAmount.text = "₱${String.format("%,.2f", creditLimit)}"
            
            // Calculate used credit (total balance)
            val usedCredit = creditLimit - availableCredit
            usedCreditText.text = "Used: ₱${String.format("%,.2f", usedCredit)}"
            
            // Calculate percentage
            val percentage = if (creditLimit > 0) {
                ((usedCredit / creditLimit) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            creditPercentageText.text = "$percentage% USED"
            
            // Update progress bar
            creditLimitProgress.setProgress(percentage, true)
            
            // Update slider max value to AVAILABLE credit (what you can actually borrow)
            try {
                if (availableCredit > 0) {
                    val newMax = availableCredit.toFloat().coerceAtLeast(10000f)
                    val currentValue = amountSlider.value
                    
                    if (newMax > amountSlider.valueFrom) {
                        amountSlider.valueTo = newMax
                        
                        // CRITICAL FIX: Ensure current value doesn't exceed new max
                        if (currentValue > newMax) {
                            amountSlider.value = newMax
                        }
                        
                        amountSlider.isEnabled = true
                    }
                    // Update max amount text
                    maxAmountText.text = "Max: ₱${String.format("%,.0fk", availableCredit / 1000)}"
                } else {
                    // No available credit - set to minimum range to avoid crash
                    amountSlider.valueTo = 10000f  // Must be greater than valueFrom (5000)
                    amountSlider.value = 5000f  // Reset to minimum
                    maxAmountText.text = "Max: ₱0"
                    // Disable the slider since user can't borrow
                    amountSlider.isEnabled = false
                }
            } catch (sliderError: Exception) {
                sliderError.printStackTrace()
                // If slider update fails, reset to safe defaults
                try {
                    amountSlider.valueTo = 10000f
                    amountSlider.value = 5000f
                    amountSlider.isEnabled = false
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            
            // Credit limit info is now visible in the UI, no need for toast
        } catch (e: Exception) {
            e.printStackTrace()
            if (isAdded && context != null) {
                Toast.makeText(context, "Error updating credit display: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun autoSelectLoanType() {
        if (!pendingLoanTypes.contains("Personal Loan")) {
            selectType("Personal Loan", cardPersonal)
        } else if (!pendingLoanTypes.contains("Multipurpose Loan")) {
            selectType("Multipurpose Loan", cardMulti)
        } else {
            selectedType = ""
        }
    }
    
    private fun populateBooster(requirements: List<Requirement>) {
        if (context == null) return
        boosterContent.removeAllViews()
        val inflater = LayoutInflater.from(context)
        
        for (req in requirements) {
            val name = req.name ?: "Unknown"
            val status = req.status ?: "Missing"
            val bonus = req.bonus ?: ""
            
            val itemData = BoosterItem(name, bonus, status, R.drawable.ic_description)
            val view = inflater.inflate(R.layout.item_checklist_req, boosterContent, false)
            
            // Adjust layout params for full width in booster
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 0, 0, 16)
            view.layoutParams = params
            
            val title = view.findViewById<TextView>(R.id.reqName)
            val bonusText = view.findViewById<TextView>(R.id.reqBonus)
            val statusPill = view.findViewById<TextView>(R.id.statusPill)
            val statusIcon = view.findViewById<ImageView>(R.id.statusIcon)
            
            title.text = itemData.name
            bonusText.text = itemData.bonus
            statusPill.text = itemData.status.uppercase()
            
            // Logic for status styling
            val context = requireContext()
             when (itemData.status) {
                "Verified" -> {
                    statusPill.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_50))
                    statusPill.setTextColor(ContextCompat.getColor(context, R.color.green_700))
                    statusIcon.setImageResource(R.drawable.ic_check_circle)
                    statusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_600))
                    bonusText.visibility = View.VISIBLE
                }
                "Pending" -> {
                    statusPill.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow_50))
                    statusPill.setTextColor(ContextCompat.getColor(context, R.color.yellow_600))
                    statusIcon.setImageResource(R.drawable.ic_description)
                    statusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow_600))
                    bonusText.visibility = View.VISIBLE
                }
                else -> { // Missing
                    statusPill.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red_50))
                    statusPill.setTextColor(ContextCompat.getColor(context, R.color.red_600))
                    statusIcon.setImageResource(R.drawable.ic_cancel)
                    statusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red_600))
                    bonusText.visibility = View.GONE
                }
            }
            
            boosterContent.addView(view)
        }
    }
    
    private fun updateRequirementsList(type: String) {
        reqTitle.text = "Requirements for $type"
        reqList.removeAllViews()
        if (context == null) return
        val inflater = LayoutInflater.from(context)
        
        val specificReqs = loanRequirements[type] ?: emptyList()
        var allMet = true
        
        for (reqName in specificReqs) {
            val item = inflater.inflate(R.layout.item_checklist_req, reqList, false)
            
            val title = item.findViewById<TextView>(R.id.reqName)
            val bonusText = item.findViewById<TextView>(R.id.reqBonus)
            val statusPill = item.findViewById<TextView>(R.id.statusPill)
            val statusIcon = item.findViewById<ImageView>(R.id.statusIcon)
            
            title.text = reqName
            bonusText.visibility = View.GONE // Hide bonus in this simplified list
            
            val status = userRequirementsMap[reqName]
            
            if (status == "Verified") {
                statusPill.text = "VERIFIED"
                statusPill.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green_50))
                statusPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_700))
                statusIcon.setImageResource(R.drawable.ic_check_circle)
                statusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.green_600))
            } else if (status == "Pending") {
                // Pending is acceptable to proceed? logic: yes, or warn. For now let's allow.
                statusPill.text = "PENDING"
                statusPill.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.yellow_50))
                statusPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.yellow_600))
                statusIcon.setImageResource(R.drawable.ic_description)
                statusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.yellow_600))
            } else {
                allMet = false
                statusPill.text = "MISSING"
                statusPill.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red_50))
                statusPill.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_600))
                statusIcon.setImageResource(R.drawable.ic_cancel)
                statusIcon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.red_600))
            }
            
            reqList.addView(item)
        }
        
        // Disable submit if requirements missing (Verified or Pending required)
        if (!allMet) {
            btnSubmit.isEnabled = false
            btnSubmit.alpha = 0.5f
            btnSubmit.text = "Incomplete Requirements"
        } else {
            btnSubmit.isEnabled = true
            btnSubmit.alpha = 1.0f
            btnSubmit.text = "Continue Application"
        }
    }
    
    private fun setupSliders() {
         val formatCurrency = NumberFormat.getCurrencyInstance(Locale("en", "PH"))
         formatCurrency.maximumFractionDigits = 0
         
         amountSlider.addOnChangeListener { _, value, _ ->
             amountDisplay.text = formatCurrency.format(value)
         }
         
         termSlider.addOnChangeListener { _, value, _ ->
             termDisplay.text = "${value.toInt()} months"
         }
    }
    
    private fun setupTypeSelection() {
        if (!pendingLoanTypes.contains("Personal Loan")) cardPersonal.setOnClickListener { selectType("Personal Loan", cardPersonal) }
        if (!pendingLoanTypes.contains("Home Loan")) cardHome.setOnClickListener { selectType("Home Loan", cardHome) }
        if (!pendingLoanTypes.contains("Multipurpose Loan")) cardMulti.setOnClickListener { selectType("Multipurpose Loan", cardMulti) }
    }
    
    private fun selectType(type: String, selectedCard: LinearLayout) {
        // Lifecycle check
        if (!isAdded || view == null) return
        
        selectedType = type
        
        // Reset all
        resetCardStyle(cardPersonal)
        resetCardStyle(cardHome)
        resetCardStyle(cardMulti)
        
        // Highlight selected - with safe view access
        try {
            selectedCard.background.setTint(ContextCompat.getColor(requireContext(), R.color.blue_50))
            if (selectedCard.childCount >= 2) {
                val icon = selectedCard.getChildAt(0) as? ImageView
                val text = selectedCard.getChildAt(1) as? TextView
                icon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.blue_900))
                text?.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // Update Reqs
        updateRequirementsList(type)
    }
    
    private fun resetCardStyle(card: LinearLayout) {
        try {
            if (!isAdded) return
            card.background.setTint(ContextCompat.getColor(requireContext(), R.color.white))
            if (card.childCount >= 2) {
                val icon = card.getChildAt(0) as? ImageView
                val text = card.getChildAt(1) as? TextView
                icon?.setColorFilter(ContextCompat.getColor(requireContext(), R.color.gray_300))
                text?.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_300))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun setupSubmit(view: View) {
        btnSubmit.setOnClickListener {
            val userId = SessionManager.getUserId(requireContext())
            
            if (userId == 0) {
                Toast.makeText(context, "Please login first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validate loan type is selected
            if (selectedType.isEmpty()) {
                Toast.makeText(context, "Please select a loan type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val amount = amountSlider.value.toDouble()
            val termMonths = termSlider.value.toInt()
            val monthlyPayment = amount / termMonths
            
            // CREDIT LIMIT VALIDATION
            if (amount > availableCredit) {
                Toast.makeText(
                    context,
                    "Insufficient credit! You requested ₱${String.format("%,.2f", amount)} but only have ₱${String.format("%,.2f", availableCredit)} available.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            
            // Show confirmation dialog
            showLoanConfirmationDialog(userId, amount, termMonths, monthlyPayment)
        }
    }
    
    private fun showLoanConfirmationDialog(userId: Int, amount: Double, termMonths: Int, monthlyPayment: Double) {
        val context = context ?: return
        
        // Create dialog
        val dialog = android.app.AlertDialog.Builder(context)
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loan_confirmation, null)
        dialog.setView(dialogView)
        
        val alertDialog = dialog.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        // Populate dialog with loan details
        val confirmLoanType = dialogView.findViewById<TextView>(R.id.confirmLoanType)
        val confirmAmount = dialogView.findViewById<TextView>(R.id.confirmAmount)
        val confirmTerm = dialogView.findViewById<TextView>(R.id.confirmTerm)
        val confirmMonthlyPayment = dialogView.findViewById<TextView>(R.id.confirmMonthlyPayment)
        val confirmCreditInfo = dialogView.findViewById<TextView>(R.id.confirmCreditInfo)
        val confirmRemainingCredit = dialogView.findViewById<TextView>(R.id.confirmRemainingCredit)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<Button>(R.id.btnConfirm)
        val cbTerms = dialogView.findViewById<android.widget.CheckBox>(R.id.cbTerms)
        val termsContainer = dialogView.findViewById<LinearLayout>(R.id.termsContainer)
        
        confirmLoanType.text = selectedType
        confirmAmount.text = "₱${String.format("%,.2f", amount)}"
        confirmTerm.text = "$termMonths months"
        confirmMonthlyPayment.text = "₱${String.format("%,.2f", monthlyPayment)}"
        confirmCreditInfo.text = "Available Credit: ₱${String.format("%,.2f", availableCredit)}"
        
        val remainingAfterLoan = availableCredit - amount
        confirmRemainingCredit.text = "Remaining after this loan: ₱${String.format("%,.2f", remainingAfterLoan)}"
        
        // Terms Logic
        btnConfirm.isEnabled = false
        btnConfirm.alpha = 0.5f
        
        // Checkbox status is controlled by the container click if disabled, or logic if enabled
        // Actually, logic: Click container -> Show Terms -> if read, enable and check box.
        
        termsContainer.setOnClickListener {
            if (!cbTerms.isChecked) {
                // Open terms to accept
                showTermsDialog {
                    cbTerms.isChecked = true
                    btnConfirm.isEnabled = true
                    btnConfirm.alpha = 1.0f
                    Toast.makeText(context, "Terms Accepted.", Toast.LENGTH_SHORT).show()
                }
            } else {
                 // Already checked, toggle off?
                 cbTerms.isChecked = false
                 btnConfirm.isEnabled = false
                 btnConfirm.alpha = 0.5f
            }
        }
        
        // Cancel button
        btnCancel.setOnClickListener {
            alertDialog.dismiss()
        }
        
        // Confirm button - submit the loan
        btnConfirm.setOnClickListener {
            if (cbTerms.isChecked) {
                alertDialog.dismiss()
                submitLoanApplication(userId, amount, termMonths, monthlyPayment)
            } else {
                Toast.makeText(context, "Please agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
            }
        }
        
        alertDialog.show()
    }

    private fun showTermsDialog(onAccepted: () -> Unit) {
        val context = context ?: return
        
        val dialog = android.app.AlertDialog.Builder(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_terms_scroll, null)
        dialog.setView(view)
        
        val alertDialog = dialog.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        
        val scrollView = view.findViewById<androidx.core.widget.NestedScrollView>(R.id.termsScrollView)
        val btnAccept = view.findViewById<Button>(R.id.btnAcceptTerms)
        
        // Scroll Listener
        scrollView.setOnScrollChangeListener(androidx.core.widget.NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            val height = v.getChildAt(0).measuredHeight
            val diff = height - v.height
            
            // Check if scrolled to bottom (allow some buffer)
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
    
    private fun submitLoanApplication(userId: Int, amount: Double, termMonths: Int, monthlyPayment: Double) {
        // Disable button while submitting
        btnSubmit.isEnabled = false
        btnSubmit.text = "Submitting..."
        
        // Make API call
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.applyLoan(
                    userId = userId,
                    loanType = selectedType,
                    amount = amount,
                    termMonths = termMonths,
                    monthlyPayment = monthlyPayment
                )
                
                // Lifecycle check before updating UI
                if (!isAdded || view == null) return@launch
                
                if (response.isSuccessful && response.body()?.success == true) {
                    // Show success dialog instead of toast
                    showSuccessDialog()
                    
                    // Reload data to reflect new pending application
                    loadPendingApplications()
                    loadCreditLimit()
                    
                    // Reset form - with safe bounds
                    try {
                        val maxValue = amountSlider.valueTo
                        amountSlider.value = 50000f.coerceAtMost(maxValue)
                        termSlider.value = 12f
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    val errorMessage = response.body()?.message ?: "Submission failed"
                    showErrorDialog(errorMessage)
                }
            } catch (e: Exception) {
                if (isAdded && context != null) {
                    showErrorDialog("Network error: ${e.message}")
                }
                e.printStackTrace()
            } finally {
                if (isAdded && ::btnSubmit.isInitialized) {
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Continue Application"
                }
            }
        }
    }
    
    private fun showSuccessDialog() {
        val context = context ?: return
        
        android.app.AlertDialog.Builder(context)
            .setTitle("✓ Application Submitted")
            .setMessage("Your $selectedType application has been submitted successfully!\n\nYour application is now pending review. You will be notified once it has been processed.")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showErrorDialog(message: String) {
        val context = context ?: return
        
        android.app.AlertDialog.Builder(context)
            .setTitle("⚠ Application Failed")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
