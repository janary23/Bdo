package com.example.bdo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar

class AppointmentsFragment : Fragment() {

    private lateinit var recyclerUpcoming: RecyclerView
    private lateinit var recyclerPast: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_appointments, container, false)
        
        recyclerUpcoming = view.findViewById(R.id.recyclerUpcomingAppts)
        recyclerPast = view.findViewById(R.id.recyclerPastAppts)
        
        setupUpcoming()
        setupPast()
        
        view.findViewById<View>(R.id.btnBookAppointment).setOnClickListener {
             showBookAppointmentDialog()
        }
        
        return view
    }

    private fun setupUpcoming() {
        // Mock Data
        val items = listOf(
            Appointment("Loan Consultation", "Feb 14, 2026", "10:00 AM", "Confirmed"),
            Appointment("Document Submission", "Feb 18, 2026", "02:30 PM", "Pending")
        )
        
        recyclerUpcoming.layoutManager = LinearLayoutManager(context)
        recyclerUpcoming.adapter = AppointmentAdapter(items)
    }

    private fun setupPast() {
        val items = listOf(
            Appointment("Branch Visit", "Jan 10, 2026", "09:00 AM", "Completed")
        )
        
        recyclerPast.layoutManager = LinearLayoutManager(context)
        recyclerPast.adapter = AppointmentAdapter(items)
    }

    private fun showBookAppointmentDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_book_appointment, null)
        val dateInput = dialogView.findViewById<EditText>(R.id.etDate)
        val timeInput = dialogView.findViewById<EditText>(R.id.etTime)
        val loanSpinner = dialogView.findViewById<Spinner>(R.id.spinnerLoan)
        val notesInput = dialogView.findViewById<EditText>(R.id.etNotes)
        
        // Setup Loan Spinner (Simulating clientLoans from PHP)
        val loans = arrayOf("Personal Loan (ID: 100052)", "Business Loan (ID: 100048)")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, loans)
        loanSpinner.adapter = adapter
        
        // Date Picker
        dateInput.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, year, month, day ->
                dateInput.setText("$year-${month + 1}-$day")
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }
        
        // Time Picker
        timeInput.setOnClickListener {
            val c = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, hour, minute ->
                timeInput.setText(String.format("%02d:%02d", hour, minute))
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show()
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Book Appointment")
            .setView(dialogView)
            .setPositiveButton("Book") { _, _ ->
                val date = dateInput.text.toString()
                val time = timeInput.text.toString()
                val loan = loanSpinner.selectedItem.toString()
                val notes = notesInput.text.toString()
                
                if (date.isNotEmpty() && time.isNotEmpty()) {
                    // In a real app, this would POST to client_appointments.php
                    Toast.makeText(context, "Appointment booked for $date at $time!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    data class Appointment(val title: String, val date: String, val time: String, val status: String)

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
            holder.title.text = item.title
            holder.date.text = item.date
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
            }
        }

        override fun getItemCount() = items.size
    }
}
