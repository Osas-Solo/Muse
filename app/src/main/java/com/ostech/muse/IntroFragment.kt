package com.ostech.muse

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.ostech.muse.databinding.FragmentIntroBinding

class IntroFragment: Fragment() {
    private var _binding: FragmentIntroBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private lateinit var introLoginButton: AppCompatButton
    private lateinit var introSignupButton: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentIntroBinding.inflate(layoutInflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            introLoginButton.setOnClickListener {
                launchLoginActivity()
            }

            introSignupButton.setOnClickListener {
                launchSignupActivity()
            }
        }
    }

    private fun launchSignupActivity() {
        val signupIntent = Intent(context, SignupActivity::class.java)
        startActivity(signupIntent)
    }

    private fun launchLoginActivity() {
        val loginIntent = Intent(context, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}