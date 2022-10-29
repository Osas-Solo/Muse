package com.ostech.muse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentProfileBinding
import com.ostech.muse.models.ErrorResponse
import com.ostech.muse.models.Subscription
import com.ostech.muse.models.User
import com.ostech.muse.models.UserProfileResponse
import com.ostech.muse.models.UserSubscriptionListResponse
import com.ostech.muse.session.SessionManager
import retrofit2.Response
import java.io.IOException

class ProfileFragment : Fragment() {
    private var _binding: com.ostech.muse.databinding.FragmentProfileBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var profileNestedScrollView: NestedScrollView
    private lateinit var profileProgressLayout: LinearLayout

    private lateinit var profileRefreshFloatingActionButton: FloatingActionButton
    private lateinit var profileFullNameTextView: AppCompatTextView
    private lateinit var profileUserIDTextView: AppCompatTextView
    private lateinit var profileGenderTextView: AppCompatTextView
    private lateinit var profileEmailAddressTextView: AppCompatTextView
    private lateinit var profilePhoneNumberTextView: AppCompatTextView
    private lateinit var profileCurrentSubscriptionTextView: AppCompatTextView
    private lateinit var profilePreviousSubscriptionRecyclerView: RecyclerView

    private var oldScrollYPosition : Int = 0

    private var loggedInUser: User? = null
    private lateinit var previousSubscriptions: List<Subscription>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentProfileBinding.inflate(layoutInflater, container, false)

        profileNestedScrollView = binding.profileNestedScrollView
        profileRefreshFloatingActionButton = binding.profileRefreshFloatingActionButton
        profileProgressLayout = binding.profileProgressLayout
        profileFullNameTextView = binding.profileFullNameTextView
        profileUserIDTextView = binding.profileUserIdTextView
        profileGenderTextView = binding.profileGenderTextView
        profileEmailAddressTextView = binding.profileEmailAddressTextView
        profilePhoneNumberTextView = binding.profilePhoneNumberTextView
        profileCurrentSubscriptionTextView = binding.profileCurrentSubscriptionTextView
        profilePreviousSubscriptionRecyclerView = binding.profilePreviousSubscriptionsRecyclerView

        profilePreviousSubscriptionRecyclerView.layoutManager = LinearLayoutManager(context)

        loadProfile()
        loadPreviousSubscriptions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Profile"
        activity?.title = fragmentTitle

        binding.apply {
            profileNestedScrollView.viewTreeObserver.addOnScrollChangedListener {
                val scrollY = profileNestedScrollView.scrollY

                if (scrollY > oldScrollYPosition) {
                    profileRefreshFloatingActionButton.hide()
                } else {
                    profileRefreshFloatingActionButton.show()
                }

                oldScrollYPosition = scrollY
            }

            profileRefreshFloatingActionButton.setOnClickListener {
                loadProfile()
                loadPreviousSubscriptions()
            }
        }
    }

    private fun loadProfile() {
        val userID = context?.let { SessionManager(it).fetchUserID() }

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            val noNetworkSnackbar = view?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection_message, "load profile"),
                    Snackbar.LENGTH_LONG
                )
            }

            noNetworkSnackbar?.show()
        } else {
            val profileResponse: LiveData<Response<UserProfileResponse>> = liveData {
                try {
                    val response = userID?.let { MuseAPIBuilder.museAPIService.getUserProfile(it) }
                    response?.let { emit(it) }
                } catch (connectionException: IOException) {
                    Log.i(tag, "loadProfile: $connectionException")
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

            profileResponse.observe(viewLifecycleOwner) { it ->
                if (it.isSuccessful) {
                    val successJSON = it.body()
                    Log.i(tag, "Profile response: $successJSON")

                    loggedInUser = successJSON?.user!!
                    Log.i(tag, "Logged in user: $loggedInUser")

                    profileFullNameTextView.text = loggedInUser!!.fullName
                    profileUserIDTextView.text = loggedInUser!!.userID.toString()
                    profileGenderTextView.text = loggedInUser!!.gender
                    profileEmailAddressTextView.text = loggedInUser!!.emailAddress
                    profilePhoneNumberTextView.text = loggedInUser!!.phoneNumber

                    profileCurrentSubscriptionTextView.text = loggedInUser!!.currentSubscription?.subscriptionType
                        ?: getText(R.string.no_active_subscription_message)

                    profileProgressLayout.visibility = View.GONE
                } else {
                    val errorJSONString = it.errorBody()?.string()
                    Log.i(tag, "Profile response: $errorJSONString")
                    val errorJSON =
                        Gson().fromJson(errorJSONString, ErrorResponse::class.java)
                    val errorMessage = errorJSON.error
                }
            }
        }
    }

    private fun loadPreviousSubscriptions() {
        val userID = context?.let { SessionManager(it).fetchUserID() }

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            val noNetworkSnackbar = view?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection_message, "load previous subscriptions"),
                    Snackbar.LENGTH_LONG
                )
            }

            noNetworkSnackbar?.show()
        } else {
            val subscriptionsResponse: LiveData<Response<UserSubscriptionListResponse>> = liveData {
                try {
                    val response = userID?.let { MuseAPIBuilder.museAPIService.getSubscriptions(it) }
                    response?.let { emit(it) }
                } catch (connectionException: IOException) {
                    Log.i(tag, "loadSubscriptions: $connectionException")
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

            subscriptionsResponse.observe(viewLifecycleOwner) { it ->
                if (it.isSuccessful) {
                    val successJSON = it.body()
                    Log.i(tag, "Subscriptions response: $successJSON")

                    previousSubscriptions = successJSON?.subscriptions!!
                    Log.i(tag, "Subscriptions: $previousSubscriptions")

                    profilePreviousSubscriptionRecyclerView.adapter = PreviousSubscriptionListAdapter(
                        previousSubscriptions
                    )
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