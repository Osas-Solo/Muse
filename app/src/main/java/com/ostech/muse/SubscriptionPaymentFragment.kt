package com.ostech.muse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.PaystackSdk.applicationContext
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.ostech.muse.databinding.FragmentSubscriptionPaymentBinding
import com.ostech.muse.models.PriceUtils
import com.ostech.muse.session.SessionManager

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

        initializePaystack()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Subscription Payment"
        activity?.title = fragmentTitle

        binding.apply {
            paySubscriptionButton.setOnClickListener {
                paySubscription()
            }
        }
    }

    private fun initializePaystack() {
        PaystackSdk.initialize(applicationContext)
        PaystackSdk.setPublicKey(BuildConfig.PAYSTACK_PUBLIC_KEY)
    }

    private fun paySubscription() {
        val cardNumber = cardNumberEditText.text.toString()
        val cardExpiryDate = cardExpiryDateEditText.text.toString()
        val cvv = cardCVVEditText.text.toString()

        val cardExpiryArray = cardExpiryDate.split("/").toTypedArray()
        val expiryMonth = cardExpiryArray[0].toInt()
        val expiryYear = cardExpiryArray[1].toInt()

        val card = Card(cardNumber, expiryMonth, expiryYear, cvv)
        val emailAddress = context?.let { SessionManager(it).fetchEmailAddress() }!!

        val charge = Charge()
        charge.amount = (subscriptionPrice * 100).toInt()
        charge.email = emailAddress
        charge.card = card

        PaystackSdk.chargeCard(activity, charge, object : Paystack.TransactionCallback {
            override fun onSuccess(transaction: Transaction) {
                parseResponse(transaction.reference)
            }

            override fun beforeValidate(transaction: Transaction) {
                Log.d("Main Activity", "beforeValidate: " + transaction.reference)
            }

            override fun onError(error: Throwable, transaction: Transaction) {
                Log.d("Main Activity", "onError: " + error.localizedMessage)
                Log.d("Main Activity", "onError: $error")
            }
        })
    }

    private fun parseResponse(transactionReference: String) {
        val message = "Payment Successful - $transactionReference"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}