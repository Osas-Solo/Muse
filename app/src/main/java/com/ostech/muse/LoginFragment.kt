package com.ostech.muse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
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
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

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
        return !loginPasswordEditText.text.toString().isEmpty()
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
            try {
                val loginResponse: LiveData<Response<UserLoginResponse>> = liveData {
                    val response = MuseAPIBuilder.museAPIService.loginUser(
                        emailAddress,
                        password,
                    )
                    emit(response)
                }

                loginResponse.observe(viewLifecycleOwner, Observer { it ->
                    if (it.isSuccessful) {
                        val successJSON = it.body()
                        Log.i(tag, "Login response: $successJSON")

                        loggedInUser = successJSON?.user!!
                        Log.i(tag, "Logged in user: $loggedInUser")

                        val session  = SessionManager(requireContext())
                        session.saveAuthToken(successJSON.sessionToken)
                        session.saveUserID(loggedInUser.userID)

                        context?.let { it1 ->
                            AlertDialog.Builder(it1)
                                .setMessage(getText(R.string.login_success_message))
                                .setPositiveButton("OK") { _, _ -> launchSignupActivity() }
                                .show()
                        }
                    } else {
                        val errorJSONString = it.errorBody()?.string()
                        Log.i(tag, "Login response: $errorJSONString")
                        val errorJSON = Gson().fromJson(errorJSONString, ErrorResponse::class.java)
                        var errorMessage = errorJSON.error

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
                })
            }  catch (throwable: Throwable) {
                when (throwable) {
                    is ConnectException, is SocketException, is SocketTimeoutException -> {
                        Log.e(tag, "Connection exception: $throwable")
                        val socketTimeOutSnackbar = view?.let {
                            Snackbar.make(
                                it,
                                getText(R.string.poor_internet_connection_message),
                                Snackbar.LENGTH_LONG
                            )
                        }

                        socketTimeOutSnackbar?.show()
                        toggleLoginInputs(true)
                    }
                    is IOException -> {
                        Log.e(tag, "Connection exception: $throwable")
                        val socketTimeOutSnackbar = view?.let {
                            Snackbar.make(
                                it,
                                getText(R.string.poor_internet_connection_message),
                                Snackbar.LENGTH_LONG
                            )
                        }

                        socketTimeOutSnackbar?.show()
                        toggleLoginInputs(true)
                    }
                }
            }
        }

        val inputMethodManager: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(loginInputLayout.windowToken, 0)
    }

    private fun toggleLoginInputs(isEnabled: Boolean) {
        loginEmailEditText.isEnabled = isEnabled
        loginPasswordEditText.isEnabled = isEnabled
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