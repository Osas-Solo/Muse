package com.ostech.muse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentLoginBinding
import com.ostech.muse.models.ErrorResponse
import com.ostech.muse.models.SignupDetailsVerification
import com.ostech.muse.models.User
import com.ostech.muse.models.UserLoginResponse
import com.ostech.muse.session.SessionManager
import retrofit2.Response
import java.io.IOException

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
    ): View {
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

            loginButton.setOnClickListener {
                loginUser()
            }
        }
    }

    private fun isEmailAddressValid(): Boolean {
        val emailAddress = loginEmailEditText.text.toString().trim()
        return SignupDetailsVerification.isEmailAddressValid(emailAddress)
    }

    private fun hasPasswordBeenEntered(): Boolean {
        return loginPasswordEditText.text.toString().isNotEmpty()
    }

    private fun toggleLoginButton() {
        loginButton.isEnabled = isEmailAddressValid() && hasPasswordBeenEntered()
    }

    private fun loginUser() {
        toggleLoginInputs(false)
        val emailAddress = loginEmailEditText.text.toString().trim()
        val password = loginPasswordEditText.text.toString().trim()

        loginProgressLayout.visibility = View.VISIBLE

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            loginProgressLayout.visibility = View.INVISIBLE

            val noNetworkSnackbar = view?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection_message, "login"),
                    Snackbar.LENGTH_LONG
                )
            }

            noNetworkSnackbar?.show()
            toggleLoginInputs(true)
        } else {
            val loginResponse: LiveData<Response<UserLoginResponse>> = liveData {
                try {
                    val response = MuseAPIBuilder.museAPIService.loginUser(
                        emailAddress,
                        password,
                    )
                    emit(response)
                } catch (connectionException: IOException) {
                    Log.i(tag, "loginUser: $connectionException")
                    val connectionErrorSnackbar = view?.let {
                        Snackbar.make(
                            it,
                            getText(R.string.poor_internet_connection_message),
                            Snackbar.LENGTH_LONG
                        )
                    }

                    connectionErrorSnackbar?.show()
                    loginProgressLayout.visibility = View.INVISIBLE
                    toggleLoginInputs(true)
                }
            }

            loginResponse.observe(viewLifecycleOwner) { it ->
                if (it.isSuccessful) {
                    val successJSON = it.body()
                    Log.i(tag, "Login response: $successJSON")

                    loggedInUser = successJSON?.user!!
                    Log.i(tag, "Logged in user: $loggedInUser")

                    val session = SessionManager(requireContext())
                    session.saveAuthToken(successJSON.sessionToken)
                    session.saveUserID(loggedInUser.userID)
                    session.saveUserEmailAddress(loggedInUser.emailAddress)

                    context?.let { it1 ->
                        AlertDialog.Builder(it1)
                            .setMessage(getText(R.string.login_success_message))
                            .setPositiveButton("OK") { _, _ -> launchNavigationActivity() }
                            .show()

                        launchNavigationActivity()
                    }
                } else {
                    val errorJSONString = it.errorBody()?.string()
                    Log.i(tag, "Login response: $errorJSONString")
                    val errorJSON =
                        Gson().fromJson(errorJSONString, ErrorResponse::class.java)
                    val errorMessage = errorJSON.error

                    val loginErrorSnackbar = view?.let {
                        Snackbar.make(
                            it,
                            errorMessage,
                            Snackbar.LENGTH_LONG
                        )
                    }

                    loginErrorSnackbar?.show()
                }

                loginProgressLayout.visibility = View.INVISIBLE
                toggleLoginInputs(true)
            }
        }

        val inputMethodManager: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(loginInputLayout.windowToken, 0)
    }

    private fun toggleLoginInputs(isEnabled: Boolean) {
        loginEmailEditText.isEnabled = isEnabled
        loginPasswordEditText.isEnabled = isEnabled
        forgotPasswordTextView.isEnabled = isEnabled
        signupAlternativeTextView.isEnabled = isEnabled
    }

    private fun launchSignupActivity() {
        val signupIntent = Intent(context, SignupActivity::class.java)
        startActivity(signupIntent)
    }

    private fun launchNavigationActivity() {
        val navigationIntent = Intent(context, NavigationActivity::class.java)
        startActivity(navigationIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}