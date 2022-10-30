package com.ostech.muse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentSubscriptionPaymentBinding
import com.ostech.muse.databinding.FragmentSubscriptionSelectionBinding
import com.ostech.muse.models.ErrorResponse
import com.ostech.muse.models.PriceUtils
import com.ostech.muse.models.SubscriptionType
import com.ostech.muse.models.SubscriptionTypeListResponse
import com.ostech.muse.session.SessionManager
import retrofit2.Response
import java.io.IOException

class SubscriptionPaymentFragment : Fragment() {
    private var _binding: FragmentSubscriptionPaymentBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var cardNumberEditText: AppCompatEditText
    private lateinit var cardExpiryDateEditText: AppCompatEditText
    private lateinit var cardCVVEditText: AppCompatEditText
    private lateinit var paySubscriptionButton: AppCompatButton

    private var subscriptionPlanID: Int = 0
    private var subscriptionPrice: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentSubscriptionPaymentBinding.inflate(layoutInflater, container, false)

        cardNumberEditText = binding.cardNumberEditText
        cardExpiryDateEditText = binding.cardExpiryDateEditText
        cardCVVEditText = binding.cardCvvEditText
        paySubscriptionButton = binding.paySubscriptionButton

        subscriptionPlanID = context?.let { SessionManager(it).fetchSubscriptionPlanID() }!!
        subscriptionPrice = context?.let { SessionManager(it).fetchSubscriptionPrice() }!!

        val subscriptionPriceText = getString(
            R.string.pay_subscription_button_text,
            PriceUtils.formatPrice(subscriptionPrice)
        )

        paySubscriptionButton.text = subscriptionPriceText

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Subscription Payment"
        activity?.title = fragmentTitle

        binding.apply {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}