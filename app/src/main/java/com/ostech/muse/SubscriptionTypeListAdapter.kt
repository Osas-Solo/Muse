package com.ostech.muse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ostech.muse.databinding.ListItemSubscriptionPlanBinding
import com.ostech.muse.models.PriceUtils
import com.ostech.muse.models.SubscriptionType

class SubscriptionTypeHolder(
    private val binding: ListItemSubscriptionPlanBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(currentSubscriptionPlan: SubscriptionType) {
        binding.subscriptionPlanNameTextView.text = currentSubscriptionPlan.subscriptionName
        binding.subscriptionPlanPriceTextView.text =
            PriceUtils.formatPrice(currentSubscriptionPlan.price)
        binding.subscriptionPlanNumberOfSongsTextView.text = currentSubscriptionPlan.numberOfSongs.toString()
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