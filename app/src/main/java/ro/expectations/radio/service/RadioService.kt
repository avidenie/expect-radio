package ro.expectations.radio.service

import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserServiceCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import ro.expectations.radio.model.RadioProvider
import ro.expectations.radio.model.RadioStation
import ro.expectations.radio.model.Resource
import ro.expectations.radio.service.notification.MediaNotificationManager
import ro.expectations.radio.service.playback.LocalPlayback
import ro.expectations.radio.service.playback.Playback
import ro.expectations.radio.utilities.Logger


class RadioService : LifecycleMediaBrowserService() {

    companion object {
        private const val TAG = "RadioService"
        const val RADIO_BROWSER_EMPTY_ROOT = "__EMPTY_ROOT__"
        const val RADIO_BROWSER_ROOT = "__ROOT__"
    }

    private lateinit var packageValidator: PackageValidator
    private lateinit var radioProvider: RadioProvider
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var playbackStateBuilder : PlaybackStateCompat.Builder
    private lateinit var mediaNotificationManager: MediaNotificationManager
    private lateinit var playback: Playback

    private var radioResource : Resource<List<RadioStation>>? = null

    override fun onCreate() {
        super.onCreate()

        packageValidator = PackageValidator(this)
        playback = LocalPlayback(this)

        initMediaLibrary()
        initMediaSession()

        mediaNotificationManager = MediaNotificationManager(this)
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
                    val mediaItems = ArrayList<MediaBrowserCompat.MediaItem>()
                    val resourceData = radioResource?.data
                    if (resourceData != null) {
                        for (radio in resourceData) {
                            val description = MediaDescriptionCompat.Builder()
                                    .setMediaId(radio.id)
                                    .setTitle(radio.name)
                                    .setSubtitle(radio.slogan)
                                    .setIconUri(Uri.parse(radio.logo))
                                    .build()
                            mediaItems.add(MediaBrowserCompat.MediaItem(description,
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

    private fun initMediaLibrary() {

        radioProvider = RadioProvider(FirebaseFirestore.getInstance())

        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null) {
            firebaseAuth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onAuthenticated()
                } else {
                    Logger.e(TAG, task.exception as Throwable, "Firebase signInAnonymously failure")
                }
            }
        } else {
            onAuthenticated()
        }
    }

    private fun onAuthenticated() {
        radioProvider.radios.observe(this, Observer { resource ->
            radioResource = resource
            notifyChildrenChanged(RADIO_BROWSER_ROOT)
        })
    }

    private fun initMediaSession() {

        // Create a new media session
        mediaSession = MediaSessionCompat(this, TAG)

        // Handle media button events and transport control commands.
        mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS)

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        playbackStateBuilder = PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY or
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)
        mediaSession.setPlaybackState(playbackStateBuilder.build())

        // Handle callbacks from a media controller
        mediaSession.setCallback(MediaSessionCallback())

        // Set the session's token so that client activities can communicate with it.
        sessionToken = mediaSession.sessionToken
    }

    override fun onDestroy() {
        super.onDestroy()

        Logger.d(TAG, "onDestroy")
    }

    fun onPlaybackStart() {
        mediaSession.isActive = false
        startService(Intent(this@RadioService, RadioService::class.java))
    }

    fun onPlaybackStop() {
        mediaSession.isActive = false
        stopSelf()
    }

    fun onPlaybackStateUpdated(newState: PlaybackStateCompat) {
        mediaSession.setPlaybackState(newState)
    }

    fun onMetadataUpdated(mediaMetadata: MediaMetadataCompat) {
        mediaSession.setMetadata(mediaMetadata)
    }

    inner class MediaSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            super.onPlay()

            Logger.d(TAG, "onPlay")

            enforceQueue()

            mediaSession.controller.queue.size == 0 && return

            val queueItem = mediaSession.controller.queue[0]

            playback.play(queueItem)
        }

        override fun onPause() {
            super.onPause()

            Logger.d(TAG, "onPause")

            playback.pause()
        }

        override fun onStop() {
            super.onStop()

            Logger.d(TAG, "onStop")

            playback.stop()
        }
    }

    private fun enforceQueue() {

        // If a queue is already set, we don't have to do anything.
        mediaSession.controller.queue != null && mediaSession.controller.queue.size > 0 && return

        val queue = mutableListOf<MediaSessionCompat.QueueItem>()
        if (radioResource?.status == Resource.Status.SUCCESS) {
            val resourceData = radioResource?.data
            if (resourceData != null) {
                var index = 0L
                for (radio in resourceData) {
                    val extras = Bundle()
                    extras.putString("__SOURCE__", radio.source)
                    val description = MediaDescriptionCompat.Builder()
                            .setMediaId(radio.id)
                            .setTitle(radio.name)
                            .setSubtitle(radio.slogan)
                            .setExtras(extras)
                            .build()
                    val queueItem = MediaSessionCompat.QueueItem(description, index++)
                    queue.add(queueItem)
                }
            }
        }

        mediaSession.setQueue(queue)
    }
}
