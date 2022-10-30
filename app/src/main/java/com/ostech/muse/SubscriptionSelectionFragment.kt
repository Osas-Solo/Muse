package com.ostech.muse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentSubscriptionSelectionBinding
import com.ostech.muse.models.ErrorResponse
import com.ostech.muse.models.SubscriptionType
import com.ostech.muse.models.SubscriptionTypeListResponse
import retrofit2.Response
import java.io.IOException

class SubscriptionSelectionFragment : Fragment() {
    private var _binding: FragmentSubscriptionSelectionBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var subscriptionPlansProgressLayout: LinearLayout

    private lateinit var subscriptionPlansRecyclerView: RecyclerView

    private lateinit var subscriptionPlans: List<SubscriptionType>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentSubscriptionSelectionBinding.inflate(layoutInflater, container, false)

        subscriptionPlansProgressLayout = binding.subscriptionPlansProgressLayout
        subscriptionPlansRecyclerView = binding.subscriptionPlansRecyclerView

        subscriptionPlansRecyclerView.layoutManager = LinearLayoutManager(context)

        loadSubscriptionPlans()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Subscription Selection"
        activity?.title = fragmentTitle

        binding.apply {
        }
    }

    private fun loadSubscriptionPlans() {
        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            val noNetworkSnackbar = view?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection_message, "load subscription plans"),
                    Snackbar.LENGTH_LONG
                )
            }

            noNetworkSnackbar?.show()
        } else {
            val subscriptionPlansResponse: LiveData<Response<SubscriptionTypeListResponse>> =
                liveData {
                    try {
                        val response = MuseAPIBuilder.museAPIService.getSubscriptionTypes()
                        emit(response)
                    } catch (connectionException: IOException) {
                        Log.i(tag, "loadSubscriptionPlans: $connectionException")
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

            subscriptionPlansResponse.observe(viewLifecycleOwner) { it ->
                if (it.isSuccessful) {
                    val successJSON = it.body()
                    Log.i(tag, "Subscription plans response: $successJSON")

                    subscriptionPlans = successJSON?.subscriptionTypes!!
                    Log.i(tag, "Subscriptions: $subscriptionPlans")

                    subscriptionPlansRecyclerView.adapter = SubscriptionTypeListAdapter(
                        subscriptionPlans
                    )

                    subscriptionPlansProgressLayout.visibility = View.GONE
                } else {
                    val errorJSONString = it.errorBody()?.string()
                    Log.i(tag, "Subscriptions response: $errorJSONString")
                    val errorJSON =
                        Gson().fromJson(errorJSONString, ErrorResponse::class.java)
                    val errorMessage = errorJSON.error
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}