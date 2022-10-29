package com.ostech.muse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ostech.muse.databinding.ListItemPreviousSubscriptionBinding
import com.ostech.muse.models.PriceUtils
import com.ostech.muse.models.Subscription

class PreviousSubscriptionHolder(
    private val binding: ListItemPreviousSubscriptionBinding
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(currentSubscription: Subscription) {
        binding.previousSubscriptionIdTextView.text = currentSubscription.subscriptionID.toString()
        binding.previousSubscriptionTransactionReferenceTextView.text = currentSubscription.transactionReference
        binding.previousSubscriptionPlanTextView.text = currentSubscription.subscriptionType
        binding.previousSubscriptionNumberOfRecognisedSongsTextView.text = currentSubscription.numberOfRecognisedSongs.toString()
        binding.previousSubscriptionNumberOfSongsLeftTextView.text = currentSubscription.numberOfSongsLeft.toString()
        binding.previousSubscriptionDateTextView.text = currentSubscription.subscriptionDate.toString()
        binding.previousSubscriptionPricePaidTextView.text = PriceUtils.formatPrice(currentSubscription.pricePaid)
    }
}

class PreviousSubscriptionListAdapter(
    private val previousSubscriptions: List<Subscription>
) : RecyclerView.Adapter<PreviousSubscriptionHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PreviousSubscriptionHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemPreviousSubscriptionBinding.inflate(inflater, parent, false)
        return PreviousSubscriptionHolder(binding)
    }

    override fun onBindViewHolder(holder: PreviousSubscriptionHolder, position: Int) {
        val crime = previousSubscriptions[position]
        holder.bind(crime)
    }

    override fun getItemCount() = previousSubscriptions.size
}
