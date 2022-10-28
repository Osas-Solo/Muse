package com.ostech.muse

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.ostech.muse.databinding.ActivityNavigationBinding
import com.ostech.muse.databinding.FragmentProfileBinding

class NavigationActivity : AppCompatActivity() {
    private var _binding: ActivityNavigationBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navigationDrawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigationDrawerLayout = binding.navigationDrawerLayout
        navigationView = binding.navigationView

        drawerToggle = ActionBarDrawerToggle(this, navigationDrawerLayout, R.string.open, R.string.open)
        navigationDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.apply {
            navigationView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.music_recogniser_menu_item -> {

                    }

                    R.id.profile_menu_item -> {

                    }
                }

                true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)){
            true
        }
        return super.onOptionsItemSelected(item)
    }
}