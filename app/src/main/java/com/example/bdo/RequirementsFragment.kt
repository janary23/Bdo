package com.example.bdo

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class RequirementsFragment : Fragment() {

    private lateinit var checklistRecyclerView: RecyclerView
    private lateinit var loanTypeSpinner: Spinner
    private lateinit var docTypeSpinner: Spinner
    private lateinit var tvProgress: TextView
    private var selectedFileName: String? = null
    
    private val filePickerLauncher = registerForActivityResult(androidx.activity.result.contract.ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Get file name from URI
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor?.moveToFirst()
            selectedFileName = cursor?.getString(nameIndex ?: 0)
            cursor?.close()
            
            // Update UI in dialog if possible (requires reference to dialog view, simplified here via toast or state)
            Toast.makeText(context, "Selected: $selectedFileName", Toast.LENGTH_SHORT).show()
            
            // Update custom dialog UI
            selectedFileName?.let { updateUploadDialogUI(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_requirements, container, false)

        checklistRecyclerView = view.findViewById(R.id.checklistRecyclerView)
        loanTypeSpinner = view.findViewById(R.id.loanTypeSpinner)
        docTypeSpinner = view.findViewById(R.id.docTypeSpinner)
        tvProgress = view.findViewById(R.id.tvProgress)

        setupSpinners()
        loadUserRequirements()

        view.findViewById<View>(R.id.btnUpload).setOnClickListener {
            showUploadDialog()
        }
        
        return view
    }
    
    private fun showUploadDialog() {
        val context = requireContext()
        val builder = AlertDialog.Builder(context)
        
        // Inflate custom layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_upload_requirements, null)
        builder.setView(dialogView)
        
        val dialog = builder.create()
        
        // Make background transparent for rounded corners
        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
        
        // UI References
        val layoutFileSelect: android.view.View = dialogView.findViewById(R.id.layoutFileSelect)
        val tvFileName: TextView = dialogView.findViewById(R.id.tvFileName)
        val tvFileHint: TextView = dialogView.findViewById(R.id.tvFileHint)
        val imgFileStatus: ImageView = dialogView.findViewById(R.id.imgFileStatus)
        val btnUpload: com.google.android.material.button.MaterialButton = dialogView.findViewById(R.id.btnConfirmUpload)
        val btnCancel: com.google.android.material.button.MaterialButton = dialogView.findViewById(R.id.btnCancel)
        
        // File Selection Logic
        layoutFileSelect.setOnClickListener {
            // Re-register or use a variable to track which dialog instance active? 
            // Simplified: we trust the single instance flow for now.
            // Ideally we'd pass a callback but for this simple refactor:
            
            // We need a way to update THIS dialog when file is picked.
            // Since launcher is in fragment, we need a mechanism.
            // Hack fix: Set a temporary listener or public variable on fragment?
            // Cleaner: Trigger launcher, and when result comes back, update the CURRENT dialog if showing.
            // Let's store reference to these views in class variables temporarily or just re-show with state.
            // Or better: Just launch.
            
            filePickerLauncher.launch("*/*")
            
            // We need to listen to the result. 
            // Since `registerForActivityResult` callback is independent, we can observe `selectedFileName` change?
            // Or we can invoke a "waiting for result" state.
        }
        
        // Manual Polling/Observer workaround since we can't easily pass the specific dialog views to the global launcher callback
        // without complex architecture changes (safest for this quick refactor):
        // We will assume the user picks a file and we update the UI when they trigger something, 
        // OR we can make the fragment track the "active upload dialog" and update it.
        
        activeUploadDialogView = dialogView // Store ref to update later
        
        btnCancel.setOnClickListener {
            selectedFileName = null
            activeUploadDialogView = null
            dialog.dismiss()
        }
        
        btnUpload.setOnClickListener {
            if (!selectedFileName.isNullOrEmpty()) {
                uploadRequirement(selectedFileName!!)
                selectedFileName = null
                activeUploadDialogView = null
                dialog.dismiss()
            }
        }
        
        dialog.show()
    }
    
    // Store reference to update UI from callback
    private var activeUploadDialogView: View? = null
    
    // Update the launcher callback to update UI
    private fun updateUploadDialogUI(fileName: String) {
        activeUploadDialogView?.let { view ->
            val tvFileName: TextView = view.findViewById(R.id.tvFileName)
            val tvFileHint: TextView = view.findViewById(R.id.tvFileHint)
            val imgFileStatus: ImageView = view.findViewById(R.id.imgFileStatus)
            val btnUpload: com.google.android.material.button.MaterialButton = view.findViewById(R.id.btnConfirmUpload)
            
            tvFileName.text = fileName
            tvFileHint.text = "Ready to upload"
            imgFileStatus.setImageResource(R.drawable.ic_check_circle)
            imgFileStatus.setColorFilter(ContextCompat.getColor(requireContext(), R.color.green_600))
            
            btnUpload.isEnabled = true
            btnUpload.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.blue_900))
        }
    }
    
    private fun uploadRequirement(fileName: String) {
        val context = context ?: return
        val userId = SessionManager.getUserId(context)
        val loanType = loanTypeSpinner.selectedItem.toString()
        val docType = docTypeSpinner.selectedItem.toString()
        
        lifecycleScope.launch {
            try {
                // Determine file path (simulated upload)
                // In a real app, we would upload the binary here using Multipart
                val filePath = "/uploads/$fileName"
                
                val response = ApiClient.apiService.uploadRequirement(
                    userId = userId,
                    requirementType = docType,
                    filePath = filePath
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Document uploaded successfully!", Toast.LENGTH_SHORT).show()
                    loadUserRequirements() // Refresh list
                } else {
                    val message = response.body()?.message ?: "Upload failed"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun setupSpinners() {
        val context = context ?: return
        val loanTypes = arrayOf("Personal Loan", "Home Loan", "Multipurpose Loan")
        val loanAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, loanTypes)
        loanTypeSpinner.adapter = loanAdapter

        val docTypes = arrayOf("Valid ID", "Proof of Income", "Employment Certificate", "Bank Statement", "Tax Return", "Billing Statement")
        val docAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, docTypes)
        docTypeSpinner.adapter = docAdapter
    }

    private fun loadUserRequirements() {
        val context = context ?: return
        val userId = SessionManager.getUserId(context)
        
        if (userId == 0) {
            Toast.makeText(context, "Please login to view requirements", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getUserRequirements(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val requirements = response.body()?.requirements ?: emptyList()
                    
                    if (requirements.isEmpty()) {
                        Toast.makeText(context, "No requirements found", Toast.LENGTH_SHORT).show()
                    } else {
                        setupChecklist(requirements)
                    }
                } else {
                    Toast.makeText(context, "Failed to load requirements", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun setupChecklist(requirements: List<Requirement>) {
        val items = requirements.map { req ->
            ChecklistItem(
                name = req.name ?: "Unknown Document",
                status = req.status ?: "Missing",
                isVerified = req.status == "Verified",
                hasBonus = !req.bonus.isNullOrEmpty(),
                bonusText = req.bonus ?: "",
                feedback = req.feedback
            )
        }

        checklistRecyclerView.layoutManager = LinearLayoutManager(context)
        checklistRecyclerView.adapter = ChecklistAdapter(items)
        
        // Update Progress
        val total = requirements.size
        val completed = requirements.count { it.status == "Verified" || it.status == "Pending" }
        tvProgress.text = "$completed / $total Completed"
    }
}

// Data Class moved outside
data class ChecklistItem(val name: String, val status: String, val isVerified: Boolean, val hasBonus: Boolean, val bonusText: String, val feedback: String?)

// Adapter moved outside
class ChecklistAdapter(private val items: List<ChecklistItem>) : RecyclerView.Adapter<ChecklistAdapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.reqName)
        val status: TextView = view.findViewById(R.id.statusPill)
        val icon: ImageView = view.findViewById(R.id.statusIcon)
        val bonus: TextView = view.findViewById(R.id.reqBonus)
        val feedback: TextView = view.findViewById(R.id.reqFeedback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_checklist_req, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.status.text = item.status.uppercase()
        
        if (item.hasBonus) {
            holder.bonus.visibility = View.VISIBLE
            holder.bonus.text = item.bonusText
        } else {
            holder.bonus.visibility = View.GONE
        }
        
        // Show feedback if present
        if (!item.feedback.isNullOrEmpty() && (item.status == "Rejected" || item.status == "Pending" || item.status == "Verified")) {
            holder.feedback.visibility = View.VISIBLE
            holder.feedback.text = if (item.status == "Rejected") "Reason: ${item.feedback}" else "Note: ${item.feedback}"
            
            // Color adjustment based on status
            val feedbackColor = when (item.status) {
                "Rejected" -> R.color.red_600
                "Verified" -> R.color.green_700
                else -> R.color.gray_600
            }
            holder.feedback.setTextColor(ContextCompat.getColor(holder.itemView.context, feedbackColor))
        } else {
            holder.feedback.visibility = View.GONE
        }

        val context = holder.itemView.context
        when (item.status) {
            "Verified" -> {
                holder.status.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_50))
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.green_700))
                holder.icon.setImageResource(R.drawable.ic_check_circle)
                holder.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_600))
            }
            "Pending" -> {
                holder.status.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow_50))
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.yellow_600))
                holder.icon.setImageResource(R.drawable.ic_description)
                holder.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow_600))
            }
            else -> { // Missing or Rejected
                holder.status.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red_50))
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.red_600))
                holder.icon.setImageResource(R.drawable.ic_cancel)
                holder.icon.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red_600))
            }
        }
    }

    override fun getItemCount() = items.size
}
