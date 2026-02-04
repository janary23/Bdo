package com.example.bdo

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Locale

class RecentAppsAdapter(private var applications: List<LoanApplication>) :
    RecyclerView.Adapter<RecentAppsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val loanType: TextView = view.findViewById(R.id.appLoanType)
        val date: TextView = view.findViewById(R.id.appDate)
        val status: TextView = view.findViewById(R.id.appStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_dashboard_application, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val app = applications[position]
        
        holder.loanType.text = app.loan_type
        
        // Format Date
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
            val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val date = inputFormat.parse(app.applied_date)
            holder.date.text = if (date != null) outputFormat.format(date) else app.applied_date
        } catch (e: Exception) {
            holder.date.text = app.applied_date
        }

        holder.status.text = app.status
        
        // Style Status
        val context = holder.itemView.context
        when (app.status.lowercase()) {
            "approved" -> {
                holder.status.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_100))
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.green_700))
            }
            "pending" -> {
                holder.status.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.yellow_100))
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.yellow_800))
            }
            "rejected" -> {
                holder.status.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red_100))
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.red_700))
            }
            else -> {
                holder.status.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.gray_100))
                holder.status.setTextColor(ContextCompat.getColor(context, R.color.gray_700))
            }
        }
    }

    override fun getItemCount() = applications.size
    
    fun updateData(newApps: List<LoanApplication>) {
        applications = newApps
        notifyDataSetChanged()
    }
}
