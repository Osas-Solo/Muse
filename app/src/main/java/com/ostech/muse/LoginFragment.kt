package com.ostech.muse

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.appcompat.widget.AppCompatEditText
import com.ostech.muse.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var emailEditText: AppCompatEditText
    private lateinit var passwordEditText: AppCompatEditText
    private lateinit var loginButton: AppCompatButton
    private lateinit var forgotPasswordTextView: AppCompatCheckedTextView
    private lateinit var signupAlternativeTextView: AppCompatCheckedTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Login"
        activity?.title = fragmentTitle

        binding.apply {
            signupAlternativeTextView.setOnClickListener {
                launchSignupActivity()
            }
        }
    }

    private fun launchSignupActivity() {
        val signupIntent = Intent(context, SignupActivity::class.java)
        startActivity(signupIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}