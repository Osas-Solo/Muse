package com.ostech.muse

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentMusicRecogniserBinding
import com.ostech.muse.models.api.response.ErrorResponse
import com.ostech.muse.models.api.response.User
import com.ostech.muse.models.api.response.UserProfileResponse
import com.ostech.muse.music.Music
import com.ostech.muse.session.SessionManager
import retrofit2.Response
import java.io.File
import java.nio.file.Files
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
    private lateinit var clearMusicFilesButton: AppCompatButton
    private lateinit var musicRecogniserProgressLayout: LinearLayout
    private lateinit var musicFilesRecyclerView: RecyclerView

    private var numberOfSongsLeftToRecognise: Int = 0
    private var numberOfSelectedSongs: Int = 0

    private var loggedInUser: User? = null

    private var audioFilesURIs = listOf<Uri>()
    private var audioFiles = mutableListOf<Music>()

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
        clearMusicFilesButton = binding.clearMusicFilesButton
        musicRecogniserProgressLayout = binding.musicRecogniserProgressLayout
        musicFilesRecyclerView = binding.musicFilesRecyclerView

        musicFilesRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Music Recogniser"
        activity?.title = fragmentTitle

        binding.apply {
            selectMusicFilesButton.setOnClickListener {
                selectMusicFiles()
            }

            clearMusicFilesButton.setOnClickListener {
                clearSelectedMusicFiles()
            }
        }
    }

    private fun selectMusicFiles() {
        selectMusicFilesButton.isEnabled = false

        val userID = context?.let { SessionManager(it).fetchUserID() }

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            val noNetworkSnackbar = view?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection_message,
                        "select music files"),
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
                    numberOfSelectedSongs = audioFiles.size

                    if (numberOfSongsLeftToRecognise > 0 && numberOfSelectedSongs <= numberOfSongsLeftToRecognise) {
                        val audioFileIntent = Intent()
                        audioFileIntent.type = "audio/*"

                        startAudioFileChooser.launch(audioFileIntent.type)
                    } else if (numberOfSelectedSongs > numberOfSongsLeftToRecognise) {
                        showMusicFilesLimitSnackbar()
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

        selectMusicFilesButton.isEnabled = true
    }

    private fun switchToSubscriptionFragment() {
        val navigationActivity = activity as NavigationActivity

        navigationActivity.switchFragment(SubscriptionSelectionFragment())
    }

    private val startAudioFileChooser = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { audioFilesPickerResult ->
        if (audioFilesPickerResult != null) {
            audioFilesURIs = audioFilesPickerResult.toList()
            Log.i(tag, "Audio files URIs: $audioFilesURIs")

            if (audioFilesURIs.size > numberOfSongsLeftToRecognise) {
                showMusicFilesLimitSnackbar()
            } else {
                audioFilesURIs.forEach { currentAudioFileURI ->
                    val currentAudioFile = currentAudioFileURI.path?.let { File(it) }

                    audioFiles.add(Music(
                        false,
                        currentAudioFile!!,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                    ))
                }

                Log.i(tag, "Audio files: $audioFiles")

                musicFilesRecyclerView.adapter = MusicListAdapter(
                    audioFiles
                )

                identifyMusicFilesButton.isEnabled = true
                clearMusicFilesButton.isEnabled = true
            }
        }
    }

    private fun showMusicFilesLimitSnackbar() {
        val musicFilesLimitSnackbar = view?.let {
            Snackbar.make(
                it,
                getString(R.string.music_files_selection_limit_text, numberOfSongsLeftToRecognise),
                Snackbar.LENGTH_LONG
            )
        }

        musicFilesLimitSnackbar?.show()
    }

    private fun clearSelectedMusicFiles() {
        audioFiles.clear()
        musicFilesRecyclerView.adapter = MusicListAdapter(
            audioFiles
        )

        identifyMusicFilesButton.isEnabled = false
        clearMusicFilesButton.isEnabled = false
        confirmRecognitionFloatingActionButton.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}