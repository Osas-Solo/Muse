package com.ostech.muse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import co.paystack.android.Paystack
import co.paystack.android.PaystackSdk
import co.paystack.android.Transaction
import co.paystack.android.model.Card
import co.paystack.android.model.Charge
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.databinding.FragmentSubscriptionPaymentBinding
import com.ostech.muse.models.PriceUtils
import com.ostech.muse.models.api.response.ErrorResponse
import com.ostech.muse.models.api.response.Subscription
import com.ostech.muse.models.api.response.UserSubscriptionResponse
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
    private lateinit var subscriptionPaymentProgressLayout: LinearLayout
    private lateinit var paySubscriptionButton: AppCompatButton

    private var subscriptionPlanID: Int = 0
    private var subscriptionPrice: Double = 0.0
    private var userID: Int = 0
    private lateinit var newSubscription: Subscription

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentSubscriptionPaymentBinding.inflate(layoutInflater, container, false)

        cardNumberEditText = binding.cardNumberEditText
        cardExpiryDateEditText = binding.cardExpiryDateEditText
        cardCVVEditText = binding.cardCvvEditText
        subscriptionPaymentProgressLayout = binding.subscriptionPaymentProgressLayout
        paySubscriptionButton = binding.paySubscriptionButton

        subscriptionPlanID = context?.let { SessionManager(it).fetchSubscriptionPlanID() }!!
        subscriptionPrice = context?.let { SessionManager(it).fetchSubscriptionPrice() }!!
        userID = context?.let { SessionManager(it).fetchUserID() }!!

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
        PaystackSdk.initialize(context)
        PaystackSdk.setPublicKey(BuildConfig.PAYSTACK_PUBLIC_KEY)
    }

    private fun paySubscription() {
        subscriptionPaymentProgressLayout.visibility = View.VISIBLE
        paySubscriptionButton.isEnabled = false

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
                subscriptionPaymentProgressLayout.visibility = View.INVISIBLE
                completeTransaction(transaction.reference)
            }

            override fun beforeValidate(transaction: Transaction) {
                Log.d(tag, "beforeValidate: " + transaction.reference)
            }

            override fun onError(error: Throwable, transaction: Transaction) {
                subscriptionPaymentProgressLayout.visibility = View.INVISIBLE
                paySubscriptionButton.isEnabled = true

                Log.d(tag, "onError: " + error.localizedMessage)
                Log.d(tag, "onError: $error")

                val paymentErrorSnackbar = view?.let {
                    Snackbar.make(
                        it,
                        getText(R.string.payment_error_message),
                        Snackbar.LENGTH_LONG
                    )
                }

                paymentErrorSnackbar?.show()
            }
        })
    }

    private fun completeTransaction(transactionReference: String) {
        val transactionReferenceMessage = "Transaction reference - $transactionReference"
        Log.i(tag, "completeTransaction: $transactionReferenceMessage")

        val subscriptionPaymentResponse: LiveData<Response<UserSubscriptionResponse>> =
            liveData {
                try {
                    val response = MuseAPIBuilder.museAPIService.paySubscription(
                        userID,
                        transactionReference,
                        subscriptionPlanID,
                        subscriptionPrice
                    )

                    emit(response)
                } catch (connectionException: IOException) {
                    Log.i(tag, "completeTransaction: $connectionException")
                    val connectionErrorSnackbar = view?.let {
                        Snackbar.make(
                            it,
                            getText(R.string.poor_internet_connection_message),
                            Snackbar.LENGTH_LONG
                        )
                    }

                    connectionErrorSnackbar?.show()
                }
            }

        subscriptionPaymentResponse.observe(viewLifecycleOwner) { it ->
            if (it.isSuccessful) {
                val successJSON = it.body()
                Log.i(tag, "Subscription payment response: $successJSON")

                newSubscription = successJSON?.subscription!!
                Log.i(tag, "Subscriptions: $newSubscription")

                context?.let { it1 ->
                    AlertDialog.Builder(it1)
                        .setMessage(getText(R.string.subscription_payment_success_message))
                        .setPositiveButton("OK") { _, _ -> switchToMusicRecogniserFragment() }
                        .show()

                    switchToMusicRecogniserFragment()
                }
            } else {
                val errorJSONString = it.errorBody()?.string()
                Log.i(tag, "Subscriptions response: $errorJSONString")
                val errorJSON =
                    Gson().fromJson(errorJSONString, ErrorResponse::class.java)
                val errorMessage = errorJSON.error

                val subscriptionPaymentErrorSnackbar = view?.let {
                    Snackbar.make(
                        it,
                        errorMessage,
                        Snackbar.LENGTH_LONG
                    )
                }

                subscriptionPaymentErrorSnackbar?.show()
            }
        }
    }

    private fun switchToMusicRecogniserFragment() {
        val navigationActivity = activity as NavigationActivity

        navigationActivity.switchFragment(MusicRecogniserFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}