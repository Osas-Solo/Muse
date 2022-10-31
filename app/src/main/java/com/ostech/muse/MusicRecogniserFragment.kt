package com.ostech.muse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentMusicRecogniserBinding
import com.ostech.muse.models.api.response.ErrorResponse
import com.ostech.muse.models.api.response.User
import com.ostech.muse.models.api.response.UserProfileResponse
import com.ostech.muse.session.SessionManager
import retrofit2.Response
import java.io.IOException

class MusicRecogniserFragment : Fragment() {
    private var _binding: FragmentMusicRecogniserBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var selectMusicFilesButton: AppCompatButton
    private lateinit var identifyMusicFilesButton: AppCompatButton
    private lateinit var confirmRecognitionFloatingActionButton: ExtendedFloatingActionButton

    private var numberOfSongsLeftToRecognise: Int = 0

    private var loggedInUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentMusicRecogniserBinding.inflate(layoutInflater, container, false)

        selectMusicFilesButton = binding.selectMusicFilesButton
        identifyMusicFilesButton = binding.identifyMusicFilesButton
        confirmRecognitionFloatingActionButton = binding.confirmRecognitionFloatingActionButton

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Music Recogniser"
        activity?.title = fragmentTitle

        binding.apply {
            selectMusicFilesButton.setOnClickListener {
                retrieveNumberOfSongsLeft()
            }
        }
    }

    private fun retrieveNumberOfSongsLeft() {
        val userID = context?.let { SessionManager(it).fetchUserID() }

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            val noNetworkSnackbar = view?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection_message,
                        "load number of songs left in subscription plan"),
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
                    Log.i(tag, "$connectionException")
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

                    Log.i(tag, "getNumberOfSongsLeft: ${loggedInUser?.currentSubscription?.numberOfSongsLeft}")

                    numberOfSongsLeftToRecognise = loggedInUser?.currentSubscription?.numberOfSongsLeft ?: 0

                    if (numberOfSongsLeftToRecognise > 0) {
                        Toast.makeText(context, "You have $numberOfSongsLeftToRecognise songs left to recognise", Toast.LENGTH_LONG).show()
                    } else {
                        context?.let { it1 ->
                            AlertDialog.Builder(it1)
                                .setMessage(getText(R.string.no_active_subscription_message))
                                .setPositiveButton(R.string.pay_subscription_text) { _, _ -> switchToSubscriptionFragment() }
                                .show()
                        }
                    }
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

    private fun switchToSubscriptionFragment() {
        val navigationActivity = activity as NavigationActivity

        navigationActivity.switchFragment(SubscriptionSelectionFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}