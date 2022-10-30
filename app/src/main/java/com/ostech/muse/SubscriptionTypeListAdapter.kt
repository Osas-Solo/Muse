package com.ostech.muse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ostech.muse.databinding.ListItemSubscriptionPlanBinding
import com.ostech.muse.models.PriceUtils
import com.ostech.muse.models.SubscriptionType
import com.ostech.muse.session.SessionManager

class SubscriptionTypeHolder(
    private val binding: ListItemSubscriptionPlanBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(currentSubscriptionPlan: SubscriptionType) {
        binding.subscriptionPlanNameTextView.text = currentSubscriptionPlan.subscriptionName
        binding.subscriptionPlanPriceTextView.text =
            PriceUtils.formatPrice(currentSubscriptionPlan.price)
        binding.subscriptionPlanNumberOfSongsTextView.text = currentSubscriptionPlan.numberOfSongs.toString()

        binding.subscriptionPlanSelectButton.setOnClickListener {
            val subscriptionPlanID = currentSubscriptionPlan.subscriptionTypeID
            val subscriptionPrice = currentSubscriptionPlan.price

            switchToSubscriptionPaymentFragment(subscriptionPlanID, subscriptionPrice)
        }
    }

    private fun switchToSubscriptionPaymentFragment(subscriptionPlanID: Int, subscriptionPrice: Double) {
        SessionManager(this.binding.root.context).saveSubscriptionPlanID(subscriptionPlanID)
        SessionManager(this.binding.root.context).saveSubscriptionPrice(subscriptionPrice)

        val navigationActivity = this.binding.root.context as NavigationActivity
        navigationActivity.switchFragment(SubscriptionPaymentFragment())
    }
}

class SubscriptionTypeListAdapter(
    private val subscriptionPlans: List<SubscriptionType>
) : RecyclerView.Adapter<SubscriptionTypeHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SubscriptionTypeHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemSubscriptionPlanBinding.inflate(inflater, parent, false)

        return SubscriptionTypeHolder(binding)
    }

    override fun onBindViewHolder(holder: SubscriptionTypeHolder, position: Int) {
        val currentSubscriptionPlan = subscriptionPlans[position]
        holder.bind(currentSubscriptionPlan)
    }

    override fun getItemCount() = subscriptionPlans.size
}