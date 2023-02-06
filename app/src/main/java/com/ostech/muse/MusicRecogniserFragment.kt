package com.ostech.muse

import android.Manifest.permission.ACCESS_NETWORK_STATE
import android.Manifest.permission.ACCESS_WIFI_STATE
import android.Manifest.permission.INTERNET
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentMusicRecogniserBinding
import com.ostech.muse.models.api.response.Artist
import com.ostech.muse.models.api.response.ErrorResponse
import com.ostech.muse.models.api.response.Genre
import com.ostech.muse.models.api.response.RecognitionResponse
import com.ostech.muse.models.api.response.User
import com.ostech.muse.models.api.response.UserProfileResponse
import com.ostech.muse.models.api.response.UserSubscriptionResponse
import com.ostech.muse.music.Music
import com.ostech.muse.music.Music.Companion.getTotalSuccessfullyRecognisedFiles
import com.ostech.muse.session.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.CannotWriteException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.TagException
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.time.ZoneId
import java.util.*


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
    private lateinit var musicRecogniserProgressTextView: AppCompatTextView

    private var numberOfSongsLeftToRecognise: Int = 0
    private var numberOfSelectedSongs: Int = 0

    private var loggedInUser: User? = null

    private var audioFilesURIs = listOf<Uri>()
    private var audioFiles = mutableListOf<Music>()

    private var museStoragePath = "/Music/Muse"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentMusicRecogniserBinding.inflate(layoutInflater, container, false)

        selectMusicFilesButton = binding.selectMusicFilesButton
        identifyMusicFilesButton = binding.identifyMusicFilesButton
        confirmRecognitionFloatingActionButton = binding.confirmRecognitionFloatingActionButton
        clearMusicFilesButton = binding.clearMusicFilesButton
        musicRecogniserProgressLayout = binding.musicRecogniserProgressLayout
        musicFilesRecyclerView = binding.musicFilesRecyclerView
        musicRecogniserProgressTextView = binding.musicRecogniserProgressTextView

        musicFilesRecyclerView.layoutManager = LinearLayoutManager(context)

        verifyPermissions()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Music Recogniser"
        activity?.title = fragmentTitle

        binding.apply {
            selectMusicFilesButton.setOnClickListener {
                lifecycleScope.launch {
                    selectMusicFiles()
                }
            }

            clearMusicFilesButton.setOnClickListener {
                clearSelectedMusicFiles()
            }

            identifyMusicFilesButton.setOnClickListener {
                recogniseMusicFiles()
            }

            confirmRecognitionFloatingActionButton.setOnClickListener {
                confirmMusicRecognition()
            }
        }
    }

    private suspend fun selectMusicFiles() {
        selectMusicFilesButton.isEnabled = false

        val userID = context?.let { SessionManager(it).fetchUserID() }

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            val noNetworkSnackbar = view?.let {
                Snackbar.make(
                    it,
                    getString(
                        R.string.no_internet_connection_message,
                        "select music files"
                    ),
                    Snackbar.LENGTH_LONG
                )
            }

            noNetworkSnackbar?.show()
            selectMusicFilesButton.isEnabled = true
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
                    selectMusicFilesButton.isEnabled = true
                }
            }

            profileResponse.observe(viewLifecycleOwner) { it ->
                if (it.isSuccessful) {
                    val successJSON = it.body()
                    Log.i(tag, "Profile response: $successJSON")

                    loggedInUser = successJSON?.user!!
                    Log.i(tag, "Logged in user: $loggedInUser")

                    Log.i(
                        tag,
                        "getNumberOfSongsLeft: ${loggedInUser?.currentSubscription?.numberOfSongsLeft}"
                    )

                    numberOfSongsLeftToRecognise =
                        loggedInUser?.currentSubscription?.numberOfSongsLeft ?: 0
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

                    val songsLeftErrorSnackbar = view?.let {
                        Snackbar.make(
                            it,
                            errorMessage,
                            Snackbar.LENGTH_LONG
                        )
                    }

                    songsLeftErrorSnackbar?.show()
                }
            }
        }
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

                    audioFiles.add(
                        Music(
                            false,
                            currentAudioFileURI,
                            currentAudioFile!!,
                            false,
                            null,
                            null,
                            null,
                            null,
                            null,
                        )
                    )
                }

                Log.i(tag, "Audio files: $audioFiles")

                musicFilesRecyclerView.adapter = MusicListAdapter(
                    audioFiles
                )

                if (audioFiles.isNotEmpty()) {
                    identifyMusicFilesButton.isEnabled = true
                    clearMusicFilesButton.isEnabled = true
                }
            }
        }

        selectMusicFilesButton.isEnabled = true
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
        for (i in audioFiles.indices) {
            val musicHolder = musicFilesRecyclerView.findViewHolderForAdapterPosition(i)
                    as MusicHolder
            musicHolder.stopMusic()
        }

        audioFiles.clear()
        musicFilesRecyclerView.adapter = MusicListAdapter(
            audioFiles
        )

        selectMusicFilesButton.isEnabled = true
        identifyMusicFilesButton.isEnabled = false
        clearMusicFilesButton.isEnabled = false
        confirmRecognitionFloatingActionButton.isEnabled = false
    }

    private fun recogniseMusicFiles() {
        musicRecogniserProgressTextView.text = getText(R.string.music_recogniser_progress_text)
        musicRecogniserProgressLayout.visibility = View.VISIBLE
        selectMusicFilesButton.isEnabled = false
        identifyMusicFilesButton.isEnabled = false
        clearMusicFilesButton.isEnabled = false

        toggleMusicHolderWidgets(false)

        Log.e(tag, museStoragePath)

        val museStoragePathCreationFile =
            File(Environment.getExternalStorageDirectory().absolutePath + museStoragePath)
        if (!museStoragePathCreationFile.exists()) {
            museStoragePathCreationFile.mkdirs()
            Log.e(tag, "Muse storage path created")
        }

        var recognitionCounter = 0

        audioFiles.forEach { currentAudioFile ->
            var filePath = currentAudioFile.file.path
            filePath = "/" + filePath.substring(filePath.lastIndexOf(":") + 1)
            val file = File(Environment.getExternalStorageDirectory().absolutePath + filePath)
            if (file.canRead()) {
                Log.e(tag, "can read $filePath")
            } else {
                Log.e(tag, "can not read $filePath")
            }

            val temporaryFile = extractPortionOfFile(file)

            if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
                val noNetworkSnackbar = view?.let {
                    Snackbar.make(
                        it,
                        getString(
                            R.string.no_internet_connection_message,
                            "recognise music files"
                        ),
                        Snackbar.LENGTH_LONG
                    )
                }

                noNetworkSnackbar?.show()
            } else {
                val recognitionResponse: LiveData<Response<RecognitionResponse>> = liveData {
                    try {
                        val requestFile: RequestBody =
                            temporaryFile!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                        val body: MultipartBody.Part =
                            MultipartBody.Part.createFormData("file", file.name, requestFile)

                        val response = MuseAPIBuilder.museAPIService.recogniseSong(body)
                        emit(response)
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

                        recognitionCounter++

                        if (recognitionCounter == audioFiles.size) {
                            musicRecogniserProgressLayout.visibility = View.GONE
                            clearMusicFilesButton.isEnabled = true
                            confirmRecognitionFloatingActionButton.isEnabled = true
                            toggleMusicHolderWidgets(true)
                        }
                    }
                }

                recognitionResponse.observe(viewLifecycleOwner) { it ->
                    it?.let {
                        if (it.isSuccessful) {
                            val successJSON = it.body()
                            Log.i(tag, "Recognition response: $filePath: $successJSON")

                            val musicHolder =
                                musicFilesRecyclerView.findViewHolderForAdapterPosition(
                                    audioFiles.indexOf(currentAudioFile)
                                )
                                        as MusicHolder

                            successJSON?.metadata?.let { metadata ->
                                currentAudioFile.isRecognitionSuccessful = true

                                val artists: List<Artist>? = metadata.music[0].artists
                                val genres: List<Genre>? = metadata.music[0].genres
                                currentAudioFile.artists = mutableListOf()
                                currentAudioFile.genres = mutableListOf()

                                artists?.forEach { artist ->
                                    currentAudioFile.artists?.add(artist.name)
                                }

                                genres?.forEach { genre ->
                                    currentAudioFile.genres?.add(genre.name)
                                }

                                currentAudioFile.title = metadata.music[0].trackTitle
                                currentAudioFile.album = metadata.music[0].albumName?.name

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    val localDate: LocalDate? =
                                        metadata.music[0].releaseDate?.toInstant()
                                            ?.atZone(ZoneId.systemDefault())?.toLocalDate()
                                    currentAudioFile.year = localDate?.year
                                } else {
                                    val calendar = Calendar.getInstance()
                                    calendar.time = metadata.music[0].releaseDate!!
                                    currentAudioFile.year = calendar.get(Calendar.YEAR)
                                }

                                musicHolder.musicCheckBox.isChecked = true
                                musicHolder.musicTitleTextView.text = currentAudioFile.title
                                musicHolder.musicArtistTextView.text =
                                    currentAudioFile.artists?.joinToString(", ")
                                musicHolder.musicAlbumTextView.text = currentAudioFile.album
                                musicHolder.musicGenreTextView.text =
                                    currentAudioFile.genres?.joinToString(", ")
                                musicHolder.musicYearTextView.text =
                                    currentAudioFile.year.toString()
                            }

                            recognitionCounter++

                            if (recognitionCounter == audioFiles.size) {
                                musicRecogniserProgressLayout.visibility = View.GONE
                                clearMusicFilesButton.isEnabled = true
                                confirmRecognitionFloatingActionButton.isEnabled = true
                                toggleMusicHolderWidgets(true)
                            }
                        } else {
                            val errorJSONString = it.errorBody()?.string()
                            Log.i(tag, "Recognition response: $errorJSONString")

                            recognitionCounter++

                            if (recognitionCounter == audioFiles.size) {
                                musicRecogniserProgressLayout.visibility = View.GONE
                                clearMusicFilesButton.isEnabled = true
                                confirmRecognitionFloatingActionButton.isEnabled = true
                                toggleMusicHolderWidgets(true)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun extractPortionOfFile(file: File): File? {
        val temporaryFile = File.createTempFile("temp", null, null)
        val fileArray = file.readBytes()
        Log.i(tag, "recogniseMusicFiles: ${file.name} size: ${fileArray.size}")

        val strippedBytesList = mutableListOf<Byte>()
        val oneMegaByteSize = 1024 * 1024

        for (i in 0 until oneMegaByteSize) {
            if (fileArray.size <= i) {
                break
            }

            strippedBytesList.add(fileArray[i])
        }

        val strippedBytesArray = strippedBytesList.toByteArray()
        Log.i(tag, "recogniseMusicFiles: ${file.name} stripped size: ${strippedBytesArray.size}")

        val fileOutputStream = FileOutputStream(temporaryFile)
        fileOutputStream.write(strippedBytesArray)
        fileOutputStream.close()
        return temporaryFile
    }

    private fun toggleMusicHolderWidgets(isEnabled: Boolean) {
        for (i in audioFiles.indices) {
            val musicHolder = musicFilesRecyclerView.findViewHolderForAdapterPosition(i)
                    as MusicHolder

            musicHolder.musicCheckBox.isEnabled = isEnabled
            musicHolder.musicPlayButton.isEnabled = isEnabled
            musicHolder.musicRemoveButton.isEnabled = isEnabled
        }
    }

    private fun confirmMusicRecognition() {
        if (audioFiles.getTotalSuccessfullyRecognisedFiles() == 0) {
            context?.let { it ->
                AlertDialog.Builder(it)
                    .setMessage(getText(R.string.no_file_selected_for_confirmation_message))
                    .setPositiveButton("OK") { _, _ -> }
                    .show()
            }
        } else {
            musicRecogniserProgressTextView.text = getString(R.string.confirm_recognition_progress_text)
            musicRecogniserProgressLayout.visibility = View.VISIBLE
            confirmRecognitionFloatingActionButton.isEnabled = false
            toggleMusicHolderWidgets(false)

            context?.let { it ->
                AlertDialog.Builder(it)
                    .setMessage(getText(R.string.confirmation_permission_message))
                    .setPositiveButton("OK") { _, _ ->
                        run {
                            val writeStoragePermissionIntent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                            writeStorageAccessPermissionGranter.launch(writeStoragePermissionIntent.action?.toUri())
                        }
                    }
                    .show()
            }
        }
    }

    private val writeStorageAccessPermissionGranter = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { writeStorageAccessPermissionGranterResult ->
        writeStorageAccessPermissionGranterResult?.let {
            Log.i(tag, "Folder path: ${it.path}")

            if (it.path.equals("/tree/primary:Music/Muse")) {
                context?.let { it1 ->
                    DocumentFile.fromTreeUri(it1, writeStorageAccessPermissionGranterResult)
                }

                activity?.grantUriPermission(
                    requireActivity().packageName,
                    writeStorageAccessPermissionGranterResult,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                activity?.contentResolver?.takePersistableUriPermission(
                    writeStorageAccessPermissionGranterResult,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )

                var confirmationCounter = 0
                val totalSuccessfulRecognitions =
                    audioFiles.getTotalSuccessfullyRecognisedFiles()

                audioFiles.forEach { currentAudioFile ->
                    if (currentAudioFile.isSuccessfullyRecognized()) {
                        val fileExtension = currentAudioFile.file.name.substringAfterLast(".")

                        val newFilePath =
                            "${museStoragePath}/${currentAudioFile.artists?.get(0)} - " +
                                    "${currentAudioFile.title}.$fileExtension".replaceIllegalCharacters()
                        Log.i(tag, "confirmMusicRecognition: New file path: $newFilePath")
                        val newFile =
                            File(Environment.getExternalStorageDirectory().absolutePath + newFilePath)

                        if (newFile.canRead()) {
                            Log.i(tag, "confirmMusicRecognition: Can read $newFilePath")
                        } else {
                            Log.i(tag, "confirmMusicRecognition: Cannot read $newFilePath")
                        }

                        var oldFilePath = currentAudioFile.file.path
                        oldFilePath = "/" + oldFilePath.substring(oldFilePath.lastIndexOf(":") + 1)
                        Log.i(tag, "confirmMusicRecognition: Old file path: $oldFilePath")

                        val oldFile =
                            File(Environment.getExternalStorageDirectory().absolutePath + oldFilePath)

                        if (oldFile.canRead()) {
                            Log.i(tag, "confirmMusicRecognition: Can read $oldFilePath")
                        } else {
                            Log.i(tag, "confirmMusicRecognition: Cannot read $oldFilePath")
                        }

                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                Files.copy(
                                    oldFile.toPath(),
                                    newFile.toPath(),
                                    StandardCopyOption.REPLACE_EXISTING
                                )
                            } else {
                                oldFile.copyTo(newFile, true)
                            }

                            Log.i(
                                tag,
                                "confirmMusicRecognition: ${newFile.name} copied successfully"
                            )
                        } catch (e: Exception) {
                            Log.e(tag, "confirmMusicRecognition: ${newFile.name} copy failed")
                            e.printStackTrace()
                        }

                        if (fileExtension == "mp3") {
                            setMP3MusicTag(currentAudioFile, newFile)
                        } else {
                            setOtherMusicFormatTag(currentAudioFile, newFile)
                        }

                        confirmationCounter++

                        if (confirmationCounter == totalSuccessfulRecognitions) {
                            updateSubscriptionPlan(totalSuccessfulRecognitions)
                            clearMusicFilesButton.isEnabled = true
                            musicRecogniserProgressLayout.visibility = View.GONE

                            displaySuccessfulConfirmationDialog(totalSuccessfulRecognitions)
                        }
                    }
                }
            } else {
                context?.let { it1 ->
                    AlertDialog.Builder(it1)
                        .setMessage(getText(R.string.confirmation_permission_not_granted_message))
                        .setPositiveButton("OK") { _, _ -> }
                        .show()
                }

                musicRecogniserProgressLayout.visibility = View.GONE
                confirmRecognitionFloatingActionButton.isEnabled = true
                toggleMusicHolderWidgets(true)
            }
        }
    }

    private fun String.replaceIllegalCharacters(): String {
        return this.replace("[\\\\/:*?\"<>|]".toRegex(), "")
    }

    private fun setMP3MusicTag(music: Music, newMusicFile: File) {
        try {
            val filePath = newMusicFile.path
            val audioFile =
                MP3File(filePath)
            val audioTag: Tag? = audioFile.iD3v2Tag ?: audioFile.createDefaultTag()

            audioTag?.let {
                audioTag.deleteField(FieldKey.ARTIST)
                audioTag.deleteField(FieldKey.TITLE)
                audioTag.deleteField(FieldKey.ALBUM)
                audioTag.deleteField(FieldKey.ALBUM_ARTIST)

                var artists = ""

                for (i in music.artists!!.indices) {
                    artists += music.artists!![i]

                    if (i != music.artists!!.size - 1) {
                        artists += ", "
                    }
                }

                var genres = ""

                for (i in music.genres!!.indices) {
                    genres += music.genres!![i]

                    if (i != music.genres!!.size - 1) {
                        genres += ", "
                    }
                }

                audioTag.setField(FieldKey.ARTIST, artists)
                audioTag.setField(FieldKey.TITLE, music.title)
                audioTag.setField(FieldKey.ALBUM, music.album)
                audioTag.setField(FieldKey.ALBUM_ARTIST, music.artists?.get(0))

                if (genres.isNotEmpty()) {
                    audioTag.deleteField(FieldKey.GENRE)
                    audioTag.setField(FieldKey.GENRE, genres)
                }

                music.year?.let {
                    audioTag.deleteField(FieldKey.YEAR)
                    audioTag.setField(FieldKey.YEAR, it.toString())
                }

                audioFile.tag = audioTag
                audioFile.commit()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: CannotWriteException) {
            e.printStackTrace()
        } catch (e: InvalidAudioFrameException) {
            e.printStackTrace()
        } catch (e: TagException) {
            e.printStackTrace()
        } catch (e: ReadOnlyFileException) {
            e.printStackTrace()
        } catch (e: CannotReadException) {
            e.printStackTrace()
        }
    }

    private fun setOtherMusicFormatTag(music: Music, newMusicFile: File) {
        try {
            val filePath = newMusicFile.path
            val audioFile =
                AudioFileIO.read(File(filePath))
            val audioTag: Tag? = audioFile.tagOrCreateDefault

            audioTag?.let {
                audioTag.deleteField(FieldKey.ARTIST)
                audioTag.deleteField(FieldKey.TITLE)
                audioTag.deleteField(FieldKey.ALBUM)
                audioTag.deleteField(FieldKey.ALBUM_ARTIST)

                var artists = ""

                for (i in music.artists!!.indices) {
                    artists += music.artists!![i]

                    if (i != music.artists!!.size - 1) {
                        artists += ", "
                    }
                }

                var genres = ""

                for (i in music.genres!!.indices) {
                    genres += music.genres!![i]

                    if (i != music.genres!!.size - 1) {
                        genres += ", "
                    }
                }

                audioTag.setField(FieldKey.ARTIST, artists)
                audioTag.setField(FieldKey.TITLE, music.title)
                audioTag.setField(FieldKey.ALBUM, music.album)
                audioTag.setField(FieldKey.ALBUM_ARTIST, music.artists?.get(0))

                if (genres.isNotEmpty()) {
                    audioTag.deleteField(FieldKey.GENRE)
                    audioTag.setField(FieldKey.GENRE, genres)
                }

                music.year?.let {
                    audioTag.deleteField(FieldKey.YEAR)
                    audioTag.setField(FieldKey.YEAR, it.toString())
                }

                audioFile.tag = audioTag
                audioFile.commit()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: CannotWriteException) {
            e.printStackTrace()
        } catch (e: InvalidAudioFrameException) {
            e.printStackTrace()
        } catch (e: TagException) {
            e.printStackTrace()
        } catch (e: ReadOnlyFileException) {
            e.printStackTrace()
        } catch (e: CannotReadException) {
            e.printStackTrace()
        }
    }

    private fun updateSubscriptionPlan(numberOfRecognisedSongs: Int) {
        val userID = context?.let { SessionManager(it).fetchUserID() }

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            Log.i(tag, "updateSubscriptionPlan: no internet connection")
        } else {
            val profileResponse: LiveData<Response<UserProfileResponse>> = liveData {
                try {
                    val response = userID?.let { MuseAPIBuilder.museAPIService.getUserProfile(it) }
                    response?.let { emit(it) }
                } catch (connectionException: IOException) {
                    Log.i(tag, "updateSubscriptionPlan: $connectionException")
                }
            }

            profileResponse.observe(viewLifecycleOwner) { it ->
                if (it.isSuccessful) {
                    val successJSON = it.body()
                    Log.i(tag, "Profile response: $successJSON")

                    loggedInUser = successJSON?.user!!
                    Log.i(tag, "Logged in user: $loggedInUser")

                    val subscriptionPlanID = loggedInUser?.currentSubscription?.subscriptionID

                    if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
                        Log.i(tag, "updateSubscriptionPlan: no internet connection")
                    } else {
                        val subscriptionUpdateResponse: LiveData<Response<UserSubscriptionResponse>> =
                            liveData {
                                try {
                                    val response =
                                        userID?.let {
                                            MuseAPIBuilder.museAPIService.updateSubscription(
                                                it,
                                                subscriptionPlanID!!, numberOfRecognisedSongs
                                            )
                                        }
                                    response?.let { emit(it) }
                                } catch (connectionException: IOException) {
                                    Log.i(tag, "updateSubscriptionPlan: $connectionException")
                                }
                            }

                        subscriptionUpdateResponse.observe(viewLifecycleOwner) { it1 ->
                            if (it1.isSuccessful) {
                                val subscriptionUpdateSuccessJSON = it1.body()
                                Log.i(
                                    tag,
                                    "Subscription update response: $subscriptionUpdateSuccessJSON"
                                )
                            } else {
                                val subscriptionUpdateErrorJSONString = it1.errorBody()?.string()
                                Log.i(
                                    tag,
                                    "Subscription update response: $subscriptionUpdateErrorJSONString"
                                )
                            }
                        }
                    }
                } else {
                    val errorJSONString = it.errorBody()?.string()
                    Log.i(tag, "Profile response: $errorJSONString")
                }
            }
        }
    }

    private fun displaySuccessfulConfirmationDialog(numberOfRecognisedSongs: Int) {
        context?.let { it ->
            AlertDialog.Builder(it)
                .setMessage(
                    resources.getQuantityString(
                        R.plurals.successful_confirmation_message,
                        numberOfRecognisedSongs,
                        numberOfRecognisedSongs
                    )
                )
                .setPositiveButton("OK") { _, _ -> }
                .show()
        }
    }

    companion object {
        private const val REQUEST_EXTERNAL_STORAGE = 1
        private val permissions = arrayOf(
            ACCESS_NETWORK_STATE,
            ACCESS_WIFI_STATE,
            INTERNET,
            WRITE_EXTERNAL_STORAGE,
            READ_EXTERNAL_STORAGE
        )
    }

    private fun verifyPermissions() {
        for (i in permissions.indices) {
            val permission = context?.let { ActivityCompat.checkSelfPermission(it, permissions[i]) }
            if (permission != PackageManager.PERMISSION_GRANTED) {
                activity?.let {
                    ActivityCompat.requestPermissions(
                        it, permissions,
                        REQUEST_EXTERNAL_STORAGE
                    )
                }
                break
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}