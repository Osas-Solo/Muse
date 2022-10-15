package com.ostech.muse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class IntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        val introFragment = IntroFragment()
        supportFragmentManager
            .beginTransaction()
            .add(R.id.intro_dummy_container, introFragment)
            .commit()
    }
}