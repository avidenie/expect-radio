package ro.expectations.radio.media.playback

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.EventLogger
import com.google.android.exoplayer2.util.Util
import mu.KotlinLogging
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import org.slf4j.impl.HandroidLoggerAdapter
import ro.expectations.radio.media.BuildConfig
import ro.expectations.radio.media.browser.MediaBrowser
import ro.expectations.radio.media.extensions.stateName

private val logger = KotlinLogging.logger {}

class PlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationBuilder

    private var isForegroundService = false

    private val player: ExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this).apply {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(C.CONTENT_TYPE_MUSIC)
                .setUsage(C.USAGE_MEDIA)
                .build()
            setAudioAttributes(audioAttributes, true)
            if (BuildConfig.DEBUG) {
                addAnalyticsListener(EventLogger(DefaultTrackSelector()))
            }
        }
    }

    // Set up the queue manager, used by both the playback preparer and the queue navigator
    private val queueManager: QueueManager by lazy {
        val userAgent = Util.getUserAgent(this, "Expect Radio")
        val dataSourceFactory = DefaultDataSourceFactory(this, userAgent, null)
        QueueManager(player, dataSourceFactory)
    }


    private val mediaBrowser: MediaBrowser by inject()

    override fun onCreate() {
        super.onCreate()

        // Set up logging
        HandroidLoggerAdapter.DEBUG = BuildConfig.DEBUG
        HandroidLoggerAdapter.APP_NAME = "ExR"

        // Build a PendingIntent that can be used to launch the UI.
        val sessionIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0)

        // Create a new media session.
        mediaSession = MediaSessionCompat(this, "Playback Service").apply {
            setSessionActivity(sessionActivityPendingIntent)
            isActive = true
        }

        // Set the session's token so that client activities can communicate with it.
        sessionToken = mediaSession.sessionToken

        // Initialise notifications.
        notificationBuilder = NotificationBuilder(this)
        notificationManager = NotificationManagerCompat.from(this)

        // Receiver for handling the becoming noisy event.
        becomingNoisyReceiver =
            BecomingNoisyReceiver(this, mediaSession.sessionToken)

        // Register a session controller callback to receive updates from the session.
        mediaSession.controller.registerCallback(MediaControllerCallback())

        // Initialise the ExoPlayer's media session connector component, which will manage the media session.
        mediaSessionConnector = MediaSessionConnector(mediaSession).also {

            // Initialise and set the player
            it.setPlayer(player)

            // Set the playback preparer
            val playbackPreparer: PlaybackPreparer = get { parametersOf(queueManager) }
            it.setPlaybackPreparer(playbackPreparer)

            // Set the default queue navigator
            it.setQueueNavigator(QueueNavigator(mediaSession, queueManager))
        }
    }

    /**
     * This method is called when swiping the application away from recents.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        logger.debug { "onTaskRemoved" }

        /**
         * By stopping playback, the player will transition to [Player.STATE_IDLE]. This will
         * cause a state change in the MediaSession, and (most importantly) call
         * [MediaControllerCallback.onPlaybackStateChanged]. Because the playback state will
         * be reported as [PlaybackStateCompat.STATE_NONE], the service will first remove
         * itself as a foreground service, and will then call [stopSelf].
         */
        player.stop(true)
    }

    override fun onDestroy() {

        logger.debug { "onDestroy" }

        mediaSession.run {
            isActive = false
            release()
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {

        logger.debug { "onGetRoot: $clientPackageName, $clientUid, $rootHints" }

        return mediaBrowser.getRoot()
    }

    override fun onLoadChildren(parentId: String, result: Result<MutableList<MediaItem>>) {

        logger.debug { "onLoadChildren: $parentId" }

        result.detach()

        mediaBrowser.loadChildren(parentId)
            .addOnSuccessListener { result.sendResult(it.toMutableList()) }
            .addOnFailureListener { result.sendResult(null) }
    }

    /**
     * Class to receive callbacks about state changes to the [MediaSessionCompat].
     */
    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)

            logger.debug { "MediaControllerCallback::onMetadataChanged: ${metadata?.description}" }

            if (metadata != null) {
                if (metadata.description.iconBitmap == null) {
                    logger.error { "iconUri: ${metadata.description.iconUri}" }
                    Glide.with(applicationContext)
                        .asBitmap()
                        .load(metadata.description.iconUri)
                        .into(object : CustomTarget<Bitmap>() {
                            override fun onLoadCleared(placeholder: Drawable?) {
                            }
                            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                                queueManager.updateMetadata(
                                    MediaMetadataCompat.Builder(metadata)
                                        .putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, resource)
                                        .build()
                                )
                                mediaSessionConnector.invalidateMediaSessionQueue()
                                mediaSessionConnector.invalidateMediaSessionMetadata()
                            }
                        })
                }
                mediaSession.controller.playbackState?.let { updateNotification(it) }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)

            logger.debug { "MediaControllerCallback::onPlaybackStateChanged: ${state?.stateName} -> $state" }

            state?.let { updateNotification(it) }
        }

        private fun updateNotification(state: PlaybackStateCompat) {

            when (state.state) {
                PlaybackStateCompat.STATE_CONNECTING,
                PlaybackStateCompat.STATE_BUFFERING,
                PlaybackStateCompat.STATE_PLAYING -> {
                    becomingNoisyReceiver.register()

                    val notification = notificationBuilder.buildNotification(mediaSession.sessionToken)

                    if (!isForegroundService) {
                        startService(Intent(applicationContext, this@PlaybackService.javaClass))
                        startForeground(NOW_PLAYING_NOTIFICATION, notification)
                        isForegroundService = true
                    } else {
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                    }
                }
                PlaybackStateCompat.STATE_NONE -> {
                    becomingNoisyReceiver.unregister()

                    stopForeground(true)
                    isForegroundService = false

                    stopSelf()
                }
                else -> {
                    becomingNoisyReceiver.unregister()

                    if (isForegroundService) {

                        stopForeground(false)
                        isForegroundService = false

                        val notification = notificationBuilder.buildNotification(mediaSession.sessionToken)
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                    }
                }
            }
        }
    }
}

/**
 * Helper class for listening for when headphones are unplugged (or the audio
 * will otherwise cause playback to become "noisy").
 */
private class BecomingNoisyReceiver(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token
) : BroadcastReceiver() {

    private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val controller = MediaControllerCompat(context, sessionToken)

    private var registered = false

    fun register() {
        if (!registered) {
            context.registerReceiver(this, noisyIntentFilter)
            registered = true
        }
    }

    fun unregister() {
        if (registered) {
            context.unregisterReceiver(this)
            registered = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            controller.transportControls.pause()
        }
    }
}

