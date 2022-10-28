package com.ostech.muse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import com.ostech.muse.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var profileFullNameTextView: AppCompatTextView
    private lateinit var profileUserIDTextView: AppCompatTextView
    private lateinit var profileGenderTextView: AppCompatTextView
    private lateinit var profileEmailAddressTextView: AppCompatTextView
    private lateinit var profilePhoneNumberTextView: AppCompatTextView
    private lateinit var profileCurrentSubscriptionTextView: AppCompatTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentProfileBinding.inflate(layoutInflater, container, false)

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

        binding.apply {
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}