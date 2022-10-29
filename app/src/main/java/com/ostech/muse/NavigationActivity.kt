package com.ostech.muse

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.google.android.material.navigation.NavigationView
import com.ostech.muse.databinding.ActivityNavigationBinding

class NavigationActivity : AppCompatActivity() {
    private var _binding: ActivityNavigationBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var navigationDrawerLayout: DrawerLayout
    private lateinit var navigationFragmentContainerView: FragmentContainerView
    private lateinit var navigationView: NavigationView
    private lateinit var onScreenFragment: Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navigationDrawerLayout = binding.navigationDrawerLayout
        navigationFragmentContainerView = binding.navigationFragmentContainerView
        navigationView = binding.navigationView

        drawerToggle =
            ActionBarDrawerToggle(this, navigationDrawerLayout, R.string.open, R.string.open)
        navigationDrawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        switchFragment(MusicRecogniserFragment())

        binding.apply {
            navigationView.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.music_recogniser_menu_item -> {
                        if (onScreenFragment !is LoginFragment) {
                            switchFragment(MusicRecogniserFragment())
                        }
                    }

                    R.id.profile_menu_item -> {
                        if (onScreenFragment !is ProfileFragment) {
                            switchFragment(ProfileFragment())
                        }
                    }
                }

                navigationDrawerLayout.closeDrawer(GravityCompat.START)

                true
            }
        }
    }

    private fun switchFragment(destinationFragment: Fragment) {
        onScreenFragment = destinationFragment

        supportFragmentManager.commit {
            replace(navigationFragmentContainerView.id, destinationFragment)
            setReorderingAllowed(true)
            addToBackStack(null)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (drawerToggle.onOptionsItemSelected(item)) {
            true
        }
        return super.onOptionsItemSelected(item)
    }
}