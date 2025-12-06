package com.example.licky

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.example.licky.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        supportActionBar?.hide()

        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_camera,
                R.id.navigation_history
            )
        )
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.navigation_camera) {
                binding.bottomAppBar.visibility = android.view.View.GONE
                binding.fab.hide()
            } else {
                binding.bottomAppBar.visibility = android.view.View.VISIBLE
                binding.fab.show()
            }
        }


        binding.fab.setOnClickListener {
            if (navController.currentDestination?.id != R.id.navigation_camera) {
                navController.navigate(R.id.navigation_camera)
            }
        }
    }
}
