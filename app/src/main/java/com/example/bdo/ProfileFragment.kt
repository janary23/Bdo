package com.example.bdo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etContact: EditText
    private lateinit var etAddress: EditText
    private lateinit var etDob: EditText
    private lateinit var spinnerStatus: Spinner
    private var currentMonthlyIncome: Double? = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)
        
        initializeViews(view)
        setupSpinner(view)
        loadUserProfile()
        
        view.findViewById<View>(R.id.btnSaveProfile).setOnClickListener {
            saveProfile()
        }

        view.findViewById<View>(R.id.btnChangePassword).setOnClickListener {
            Toast.makeText(context, "Password change feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        view.findViewById<View>(R.id.btnLogout).setOnClickListener {
            logout()
        }
        
        return view
    }
    
    private fun logout() {
        // Clear session
        SessionManager.clearSession(requireContext())
        
        // Navigate to LoginActivity
        val intent = android.content.Intent(requireActivity(), LoginActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }
    
    private lateinit var layoutEmploymentFields: View
    private lateinit var etEmployer: EditText
    private lateinit var etIncome: EditText
    private lateinit var etYears: EditText

    private fun initializeViews(view: View) {
        etFirstName = view.findViewById(R.id.etFirstName)
        etLastName = view.findViewById(R.id.etLastName)
        etEmail = view.findViewById(R.id.etEmail)
        etContact = view.findViewById(R.id.etContact)
        etAddress = view.findViewById(R.id.etAddress)
        etDob = view.findViewById(R.id.etDob)
        spinnerStatus = view.findViewById(R.id.spinnerStatus)
        
        // Find the employment container row(s) to hide
        // Assuming there isn't a single container ID yet in XML, we might need to target the parent LinearLayouts or find views directly.
        // Based on XML structure:
        // We have etEmployer, etIncome, etYears.
        etEmployer = view.findViewById(R.id.etEmployer)
        etIncome = view.findViewById(R.id.etIncome)
        etYears = view.findViewById(R.id.etYears)
        
        // For convenience, let's toggle visibility of the parent containers if possible,
        // or just the fields. Toggling fields is safer if we don't have IDs for containers.
        // Ideally we should update XML to wrap these in a LinearLayout with an ID.
        // But since I can't easily edit XML and verify IDs without re-reading, 
        // I will try to find the parents of the EditTexts (which are LinearLayouts in the XML provided earlier).
    }

    private fun setupSpinner(view: View) {
        val statuses = arrayOf("Full-Time Employed", "Part-Time Employed", "Self-Employed", "Freelancer", "Retired", "Student", "Unemployed")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, statuses)
        spinnerStatus.adapter = adapter
        
        spinnerStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val status = statuses[position]
                toggleEmploymentFields(status)
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }
    }
    
    private fun toggleEmploymentFields(status: String) {
        val isEmployed = status.contains("Employed") || status == "Freelancer"
        val visibility = if (isEmployed) View.VISIBLE else View.GONE
        
        // We need to find the parent layouts to hide the labels too, but for now filtering inputs is key.
        // Or simply set visibility on the EditTexts. Labels will remain though which is ugly.
        // Let's rely on finding views by ID if we can add IDs to the containers in XML, OR
        // just hide the inputs for now as a quick fix.
        // Ideally we should update XML to wrap these in a LinearLayout with an ID.
        // But since I can't easily edit XML and verify IDs without re-reading, 
        // I will try to find the parents of the EditTexts (which are LinearLayouts in the XML provided earlier).
        
        (etEmployer.parent as? View)?.visibility = visibility
        (etIncome.parent as? View)?.visibility = visibility
        (etYears.parent as? View)?.visibility = visibility
        
        // If there are specific parent containers for rows, we might need to hide them too to remove gaps
        // The XML structure has rows of 2 items.
        // Row 1: Status | Employer
        // Row 2: Income | Years
        
        // If hidden, the "Status" dropdown should span full width? It's in a horizontal LL.
        // If "Employer" is hidden, "Status" takes up 50% width and there's a gap?
        // Actually, let's just disable them first as strict hiding might break layout alignment if weights are used.
        // XML uses layout_weight="1". If we set visibility GONE on one child, the other takes full width. 
        // This is perfect! 
        
        // So:
        // Row 1: Status (Weight 1) + Employer (Weight 1) -> Hide Employer Parent -> Status takes full width.
        // Row 2: Income (Weight 1) + Years (Weight 1) -> Hide Both Parents -> Whole row disappears?
        // Row 2 is in a horizontal LinearLayout. If both children are GONE, the row height becomes 0.
        
        // Wait, "Status" is in Row 1. We ONLY want to hide Employer.
    }
    
    private fun loadUserProfile() {
        val userId = SessionManager.getUserId(requireContext())
        
        if (userId == 0) {
            Toast.makeText(context, "Please login to view profile", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getUserProfile(userId)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val user = response.body()?.user
                    
                    if (user != null) {
                        populateProfile(user)
                    }
                } else {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
    
    private fun populateProfile(user: User) {
        // ... existing name/email/contact/address/dob population ...
        // Split full name into first and last name
        val nameParts = user.full_name.split(" ", limit = 2)
        etFirstName.setText(nameParts.getOrNull(0) ?: "")
        etLastName.setText(nameParts.getOrNull(1) ?: "")
        
        etEmail.setText(user.email)
        etEmail.isEnabled = false 
        etContact.setText(user.phone ?: "")
        etAddress.setText(user.address ?: "")
        etDob.setText(user.date_of_birth ?: "")
        
        // Store income to preserve it
        currentMonthlyIncome = user.monthly_income
        
        // Set occupation spinner
        val userOccupation = user.occupation ?: "Employed"
        val adapter = spinnerStatus.adapter as ArrayAdapter<String>
        val position = adapter.getPosition(userOccupation)
        if (position >= 0) {
            spinnerStatus.setSelection(position)
        } else {
            for (i in 0 until adapter.count) {
                if (adapter.getItem(i).equals(userOccupation, ignoreCase = true)) {
                    spinnerStatus.setSelection(i)
                    break
                }
            }
        }
        
        // Initial toggle
        toggleEmploymentFields(userOccupation)
    }
    
    private fun saveProfile() {
        val userId = SessionManager.getUserId(requireContext())
        
        if (userId == 0) {
            Toast.makeText(context, "Please login to save profile", Toast.LENGTH_SHORT).show()
            return
        }
        
        val firstName = etFirstName.text.toString().trim()
        val lastName = etLastName.text.toString().trim()
        val fullName = "$firstName $lastName".trim()
        val phone = etContact.text.toString().trim().ifEmpty { null }
        val address = etAddress.text.toString().trim().ifEmpty { null }
        val dateOfBirth = etDob.text.toString().trim().ifEmpty { null }
        val occupation = spinnerStatus.selectedItem.toString()
        
        if (firstName.isEmpty()) {
            Toast.makeText(context, "First name is required", Toast.LENGTH_SHORT).show()
            return
        }
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.updateUserProfile(
                    userId = userId,
                    fullName = fullName,
                    phone = phone,
                    address = address,
                    dateOfBirth = dateOfBirth,
                    occupation = occupation,
                    monthlyIncome = currentMonthlyIncome // Send back existing income
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                    // Reload profile to refresh data
                    loadUserProfile()
                } else {
                    val message = response.body()?.message ?: "Failed to update profile"
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }
}
