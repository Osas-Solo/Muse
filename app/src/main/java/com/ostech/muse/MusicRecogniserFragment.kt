package com.ostech.muse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.ostech.muse.databinding.FragmentMusicRecogniserBinding
import com.ostech.muse.models.User

class MusicRecogniserFragment : Fragment() {
    private var _binding: FragmentMusicRecogniserBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var selectMusicFilesButton: AppCompatButton
    private lateinit var identifyMusicFilesButton: AppCompatButton
    private lateinit var confirmRecognitionFloatingActionButton: ExtendedFloatingActionButton

    private var loggedInUser: User? = null

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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Music Recogniser"
        activity?.title = fragmentTitle

        binding.apply {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}