package com.tajmoti.tulip.ui

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.tajmoti.tulip.R
import com.tajmoti.tulip.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(
    R.layout.activity_main
) {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        binding.navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    fun swapActionBar(toolbar: Toolbar?) {
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            val appBarConfiguration = AppBarConfiguration(
                setOf(
                    R.id.navigation_search,
                    R.id.navigation_library,
                    R.id.navigation_downloads
                )
            )
            setupActionBarWithNavController(navController, appBarConfiguration)
        } else {
            // This is a hack to remove listeners referencing previously attached action bars.
            // Those action bars remained referenced by action bar destination change listeners.
            removeStaleListeners()
        }
    }

    private fun removeStaleListeners() {
        navController.destinationListeners
            .filter { it.javaClass.simpleName == "ActionBarOnDestinationChangedListener" }
            .forEach { navController.removeOnDestinationChangedListener(it) }
    }

    private val NavController.destinationListeners: List<NavController.OnDestinationChangedListener>
        @Suppress("UNCHECKED_CAST")
        get() = listenersField.get(this) as List<NavController.OnDestinationChangedListener>

    companion object {
        private val listenersField = NavController::class.java
            .getDeclaredField("onDestinationChangedListeners")
            .apply { isAccessible = true }
    }
}