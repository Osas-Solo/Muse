package com.ostech.muse

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

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