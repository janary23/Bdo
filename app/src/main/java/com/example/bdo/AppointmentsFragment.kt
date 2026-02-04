package com.example.bdo

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import java.util.Calendar

class AppointmentsFragment : Fragment() {

    private lateinit var recyclerUpcoming: RecyclerView
    private lateinit var recyclerPast: RecyclerView
    private lateinit var emptyStateUpcoming: View
    private lateinit var emptyStatePast: View
    
    // Store active loans for the booking dialog
    private var activeLoans: List<Loan> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_appointments, container, false)
        
        recyclerUpcoming = view.findViewById(R.id.recyclerUpcomingAppts)
        recyclerPast = view.findViewById(R.id.recyclerPastAppts)
        emptyStateUpcoming = view.findViewById(R.id.emptyStateUpcoming)
        emptyStatePast = view.findViewById(R.id.emptyStatePast)
        
        view.findViewById<View>(R.id.btnBookAppointment).setOnClickListener {
             showBookAppointmentDialog()
        }
        
        return view
    }
    
    override fun onResume() {
        super.onResume()
        loadAppointments()
        loadActiveLoans()
    }

    private fun loadAppointments() {
        val context = context ?: return
        val userId = SessionManager.getUserId(context)
        if (userId == 0) return
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getAppointments(userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val appointments = response.body()?.appointments ?: emptyList()
                    
                    val upcoming = appointments.filter { it.status == "Pending" || it.status == "Confirmed" }
                    val past = appointments.filter { it.status == "Completed" || it.status == "Cancelled" }
                    
                    setupRecycler(recyclerUpcoming, emptyStateUpcoming, upcoming, "No Upcoming Appointments", "You don't have any appointments scheduled.")
                    setupRecycler(recyclerPast, emptyStatePast, past, "No Appointment History", "Your past appointments will appear here.")
                    
                } else {
                     setupRecycler(recyclerUpcoming, emptyStateUpcoming, emptyList(), "No Upcoming Appointments", "You don't have any appointments scheduled.")
                     setupRecycler(recyclerPast, emptyStatePast, emptyList(), "No Appointment History", "Your past appointments will appear here.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (isAdded && view != null) {
                    Toast.makeText(context, "Error loading appointments", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun loadActiveLoans() {
        val context = context ?: return
        val userId = SessionManager.getUserId(context)
        if (userId == 0) return
        
        lifecycleScope.launch {
             try {
                 val response = ApiClient.apiService.getUserLoans(userId)
                 if (response.isSuccessful && response.body()?.success == true) {
                     activeLoans = response.body()?.loans?.filter { it.status == "Active" } ?: emptyList()
                 }
             } catch (e: Exception) {
                 e.printStackTrace()
             }
        }
    }

    private fun setupRecycler(recycler: RecyclerView, emptyView: View, items: List<Appointment>, title: String, subtitle: String) {
        if (items.isEmpty()) {
            recycler.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
            
            val tvTitle = emptyView.findViewById<TextView>(R.id.tvEmptyTitle)
            val tvSubtitle = emptyView.findViewById<TextView>(R.id.tvEmptySubtitle)
            tvTitle.text = title
            tvSubtitle.text = subtitle
            
        } else {
            recycler.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            
            recycler.layoutManager = LinearLayoutManager(context)
            recycler.adapter = AppointmentAdapter(items)
        }
    }

    private fun showBookAppointmentDialog() {
        val context = context ?: return
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_book_appointment, null)
        val dateInput = dialogView.findViewById<EditText>(R.id.etDate)
        val timeInput = dialogView.findViewById<EditText>(R.id.etTime)
        val loanSpinner = dialogView.findViewById<Spinner>(R.id.spinnerLoan)
        val notesInput = dialogView.findViewById<EditText>(R.id.etNotes)
        
        // Setup Loan Spinner with Real Data
        val loanStrings = mutableListOf<String>()
        val loanMap = mutableMapOf<String, Int>() // Display String -> Loan ID
        
        loanStrings.add("General Inquiry (No Specific Loan)")
        
        for (loan in activeLoans) {
            val display = "${loan.loan_type} (ID: ${loan.loan_id})"
            loanStrings.add(display)
            loanMap[display] = loan.loan_id
        }
        
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, loanStrings)
        loanSpinner.adapter = adapter
        
        // Date Picker
        dateInput.setOnClickListener {
            val c = Calendar.getInstance()
            val dpd = DatePickerDialog(context, { _, year, month, day ->
                // Ensure format YYYY-MM-DD for consistency with PHP date type
                dateInput.setText(String.format("%d-%02d-%02d", year, month + 1, day))
                // Clear time if date changes to prevent invalid combinations
                timeInput.setText("")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
            
            // Set Min Date to Today
            dpd.datePicker.minDate = System.currentTimeMillis() - 1000
            dpd.show()
        }
        
        // Time Picker
        timeInput.setOnClickListener {
            // Require date first
            val dateStr = dateInput.text.toString()
            if (dateStr.isEmpty()) {
                Toast.makeText(context, "Please select a date first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val c = Calendar.getInstance()
            TimePickerDialog(context, { _, hour, minute ->
                 // Create Calendar for selected time
                 val selectedCal = Calendar.getInstance()
                 // Parse dateStr (YYYY-MM-DD)
                 val parts = dateStr.split("-")
                 if (parts.size == 3) {
                     selectedCal.set(Calendar.YEAR, parts[0].toInt())
                     selectedCal.set(Calendar.MONTH, parts[1].toInt() - 1)
                     selectedCal.set(Calendar.DAY_OF_MONTH, parts[2].toInt())
                 }
                 selectedCal.set(Calendar.HOUR_OF_DAY, hour)
                 selectedCal.set(Calendar.MINUTE, minute)
                 selectedCal.set(Calendar.SECOND, 0)
                 
                 // Check if in past (allow 1 min buffer)
                 if (selectedCal.timeInMillis < System.currentTimeMillis() - 60000) {
                     Toast.makeText(context, "Cannot select a past time", Toast.LENGTH_SHORT).show()
                 } else if (hour < 9 || hour >= 16) {
                     Toast.makeText(context, "Booking time not available", Toast.LENGTH_SHORT).show()
                 } else {
                     // Ensure format HH:MM:SS for Time type
                     timeInput.setText(String.format("%02d:%02d:00", hour, minute))
                 }
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
        }
        
        val dialog = AlertDialog.Builder(context)
            .setTitle("Book Appointment")
            .setView(dialogView)
            .setPositiveButton("Book", null) // Set null here to override later
            .setNegativeButton("Cancel", null)
            .create()
            
        dialog.show()
        
        // Override Custom Button to prevent dismiss on validation fail
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val date = dateInput.text.toString()
            val time = timeInput.text.toString()
            val selectedString = loanSpinner.selectedItem.toString()
            val loanId = loanMap[selectedString] // Null if General Inquiry
            val notes = notesInput.text.toString()
            
            if (date.isNotEmpty() && time.isNotEmpty()) {
                submitAppointment(date, time, loanId, notes, dialog)
            } else {
                Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun submitAppointment(date: String, time: String, loanId: Int?, notes: String, dialog: AlertDialog) {
        val userId = SessionManager.getUserId(requireContext())
        
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.createAppointment(
                    userId = userId,
                    loanId = loanId,
                    dateScheduled = date,
                    timeScheduled = time,
                    notes = notes
                )
                
                if (response.isSuccessful && response.body()?.success == true) {
                    Toast.makeText(context, "Appointment Booked Successfully!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                    loadAppointments() // Refresh list
                } else {
                     Toast.makeText(context, "Failed: ${response.body()?.message}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    inner class AppointmentAdapter(private val items: List<Appointment>) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {
        
        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.payType)
            val date: TextView = view.findViewById(R.id.payDate)
            val time: TextView = view.findViewById(R.id.payAmount)
            val status: TextView = view.findViewById(R.id.payStatus)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_history, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = items[position]
            // Use notes/purpose as title, fallback to "Appointment"
            holder.title.text = item.notes?.takeIf { it.isNotEmpty() } ?: "General Appointment"
            holder.date.text = item.date
            
            // Format time if needed? "10:00:00" -> "10:00 AM"
            holder.time.text = item.time
            holder.status.text = item.status.uppercase()
            
            // Custom Styling for Time (reusing Amount field)
            holder.time.setTextColor(android.graphics.Color.DKGRAY)
            holder.time.textSize = 14f
            
            // Status Color
            when (item.status) {
                "Confirmed" -> {
                    holder.status.setTextColor(android.graphics.Color.parseColor("#166534")) // Green
                    holder.status.setBackgroundColor(android.graphics.Color.parseColor("#DCFCE7"))
                }
                "Pending" -> {
                    holder.status.setTextColor(android.graphics.Color.parseColor("#CA8A04")) // Yellow
                    holder.status.setBackgroundColor(android.graphics.Color.parseColor("#FEF9C3"))
                }
                "Completed" -> {
                    holder.status.setTextColor(android.graphics.Color.GRAY)
                    holder.status.setBackgroundColor(android.graphics.Color.parseColor("#F3F4F6"))
                }
                else -> { // Cancelled etc
                    holder.status.setTextColor(android.graphics.Color.RED)
                    holder.status.setBackgroundColor(android.graphics.Color.parseColor("#FEE2E2"))
                }
            }
        }

        override fun getItemCount() = items.size
    }
}
