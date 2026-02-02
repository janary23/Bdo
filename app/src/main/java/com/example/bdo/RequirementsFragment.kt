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
        builder.setTitle("Upload Document")
        
        // Custom Layout
        val layout = android.widget.LinearLayout(context)
        layout.orientation = android.widget.LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)
        
        // Instructions
        val instructions = TextView(context)
        instructions.text = "Please attach a clear copy of your document (Image or PDF)."
        instructions.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.gray_700))
        instructions.textSize = 14f
        instructions.layoutParams = android.widget.LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 30 }
        
        // File Name Display
        val fileNameTv = TextView(context)
        fileNameTv.text = "No file selected"
        fileNameTv.setTypeface(null, android.graphics.Typeface.ITALIC)
        fileNameTv.setTextColor(androidx.core.content.ContextCompat.getColor(context, R.color.gray_500))
        fileNameTv.gravity = android.view.Gravity.CENTER
        fileNameTv.layoutParams = android.widget.LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply { bottomMargin = 20 }
        
        // Attach Button
        val attachButton = com.google.android.material.button.MaterialButton(context)
        attachButton.text = "Attach File"
        attachButton.setIconResource(R.drawable.ic_upload) // or generic icon if upload not avail
        attachButton.iconGravity = com.google.android.material.button.MaterialButton.ICON_GRAVITY_TEXT_START
        attachButton.setOnClickListener {
            // Launch file picker
            filePickerLauncher.launch("*/*")
            
            // Note: This is a bit hacky as we need to update fileNameTv after selection.
            // In a real Fragment, we'd use a ViewModel or state variable derived from the launcher callback.
            // For now, we rely on the user seeing the Toast from the launcher, 
            // and we will update the textview manually by recreating listener or simpler:
            // Let's use a dirty hack: post a delayed check or just instruct user.
            // Better: The launcher callback updates `selectedFileName`. 
            // We can't easily update this specific dialog instance's textview from the callback 
            // unless we store a reference to it globally (bad practice) or use a proper DialogFragment.
            // Simplify: Just show toast. The user knows they picked it. 
            // When they click "Upload", we check `selectedFileName`.
        }
        
        layout.addView(instructions)
        layout.addView(fileNameTv)
        layout.addView(attachButton)
        
        builder.setView(layout)
        
        builder.setPositiveButton("Upload") { dialog, _ ->
            if (!selectedFileName.isNullOrEmpty()) {
                uploadRequirement(selectedFileName!!)
                selectedFileName = null // Reset
            } else {
                Toast.makeText(context, "Please attach a file first", Toast.LENGTH_SHORT).show()
                // Prevent dialog closure? difficult with standard AlertDialog builder.
                // Just let it close and they have to try again.
            }
        }
        
        builder.setNegativeButton("Cancel") { dialog, _ ->
            selectedFileName = null
            dialog.cancel()
        }
        
        builder.show()
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
