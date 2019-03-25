package ro.expectations.radio

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.appcompat.app.AppCompatActivity
import mu.KLogging
import ro.expectations.radio.media.PlaybackService

class MainActivity : AppCompatActivity() {

    companion object : KLogging()

    private lateinit var mediaBrowser: MediaBrowserCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, PlaybackService::class.java),
            connectionCallbacks,
            null
        )

        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        mediaBrowser.connect()
    }

    override fun onResume() {
        super.onResume()

        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()

        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    private val connectionCallbacks = object : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {
            super.onConnected()

            logger.info { "connected to playback service" }

            mediaBrowser.sessionToken.also { token ->

                val mediaController = MediaControllerCompat(
                    this@MainActivity,
                    token
                )

                MediaControllerCompat.setMediaController(this@MainActivity, mediaController)

                mediaController.registerCallback(controllerCallback)

                mediaController.transportControls.playFromMediaId("xxxxx", null)
            }
        }

        override fun onConnectionSuspended() {
            logger.error { "The Service has crashed. Disable transport controls until it automatically reconnects." }
        }

        override fun onConnectionFailed() {
            logger.error { "The Service has refused our connection" }
        }
    }

    private var controllerCallback = object : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            // logger.info { "MediaControllerCallback::onMetadataChanged: ${metadata?.description}" }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            // logger.info { "MediaControllerCallback::onPlaybackStateChanged: ${state?.stateName} -> $state" }
        }
    }
}
