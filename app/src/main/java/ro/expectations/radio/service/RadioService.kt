package ro.expectations.radio.service

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ro.expectations.radio.model.RadioProvider
import ro.expectations.radio.model.Resource
import ro.expectations.radio.utilities.Logger
import java.util.*


class RadioService : LifecycleMediaBrowserService() {

    companion object {
        private const val TAG = "RadioService"
        const val RADIO_BROWSER_EMPTY_ROOT = "__EMPTY_ROOT__"
        const val RADIO_BROWSER_ROOT = "__ROOT__"
    }

    private lateinit var radioProvider: RadioProvider
    private lateinit var packageValidator: PackageValidator
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var playbackStateBuilder : PlaybackStateCompat.Builder

    private var radioResource : Resource<List<MediaMetadataCompat>>? = null

    override fun onCreate() {

        super.onCreate()

        radioProvider = RadioProvider(
                FirebaseFirestore.getInstance()
        )

        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null) {
            firebaseAuth.signInAnonymously().addOnCompleteListener({ task ->
                if (task.isSuccessful) {
                    onAuthenticated()
                } else {
                    Logger.e(TAG, task.exception as Throwable, "Firebase signInAnonymously failure")
                }
            })
        } else {
            onAuthenticated()
        }

        packageValidator = PackageValidator(this)

        // Create a new media session
        mediaSession = MediaSessionCompat(this, TAG)

        // Enable callbacks from MediaButtons and TransportControls
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        playbackStateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PLAY_PAUSE)
        mediaSession.setPlaybackState(playbackStateBuilder.build())

        // Handle callbacks from a media controller
        mediaSession.setCallback(MediaSessionCallback())

        // Set the session's token so that client activities can communicate with it.
        sessionToken = mediaSession.sessionToken
    }

    private fun onAuthenticated() {
        radioProvider.radios.observe(this, Observer { resource ->
            radioResource = resource
            notifyChildrenChanged(RADIO_BROWSER_ROOT)
        })
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {

        if (!packageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            Logger.i(TAG, """
                    |OnGetRoot: Browsing NOT ALLOWED for unknown caller $clientPackageName.
                    |Returning empty browser root so all apps can use MediaController.
                    """.trimMargin())
             return MediaBrowserServiceCompat.BrowserRoot(RADIO_BROWSER_EMPTY_ROOT, null)
        }

        return MediaBrowserServiceCompat.BrowserRoot(RADIO_BROWSER_ROOT, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        when (RADIO_BROWSER_EMPTY_ROOT) {
            parentId -> result.sendResult(ArrayList())
            else -> {
                if (radioResource?.status == Resource.Status.SUCCESS) {
                    val data = radioResource?.data
                    val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
                    if (data != null) {
                        for (metadata in data) {
                            mediaItems.add(MediaBrowserCompat.MediaItem(metadata.description,
                                MediaBrowserCompat.MediaItem.FLAG_PLAYABLE))
                        }
                    }
                    result.sendResult(mediaItems)
                } else {
                    result.sendResult(ArrayList())
                }
            }
        }
    }

    private inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()

            // 1. Audio Focus
            // todo: Always call requestFocus() first, proceed only if focus is granted.

            // 2. Service
            // todo: startService()

            // 3. Media Session
            mediaSession.isActive = true
            // todo: Update metadata and state

            // 4. Player related actions
            // todo: Start the player

            // 5. Becoming Noisy
            // todo: handle become noisy

            // 6. Notifications
            // todo: handle notifications
        }

        override fun onPause() {
            super.onPause()

            // 1. Media Session
            // todo: update metadata and state

            // 2. Player related actions
            // todo: Pause the player

            // 3. Becoming Noisy
            // todo: unregister receiver for becoming noisy

            // 4. Notifications
            stopForeground(false)
        }

        override fun onStop() {
            super.onStop()

            // 1. Audio Focus
            // todo: abandon audio focus

            // 2. Service
            stopSelf()

            // 3. Media Session
            mediaSession.isActive = false
            // todo: update metadata and state

            // 4. Player related actions
            // todo: Stop the player

            // 5. Notifications
            stopForeground(true)
        }
    }
}
