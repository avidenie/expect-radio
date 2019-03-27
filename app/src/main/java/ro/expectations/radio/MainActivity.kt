package ro.expectations.radio

import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import mu.KotlinLogging
import org.slf4j.impl.HandroidLoggerAdapter
import ro.expectations.radio.media.BuildConfig

private val logger = KotlinLogging.logger {}

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MediaSessionViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up logging
        HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG
        HandroidLoggerAdapter.APP_NAME = "ExR"

        setContentView(R.layout.activity_main)

        // Since the app plays audio media files the volume controls should adjust the music volume.
        volumeControlStream = AudioManager.STREAM_MUSIC

        viewModel = ViewModelProviders
            .of(this, InjectorUtils.provideMainActivityViewModel(this))
            .get(MediaSessionViewModel::class.java)

        viewModel.isConnected.observe(this, Observer { isConnected ->

            logger.debug { "MainActivity::viewModel.isConnected = $isConnected" }

            if (isConnected) {
                logger.debug { "connected to media browser service" }

                viewModel.playMedia()
            }
        })
    }
}
