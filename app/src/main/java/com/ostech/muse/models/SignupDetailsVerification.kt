package com.ostech.muse.models

class SignupDetailsVerification {
    companion object {
        fun isEmailAddressValid(emailAddress: String): Boolean {
            val emailAddressRegex = Regex("^[A-Za-z0-9+_.-]+@(.+\\..+)$")

            return emailAddressRegex.matches(emailAddress)
        }

        fun doesPasswordContainUppercase(password: String): Boolean {
            val upperCaseRegex = Regex("[A-Z]")

            return upperCaseRegex.containsMatchIn(password)
        }

        fun doesPasswordContainLowercase(password: String): Boolean {
            val lowerCaseRegex = Regex("[a-z]")

            return lowerCaseRegex.containsMatchIn(password)
        }

        fun doesPasswordContainDigit(password: String): Boolean {
            val digitRegex = Regex("[0-9]")

            return digitRegex.containsMatchIn(password)
        }

        fun isPasswordRequiredLength(password: String): Boolean =
            password.length in 8..20

        fun isPasswordConfirmed(password: String, passwordConfirmer: String) =
            password == passwordConfirmer && !password.isEmpty()  && !passwordConfirmer.isEmpty()

        fun isNameValid(name: String): Boolean {
            val nameRegex = Regex("^[A-Za-z]+\$")

            return nameRegex.matches(name)
        }

        fun isGenderValid(gender: Char): Boolean =
            gender.uppercaseChar() == 'M' || gender.uppercaseChar() == 'F'

        fun isPhoneNumberValid(phoneNumber: String): Boolean {
            val phoneNumberRegex = Regex("^0[7-9][0|1][0-9]{8}\$")

            return phoneNumberRegex.matches(phoneNumber)
        }
    }
}