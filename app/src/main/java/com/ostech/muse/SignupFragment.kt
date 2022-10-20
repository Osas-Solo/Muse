package com.ostech.muse

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.liveData
import com.ostech.muse.api.MuseAPIBuilder
import com.ostech.muse.api.NetworkUtil
import com.ostech.muse.databinding.FragmentSignupBinding
import com.ostech.muse.models.SignupDetailsVerification
import com.ostech.muse.models.User
import retrofit2.Call
import retrofit2.Response

class SignupFragment : Fragment() {
    private var _binding: FragmentSignupBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

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
    private lateinit var loginAlternativeTextView: AppCompatCheckedTextView

    private lateinit var signedupUser: User

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding =
            FragmentSignupBinding.inflate(layoutInflater, container, false)

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
        signupProgressLayout = binding.signupProgressLayout

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fragmentTitle = getString(R.string.app_name) + " - Signup"
        activity?.title = fragmentTitle

        binding.apply {
            signupFirstNameEditText.doOnTextChanged { text, start, before, count ->
                validateFirstName()
            }

            signupLastNameEditText.doOnTextChanged { text, start, before, count ->
                validateLastName()
            }

            signupGenderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    validateGender()
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                }
            }

            signupEmailEditText.doOnTextChanged { text, start, before, count ->
                validateEmailAddress()
            }

            signupPasswordEditText.doOnTextChanged { text, start, before, count ->
                validatePassword()
                confirmPassword()
            }

            signupPasswordConfirmerEditText.doOnTextChanged { text, start, before, count ->
                confirmPassword()
            }

            signupPhoneNumberEditText.doOnTextChanged { text, start, before, count ->
                validatePhoneNumber()
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
        val firstName = signupFirstNameEditText.text.toString()
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
        val lastName = signupLastNameEditText.text.toString()
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
        val emailAddress = signupEmailEditText.text.toString()
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
        val passwordConfirmer = signupPasswordConfirmerEditText.text.toString()
        val isPasswordConfirmed = SignupDetailsVerification.isPasswordConfirmed(password, passwordConfirmer)

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
        val phoneNumber = signupPhoneNumberEditText.text.toString()
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
        val firstName = signupFirstNameEditText.text.toString()
        val lastName = signupLastNameEditText.text.toString()
        val gender = if (signupGenderSpinner.selectedItem.toString().isEmpty()) ' ' else
            signupGenderSpinner.selectedItem.toString()[0]
        val emailAddress = signupEmailEditText.text.toString()
        val password = signupPasswordEditText.text.toString()
        val passwordConfirmer = signupPasswordConfirmerEditText.text.toString()
        val phoneNumber = signupPhoneNumberEditText.text.toString()

        signupProgressLayout.visibility = View.VISIBLE

        if (NetworkUtil.getConnectivityStatus(context) == NetworkUtil.TYPE_NOT_CONNECTED) {
            Toast.makeText(context, "Please ensure you are connected to the internet while signing up",
                Toast.LENGTH_LONG).show()
        } else {
            val signupResponse: LiveData<Response<String>> = liveData {
                val response = MuseAPIBuilder.museAPIService.signupUser(
                    firstName,
                    lastName,
                    gender,
                    emailAddress,
                    password,
                    passwordConfirmer,
                    phoneNumber)
                emit(response)
            }

            signupResponse.observe(viewLifecycleOwner, Observer {
                if (it.isSuccessful) {
                    Toast.makeText(context, "Signup request was cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    Log.i(tag, "Signup response: ${it.errorBody()?.string()}")
                }

                signupProgressLayout.visibility = View.INVISIBLE
            })
        }
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