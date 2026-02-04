package com.example.bdo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class WelcomeWalkthroughDialog : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Apply the app theme to ensure Material components (like buttons) work correctly
        setStyle(STYLE_NORMAL, R.style.Theme_BDO)
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var btnNext: android.widget.Button
    private lateinit var btnSkip: android.widget.Button
    private lateinit var layoutIndicators: LinearLayout
    
    private val pages = listOf(
        WalkthroughPage(
            R.drawable.ic_welcome,
            "Welcome to BDO Loans",
            "Your trusted partner for financial growth and success"
        ),
        WalkthroughPage(
            R.drawable.ic_rocket,
            "Apply in Minutes",
            "Get loan approvals faster than ever. Simple forms, instant processing."
        ),
        WalkthroughPage(
            R.drawable.ic_dashboard,
            "Track Everything",
            "Monitor your loans, payments, and appointments all in one place."
        ),
        WalkthroughPage(
            R.drawable.ic_check_circle,
            "Fast Approvals",
            "Get approved quickly and start achieving your financial goals today."
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_welcome_walkthrough, container, false)
        
        viewPager = view.findViewById(R.id.viewPager)
        btnNext = view.findViewById(R.id.btnNext)
        btnSkip = view.findViewById(R.id.btnSkip)
        layoutIndicators = view.findViewById(R.id.layoutIndicators)
        
        setupViewPager()
        setupIndicators()
        setupButtons()
        
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return dialog
    }
    
    override fun onStart() {
        super.onStart()
        // Set dialog width to 85% of screen width to act as "floating" and not touch edges
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        dialog?.window?.setLayout(
            width, 
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    
    private fun setupViewPager() {
        viewPager.adapter = WalkthroughAdapter(pages)
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                updateButtons(position)
            }
        })
    }
    
    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(pages.size)
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        
        for (i in indicators.indices) {
            indicators[i] = ImageView(requireContext())
            indicators[i]?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.indicator_inactive
                )
            )
            indicators[i]?.layoutParams = layoutParams
            layoutIndicators.addView(indicators[i])
        }
        
        // Set first indicator as active
        indicators[0]?.setImageDrawable(
            ContextCompat.getDrawable(
                requireContext(),
                R.drawable.indicator_active
            )
        )
    }
    
    private fun updateIndicators(position: Int) {
        for (i in 0 until layoutIndicators.childCount) {
            val indicator = layoutIndicators.getChildAt(i) as ImageView
            if (i == position) {
                indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.indicator_active
                    )
                )
            } else {
                indicator.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }
    
    private fun updateButtons(position: Int) {
        if (position == pages.size - 1) {
            btnNext.text = "Get Started"
        } else {
            btnNext.text = "Next"
        }
    }
    
    private fun setupButtons() {
        btnNext.setOnClickListener {
            if (viewPager.currentItem < pages.size - 1) {
                viewPager.currentItem += 1
            } else {
                markAsSeen()
            }
        }
        
        btnSkip.setOnClickListener {
            markAsSeen()
        }
    }

    private fun markAsSeen() {
        val context = requireContext()
        val userId = SessionManager.getUserId(context)
        
        // Optimistically dismiss
        dismiss()
        
        // Call API to update status
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.updateWalkthroughStatus(userId)
                if (!response.isSuccessful) {
                    // Silently fail or log
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    companion object {
        const val TAG = "WelcomeWalkthroughDialog"
    }
}

// Data class for walkthrough pages
data class WalkthroughPage(
    val iconRes: Int,
    val title: String,
    val description: String
)

// ViewPager Adapter
class WalkthroughAdapter(private val pages: List<WalkthroughPage>) : 
    RecyclerView.Adapter<WalkthroughAdapter.WalkthroughViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WalkthroughViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_walkthrough_page, parent, false)
        return WalkthroughViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: WalkthroughViewHolder, position: Int) {
        holder.bind(pages[position])
    }
    
    override fun getItemCount(): Int = pages.size
    
    class WalkthroughViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtDescription: TextView = itemView.findViewById(R.id.txtDescription)
        
        fun bind(page: WalkthroughPage) {
            imgIcon.setImageResource(page.iconRes)
            txtTitle.text = page.title
            txtDescription.text = page.description
        }
    }
}
