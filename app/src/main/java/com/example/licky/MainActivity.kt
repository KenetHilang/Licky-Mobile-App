package com.example.licky

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.licky.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        navView.setupWithNavController(navController)

        AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_camera,
                R.id.navigation_history
            )
        )


        val homeToHistory = NavOptions.Builder()
            .setEnterAnim(R.anim.to_right)
            .setExitAnim(R.anim.from_left)
            .build()


        val historyToHome = NavOptions.Builder()
            .setEnterAnim(R.anim.to_left)
            .setExitAnim(R.anim.from_right)
            .build()

        val defaultAnimation = NavOptions.Builder()
            .setEnterAnim(android.R.anim.fade_in)
            .build()


        navView.setOnItemSelectedListener { item ->
            val currentId = navController.currentDestination?.id
            val targetId = item.itemId

            if (currentId != targetId) {
                if (currentId == R.id.navigation_home && targetId == R.id.navigation_history) {
                    navController.navigate(targetId, null, homeToHistory)
                }
                else if (currentId == R.id.navigation_history && targetId == R.id.navigation_home) {
                    navController.navigate(targetId, null, historyToHome)
                }
                else {
                    navController.navigate(targetId, null, defaultAnimation)
                }
            }
            true
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_camera || destination.id == R.id.resultDetailFragment) {
                binding.bottomAppBar.visibility = android.view.View.GONE
                binding.navView.visibility = android.view.View.GONE
                binding.fab.hide()
            } else {
                binding.bottomAppBar.visibility = android.view.View.VISIBLE
                binding.navView.visibility = android.view.View.VISIBLE
                binding.fab.show()
                binding.navView.menu.findItem(destination.id)?.isChecked = true
            }
        }

        binding.fab.setOnClickListener {
            if (navController.currentDestination?.id != R.id.navigation_camera) {
                navController.navigate(R.id.navigation_camera)
            }
        }
    }
}
