package com.ostech.muse

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setMessage(getText(R.string.exit_prompt_message))
            .setPositiveButton("Yes") { _, _ -> finish() }
            .setNegativeButton("No") { _, _ ->  }
            .show()
    }
}