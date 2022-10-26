package com.ostech.muse

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import com.ostech.muse.databinding.FragmentLoginBinding
import com.ostech.muse.models.SignupDetailsVerification
import com.ostech.muse.models.User

class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var loginInputLayout: LinearLayout
    private lateinit var loginEmailEditText: AppCompatEditText
    private lateinit var loginPasswordEditText: AppCompatEditText

    private lateinit var loginButton: AppCompatButton
    private lateinit var loginProgressLayout: LinearLayout
    private lateinit var forgotPasswordTextView: AppCompatTextView
    private lateinit var signupAlternativeTextView: AppCompatTextView

    private lateinit var loggedInUser: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentLoginBinding.inflate(layoutInflater, container, false)

        loginInputLayout = binding.loginInputsLayout

        loginEmailEditText = binding.loginEmailEditText
        loginPasswordEditText = binding.loginPasswordEditText

        loginButton = binding.loginButton
        loginProgressLayout = binding.loginProgressLayout
        forgotPasswordTextView = binding.loginForgotPasswordTextView
        signupAlternativeTextView = binding.signupAlternativeTextView

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Login"
        activity?.title = fragmentTitle

        binding.apply {
            loginEmailEditText.doOnTextChanged { _, _, _, _ ->
                toggleLoginButton()
            }

            loginPasswordEditText.doOnTextChanged { _, _, _, _ ->
                toggleLoginButton()
            }

            signupAlternativeTextView.setOnClickListener {
                launchSignupActivity()
            }
        }
    }

    private fun isEmailAddressValid(): Boolean {
        val emailAddress = loginEmailEditText.text.toString().trim()
        return SignupDetailsVerification.isEmailAddressValid(emailAddress)
    }

    private fun hasPasswordBeenEntered(): Boolean {
        return !loginPasswordEditText.text.toString().isEmpty()
    }

    private fun toggleLoginButton() {
         loginButton.isEnabled = isEmailAddressValid() && hasPasswordBeenEntered()
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