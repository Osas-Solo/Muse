package com.ostech.muse

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentSignupBinding
import com.ostech.muse.models.SignupDetailsVerification
import com.ostech.muse.models.api.response.ErrorResponse
import com.ostech.muse.models.api.response.User
import com.ostech.muse.models.api.response.UserSignupResponse
import retrofit2.Response
import java.io.IOException

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var signupInputLayout: LinearLayout
    private lateinit var signupFirstNameEditText: AppCompatEditText
    private lateinit var signupFirstNameCheckBox: AppCompatCheckBox
    private lateinit var signupLastNameEditText: AppCompatEditText
    private lateinit var signupLastNameCheckBox: AppCompatCheckBox
    private lateinit var signupGenderSpinner: AppCompatSpinner
    private lateinit var signupGenderCheckBox: AppCompatCheckBox
    private lateinit var signupEmailEditText: AppCompatEditText
    private lateinit var signupEmailCheckBox: AppCompatCheckBox
    private lateinit var signupPasswordEditText: AppCompatEditText
    private lateinit var signupUppercasePasswordCheckBox: AppCompatCheckBox
    private lateinit var signupLowerCasePasswordCheckBox: AppCompatCheckBox
    private lateinit var signupDigitPasswordCheckBox: AppCompatCheckBox
    private lateinit var signupPasswordLengthCheckBox: AppCompatCheckBox
    private lateinit var signupPasswordConfirmerEditText: AppCompatEditText
    private lateinit var signupPasswordConfirmerCheckBox: AppCompatCheckBox
    private lateinit var signupPhoneNumberEditText: AppCompatEditText
    private lateinit var signupPhoneNumberCheckBox: AppCompatCheckBox

    private lateinit var signupButton: AppCompatButton
    private lateinit var signupProgressLayout: LinearLayout
    private lateinit var loginAlternativeTextView: AppCompatTextView

    private lateinit var signedupUser: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentSignupBinding.inflate(layoutInflater, container, false)

        signupInputLayout = binding.signupInputsLayout
        signupFirstNameEditText = binding.signupFirstNameEditText
        signupFirstNameCheckBox = binding.signupFirstNameCheckBox
        signupLastNameEditText = binding.signupLastNameEditText
        signupLastNameCheckBox = binding.signupLastNameCheckBox
        signupGenderSpinner = binding.signupGenderSpinner
        signupGenderCheckBox = binding.signupGenderCheckBox
        signupEmailEditText = binding.signupEmailEditText
        signupEmailCheckBox = binding.signupEmailCheckBox
        signupPasswordEditText = binding.signupPasswordEditText
        signupUppercasePasswordCheckBox = binding.signupUppercasePasswordCheckBox
        signupLowerCasePasswordCheckBox = binding.signupLowercasePasswordCheckBox
        signupDigitPasswordCheckBox = binding.signupDigitPasswordCheckBox
        signupPasswordLengthCheckBox = binding.signupPasswordLengthCheckBox
        signupPasswordConfirmerEditText = binding.signupPasswordConfirmerEditText
        signupPasswordConfirmerCheckBox = binding.signupPasswordConfirmerCheckBox
        signupPhoneNumberEditText = binding.signupPhoneNumberEditText
        signupPhoneNumberCheckBox = binding.signupPhoneNumberCheckBox
        signupButton = binding.signupButton
        signupProgressLayout = binding.signupProgressLayout
        loginAlternativeTextView = binding.loginAlternativeTextView

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Signup"
        activity?.title = fragmentTitle

        binding.apply {
            signupFirstNameEditText.doOnTextChanged { _, _, _, _ ->
                validateFirstName()
                toggleSignupButton()
            }

            signupLastNameEditText.doOnTextChanged { _, _, _, _ ->
                validateLastName()
                toggleSignupButton()
            }

            signupGenderSpinner.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        validateGender()
                        toggleSignupButton()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {
                    }
                }

            signupEmailEditText.doOnTextChanged { _, _, _, _ ->
                validateEmailAddress()
                toggleSignupButton()
            }

            signupPasswordEditText.doOnTextChanged { _, _, _, _ ->
                validatePassword()
                confirmPassword()
                toggleSignupButton()
            }

            signupPasswordConfirmerEditText.doOnTextChanged { _, _, _, _ ->
                confirmPassword()
                toggleSignupButton()
            }

            signupPhoneNumberEditText.doOnTextChanged { _, _, _, _ ->
                validatePhoneNumber()
                toggleSignupButton()
            }

            signupButton.setOnClickListener {
                signupUser()
            }

            loginAlternativeTextView.setOnClickListener {
                launchLoginActivity()
            }
        }
    }

    private fun launchLoginActivity() {
        val loginIntent = Intent(context, LoginActivity::class.java)
        startActivity(loginIntent)
    }

    private fun validateFirstName() {
        val firstName = signupFirstNameEditText.text.toString().trim()
        val isFirstNameValid = SignupDetailsVerification.isNameValid(firstName)

        if (isFirstNameValid) {
            signupFirstNameCheckBox.text = getText(R.string.valid_first_name_hint_text)
            signupFirstNameCheckBox.isChecked = true
            signupFirstNameCheckBox.isEnabled = true
        } else {
            signupFirstNameCheckBox.text = getText(R.string.invalid_first_name_hint_text)
            signupFirstNameCheckBox.isChecked = false
            signupFirstNameCheckBox.isEnabled = false
        }
    }

    private fun validateLastName() {
        val lastName = signupLastNameEditText.text.toString().trim()
        val isLastNameValid = SignupDetailsVerification.isNameValid(lastName)

        if (isLastNameValid) {
            signupLastNameCheckBox.text = getText(R.string.valid_last_name_hint_text)
            signupLastNameCheckBox.isChecked = true
            signupLastNameCheckBox.isEnabled = true
        } else {
            signupLastNameCheckBox.text = getText(R.string.invalid_last_name_hint_text)
            signupLastNameCheckBox.isChecked = false
            signupLastNameCheckBox.isEnabled = false
        }
    }

    private fun validateEmailAddress() {
        val emailAddress = signupEmailEditText.text.toString().trim()
        val isEmailAddressValid = SignupDetailsVerification.isEmailAddressValid(emailAddress)

        if (isEmailAddressValid) {
            signupEmailCheckBox.text = getText(R.string.valid_email_address_hint_text)
            signupEmailCheckBox.isChecked = true
            signupEmailCheckBox.isEnabled = true
        } else {
            signupEmailCheckBox.text = getText(R.string.invalid_email_address_hint_text)
            signupEmailCheckBox.isChecked = false
            signupEmailCheckBox.isEnabled = false
        }
    }

    private fun validateGender() {
        val gender = if (signupGenderSpinner.selectedItem.toString().isEmpty()) ' ' else
            signupGenderSpinner.selectedItem.toString()[0]
        val isGenderValid = SignupDetailsVerification.isGenderValid(gender)

        if (isGenderValid) {
            signupGenderCheckBox.text = getText(R.string.valid_gender_hint_text)
            signupGenderCheckBox.isChecked = true
            signupGenderCheckBox.isEnabled = true
        } else {
            signupGenderCheckBox.text = getText(R.string.invalid_gender_hint_text)
            signupGenderCheckBox.isChecked = false
            signupGenderCheckBox.isEnabled = false
        }
    }

    private fun validatePassword() {
        val password = signupPasswordEditText.text.toString()

        validatePasswordForUppercase(password)
        validatePasswordForLowercase(password)
        validatePasswordForDigit(password)
        validatePasswordForRequiredLength(password)
    }

    private fun validatePasswordForUppercase(password: String) {
        val doesPasswordContainUppercase =
            SignupDetailsVerification.doesPasswordContainUppercase(password)

        if (doesPasswordContainUppercase) {
            signupUppercasePasswordCheckBox.isChecked = true
            signupUppercasePasswordCheckBox.isEnabled = true
        } else {
            signupUppercasePasswordCheckBox.isChecked = false
            signupUppercasePasswordCheckBox.isEnabled = false
        }
    }

    private fun validatePasswordForLowercase(password: String) {
        val doesPasswordContainLowercase =
            SignupDetailsVerification.doesPasswordContainLowercase(password)

        if (doesPasswordContainLowercase) {
            signupLowerCasePasswordCheckBox.isChecked = true
            signupLowerCasePasswordCheckBox.isEnabled = true
        } else {
            signupLowerCasePasswordCheckBox.isChecked = false
            signupLowerCasePasswordCheckBox.isEnabled = false
        }
    }

    private fun validatePasswordForDigit(password: String) {
        val doesPasswordContainDigit = SignupDetailsVerification.doesPasswordContainDigit(password)

        if (doesPasswordContainDigit) {
            signupDigitPasswordCheckBox.isChecked = true
            signupDigitPasswordCheckBox.isEnabled = true
        } else {
            signupDigitPasswordCheckBox.isChecked = false
            signupDigitPasswordCheckBox.isEnabled = false
        }
    }

    private fun validatePasswordForRequiredLength(password: String) {
        val isPasswordRequiredLength = SignupDetailsVerification.isPasswordRequiredLength(password)

        if (isPasswordRequiredLength) {
            signupPasswordLengthCheckBox.isChecked = true
            signupPasswordLengthCheckBox.isEnabled = true
        } else {
            signupPasswordLengthCheckBox.isChecked = false
            signupPasswordLengthCheckBox.isEnabled = false
        }
    }

    private fun confirmPassword() {
        val password = signupPasswordEditText.text.toString()
        val passwordConfirmer = signupPasswordConfirmerEditText.text.toString().trim()
        val isPasswordConfirmed =
            SignupDetailsVerification.isPasswordConfirmed(password, passwordConfirmer)

        if (isPasswordConfirmed) {
            signupPasswordConfirmerCheckBox.text = getText(R.string.confirmed_password_hint_text)
            signupPasswordConfirmerCheckBox.isChecked = true
            signupPasswordConfirmerCheckBox.isEnabled = true
        } else {
            signupPasswordConfirmerCheckBox.text = getText(R.string.unconfirmed_password_hint_text)
            signupPasswordConfirmerCheckBox.isChecked = false
            signupPasswordConfirmerCheckBox.isEnabled = false
        }
    }

    private fun validatePhoneNumber() {
        val phoneNumber = signupPhoneNumberEditText.text.toString().trim()
        val isPhoneNumberValid = SignupDetailsVerification.isPhoneNumberValid(phoneNumber)

        if (isPhoneNumberValid) {
            signupPhoneNumberCheckBox.text = getText(R.string.valid_phone_number_hint_text)
            signupPhoneNumberCheckBox.isChecked = true
            signupPhoneNumberCheckBox.isEnabled = true
        } else {
            signupPhoneNumberCheckBox.text = getText(R.string.invalid_phone_number_hint_text)
            signupPhoneNumberCheckBox.isChecked = false
            signupPhoneNumberCheckBox.isEnabled = false
        }
    }

    private fun signupUser() {
        toggleSignupInputs(false)
        signupButton.isEnabled = false

        val firstName = signupFirstNameEditText.text.toString().trim()
        val lastName = signupLastNameEditText.text.toString().trim()
        val gender = if (signupGenderSpinner.selectedItem.toString().isEmpty()) ' ' else
            signupGenderSpinner.selectedItem.toString()[0]
        val emailAddress = signupEmailEditText.text.toString().trim()
        val password = signupPasswordEditText.text.toString()
        val passwordConfirmer = signupPasswordConfirmerEditText.text.toString()
        val phoneNumber = signupPhoneNumberEditText.text.toString().trim()

        signupProgressLayout.visibility = View.VISIBLE

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            signupProgressLayout.visibility = View.INVISIBLE

            val noNetworkSnackbar = view?.let {
                Snackbar.make(
                    it,
                    getString(R.string.no_internet_connection_message, "signup"),
                    Snackbar.LENGTH_LONG
                )
            }

            noNetworkSnackbar?.show()
            toggleSignupInputs(true)
            signupButton.isEnabled = true
        } else {
            val signupResponse: LiveData<Response<UserSignupResponse>> = liveData {
                try {
                    val response = MuseAPIBuilder.museAPIService.signupUser(
                        firstName,
                        lastName,
                        gender,
                        emailAddress,
                        password,
                        passwordConfirmer,
                        phoneNumber
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
                    signupProgressLayout.visibility = View.INVISIBLE
                    toggleSignupInputs(true)
                    signupButton.isEnabled = true
                }
            }

            signupResponse.observe(viewLifecycleOwner) { it ->
                if (it.isSuccessful) {
                    val successJSON = it.body()
                    Log.i(tag, "Signup response: $successJSON")

                    signedupUser = successJSON?.user!!
                    Log.i(tag, "Signed up user: $signedupUser")

                    context?.let { it1 ->
                        AlertDialog.Builder(it1)
                            .setMessage(getText(R.string.signup_success_message))
                            .setPositiveButton("OK") { _, _ -> launchLoginActivity() }
                            .show()
                    }
                } else {
                    val errorJSONString = it.errorBody()?.string()
                    Log.i(tag, "Signup response: $errorJSONString")
                    val errorJSON = Gson().fromJson(errorJSONString, ErrorResponse::class.java)
                    val errorMessage = errorJSON.error

                    if (errorMessage.contains("Sorry", ignoreCase = true)) {
                        val existingUserSnackbar = view?.let {
                            Snackbar.make(
                                it,
                                errorMessage,
                                Snackbar.LENGTH_LONG
                            )
                        }

                        existingUserSnackbar?.show()
                    }
                }

                signupProgressLayout.visibility = View.INVISIBLE
                toggleSignupInputs(true)
                signupButton.isEnabled = true
            }
        }

        val inputMethodManager: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(signupInputLayout.windowToken, 0)
    }

    private fun toggleSignupButton() {
        signupButton.isEnabled = areSignupDetailsValid()
    }

    private fun toggleSignupInputs(isEnabled: Boolean) {
        signupFirstNameEditText.isEnabled = isEnabled
        signupLastNameEditText.isEnabled = isEnabled
        signupGenderSpinner.isEnabled = isEnabled
        signupEmailEditText.isEnabled = isEnabled
        signupPasswordEditText.isEnabled = isEnabled
        signupPasswordConfirmerEditText.isEnabled = isEnabled
        signupPhoneNumberEditText.isEnabled = isEnabled
        loginAlternativeTextView.isEnabled = isEnabled
    }

    private fun areSignupDetailsValid(): Boolean {
        return signupFirstNameCheckBox.isChecked &&
                signupLastNameCheckBox.isChecked &&
                signupEmailCheckBox.isChecked &&
                signupGenderCheckBox.isChecked &&
                signupUppercasePasswordCheckBox.isChecked &&
                signupLowerCasePasswordCheckBox.isChecked &&
                signupDigitPasswordCheckBox.isChecked &&
                signupPasswordLengthCheckBox.isChecked &&
                signupPasswordConfirmerCheckBox.isChecked &&
                signupPhoneNumberCheckBox.isChecked
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}