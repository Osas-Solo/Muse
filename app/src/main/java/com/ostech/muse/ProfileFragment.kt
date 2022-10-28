package com.ostech.muse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentProfileBinding
import com.ostech.muse.models.ErrorResponse
import com.ostech.muse.models.User
import com.ostech.muse.models.UserLoginResponse
import com.ostech.muse.models.UserProfileResponse
import com.ostech.muse.session.SessionManager
import retrofit2.Response
import java.io.IOException

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var profileProgressLayout: LinearLayout

    private lateinit var profileFullNameTextView: AppCompatTextView
    private lateinit var profileUserIDTextView: AppCompatTextView
    private lateinit var profileGenderTextView: AppCompatTextView
    private lateinit var profileEmailAddressTextView: AppCompatTextView
    private lateinit var profilePhoneNumberTextView: AppCompatTextView
    private lateinit var profileCurrentSubscriptionTextView: AppCompatTextView

    private var loggedInUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentProfileBinding.inflate(layoutInflater, container, false)

        profileProgressLayout = binding.profileProgressLayout
        profileFullNameTextView = binding.profileFullNameTextView
        profileUserIDTextView = binding.profileUserIdTextView
        profileGenderTextView = binding.profileGenderTextView
        profileEmailAddressTextView = binding.profileEmailAddressTextView
        profilePhoneNumberTextView = binding.profilePhoneNumberTextView
        profileCurrentSubscriptionTextView = binding.profileCurrentSubscriptionTextView

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Profile"
        activity?.title = fragmentTitle

        loadProfile()

        binding.apply {
        }
    }

    private fun loadProfile() {
        val userID = context?.let { SessionManager(it).fetchUserID() }

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            val noNetworkSnackbar = view?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection_message, "login"),
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}