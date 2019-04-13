package ro.expectations.radio

import android.media.AudioManager
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior
import kotlinx.android.synthetic.main.activity_main.*
import mu.KotlinLogging
import org.slf4j.impl.HandroidLoggerAdapter
import ro.expectations.radio.extensions.setupWithNavController
import ro.expectations.radio.media.BuildConfig
import ro.expectations.radio.viewmodels.MediaSessionViewModel

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

        appBar.addOnOffsetChangedListener(object : AppBarStateChangeListener() {
            override fun onStateChanged(appBarLayout: AppBarLayout, state: State) {
                if (state == State.EXPANDED) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                }
            }
        })

        // Since the app plays audio media files the volume controls should adjust the music volume.
        volumeControlStream = AudioManager.STREAM_MUSIC

        // Connect to the Media Session
        viewModel = ViewModelProviders
            .of(this, InjectorUtils.provideMediaSessionViewModel(this))
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

        // Whenever the selected controller changes
        controller.observe(this, Observer { navController ->

            // setup the action bar
            NavigationUI.setupWithNavController(collapsingToolbar, toolbar, navController)

            // and make sure the bottomNavigation is visible
            ((bottomNavigation.layoutParams as CoordinatorLayout.LayoutParams).behavior as HideBottomViewOnScrollBehavior).slideUp(bottomNavigation)
        })

        currentNavController = controller
    }
}
