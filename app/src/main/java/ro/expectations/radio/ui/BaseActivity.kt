package ro.expectations.radio.ui

import android.content.ComponentName
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import ro.expectations.radio.service.RadioService
import ro.expectations.radio.utilities.Logger

abstract class BaseActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BaseActivity"
    }

    private var shouldConnect = false
    protected lateinit var mediaBrowser : MediaBrowserCompat
    private val connectionCallback = MediaBrowserConnectionCallback()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        volumeControlStream = AudioManager.STREAM_MUSIC

        val firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            firebaseAuth.signInAnonymously().addOnCompleteListener(this, { task ->
                if (task.isSuccessful) {
                    onAuthenticated()
                } else {
                    Logger.e(TAG, task.exception as Throwable, "Firebase signInAnonymously failure")
                }
            })
        } else {
            onAuthenticated()
        }
    }

    override fun onStart() {
        super.onStart()

        shouldConnect = if (::mediaBrowser.isInitialized && !mediaBrowser.isConnected) {
            mediaBrowser.connect()
            false
        } else {
            true
        }
    }

    private fun onAuthenticated() {

        mediaBrowser = MediaBrowserCompat(
                this,
                ComponentName(this, RadioService::class.java),
                connectionCallback,
                null)

        if (shouldConnect) {
            mediaBrowser.connect()
            shouldConnect = false
        }
    }

    override fun onStop() {
        super.onStop()

        if (::mediaBrowser.isInitialized && mediaBrowser.isConnected) {
            mediaBrowser.disconnect()
        }
    }

    abstract fun onConnected()

    private inner class MediaBrowserConnectionCallback : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {

            // Create a MediaControllerCompat
            val mediaController = MediaControllerCompat(this@BaseActivity,
                    mediaBrowser.sessionToken)

            // Save the controller
            MediaControllerCompat.setMediaController(this@BaseActivity, mediaController)

            // Allow children classes to perform more actions when connected
            this@BaseActivity.onConnected()
        }

        override fun onConnectionSuspended() {
            // The Service has crashed. Disable transport controls until it automatically reconnects
        }

        override fun onConnectionFailed() {
            // The Service has refused our connection
        }
    }
}
