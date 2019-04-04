package ro.expectations.radio

import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import mu.KotlinLogging
import org.slf4j.impl.HandroidLoggerAdapter
import ro.expectations.radio.extensions.setupWithNavController
import ro.expectations.radio.media.BuildConfig

private val logger = KotlinLogging.logger {}

class MainActivity : AppCompatActivity() {

    private var currentNavController: LiveData<NavController>? = null
    private lateinit var viewModel: MediaSessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up logging
        HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG
        HandroidLoggerAdapter.APP_NAME = "ExR"
        if (BuildConfig.DEBUG) {
            FragmentManager.enableDebugLogging(true)
        }

        logger.debug { "MainActivity::onCreate" }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Since the app plays audio media files the volume controls should adjust the music volume.
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Connect to the Media Session
        viewModel = ViewModelProviders
            .of(this, InjectorUtils.provideMainActivityViewModel(this))
            .get(MediaSessionViewModel::class.java)

        // Only set up the BottomNavigationView if the the activity is NOT being re-initialized
        // from a previously saved state. Else, wait for onRestoreInstanceState.
        if (savedInstanceState == null) {
            setupBottomNavigation()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        // BottomNavigationView has restored its instance state and its selectedItemId,
        // so we can proceed with set it up with Navigation
        setupBottomNavigation()
    }

    override fun onSupportNavigateUp(): Boolean = currentNavController?.value?.navigateUp() ?: false

    override fun onBackPressed() {
        if (currentNavController?.value?.popBackStack() != true) {
            super.onBackPressed()
        }
    }

    private fun setupBottomNavigation() {

        val navGraphIds = listOf(R.navigation.radios, R.navigation.podcasts, R.navigation.music)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigation.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.navHostContainer,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
        })
        currentNavController = controller
    }
}
