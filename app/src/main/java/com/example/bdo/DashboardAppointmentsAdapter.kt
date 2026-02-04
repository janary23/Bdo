package com.example.bdo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class DashboardAppointmentsAdapter(private var appointments: List<Appointment>) : 
    RecyclerView.Adapter<DashboardAppointmentsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val month: TextView = view.findViewById(R.id.apptMonth)
        val day: TextView = view.findViewById(R.id.apptDay)
        val title: TextView = view.findViewById(R.id.apptTitle)
        val time: TextView = view.findViewById(R.id.apptTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appt = appointments[position]
        
        holder.title.text = appt.notes ?: "Appointment" // Or map purpose if available
        
        // Format Time
        try {
            val inputTime = SimpleDateFormat("HH:mm:ss", Locale.US)
            val outputTime = SimpleDateFormat("h:mm a", Locale.US)
            val timeObj = inputTime.parse(appt.time)
            holder.time.text = if (timeObj != null) outputTime.format(timeObj) else appt.time
        } catch (e: Exception) {
            holder.time.text = appt.time
        }

        // Format Date for Day/Month box
        try {
            val inputDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val monthFormat = SimpleDateFormat("MMM", Locale.US)
            val dayFormat = SimpleDateFormat("dd", Locale.US)
            val dateObj = inputDate.parse(appt.date)
            
            if (dateObj != null) {
                holder.month.text = monthFormat.format(dateObj).uppercase()
                holder.day.text = dayFormat.format(dateObj)
            } else {
                holder.month.text = "-"
                holder.day.text = "-"
            }
        } catch (e: Exception) {
            holder.month.text = "-"
            holder.day.text = "-"
        }
    }

    override fun getItemCount() = appointments.size
    
    fun updateData(newAppts: List<Appointment>) {
        appointments = newAppts
        notifyDataSetChanged()
    }
}
