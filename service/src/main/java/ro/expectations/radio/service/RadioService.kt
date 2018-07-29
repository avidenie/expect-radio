package ro.expectations.radio.service

import android.app.PendingIntent
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.media.*
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.facebook.common.executors.UiThreadImmediateExecutorService
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequest
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.firebase.auth.FirebaseAuth
import ro.expectations.radio.common.Logger
import ro.expectations.radio.service.db.RadioEntity
import ro.expectations.radio.service.extensions.stateName
import ro.expectations.radio.service.mediasession.*


private const val TAG = "RadioService"
private const val EXPECT_RADIO_USER_AGENT = "Expect.Radio"

private const val RADIO_BROWSER_SERVICE_EMPTY_ROOT = "__EMPTY_ROOT__"
const val RADIO_BROWSER_SERVICE_ROOT = "__ROOT__"

class RadioService : LifecycleMediaBrowserService() {

    private lateinit var packageValidator: PackageValidator
    private lateinit var mediaSession : MediaSessionCompat
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private lateinit var playbackPreparer: PlaybackPreparer
    private lateinit var queueNavigator: QueueNavigator
    private lateinit var mediaSessionConnector: MediaSessionConnector

    private var isForegroundService = false

    private val audioAttributes = AudioAttributesCompat.Builder()
            .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributesCompat.USAGE_MEDIA)
            .build()

    private val exoPlayer: ExoPlayer by lazy {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        AudioFocusAwarePlayer(audioAttributes,
                audioManager,
                ExoPlayerFactory.newSimpleInstance(
                        DefaultRenderersFactory(this),
                        DefaultTrackSelector(),
                        DefaultLoadControl()))
    }

    override fun onCreate() {
        super.onCreate()
        packageValidator = PackageValidator(this)
        enforceAuth()
        initMediaSession()
    }

    override fun onGetRoot(clientPackageName: String, clientUid: Int, rootHints: Bundle?): BrowserRoot? {

        if (!packageValidator.isCallerAllowed(this, clientPackageName, clientUid)) {
            Logger.i(TAG, """
                    |OnGetRoot: Browsing NOT ALLOWED for unknown caller $clientPackageName.
                    |Returning empty browser root so all apps can use MediaController.
                    """.trimMargin())
             return MediaBrowserServiceCompat.BrowserRoot(RADIO_BROWSER_SERVICE_EMPTY_ROOT, null)
        }

        return MediaBrowserServiceCompat.BrowserRoot(RADIO_BROWSER_SERVICE_ROOT, null)
    }

    override fun onLoadChildren(parentId: String, result: Result<List<MediaBrowserCompat.MediaItem>>) {
        when (RADIO_BROWSER_SERVICE_EMPTY_ROOT) {
            parentId -> result.sendResult(arrayListOf())
            else -> {
                result.detach()

                val model = ServiceLocator.instance(this).getModel()
                model.radios.observe(this, object: Observer<PagedList<RadioEntity>> {
                    override fun onChanged(radios: PagedList<RadioEntity>?) {
                        if (radios != null) {
                            if (radios.isNotEmpty()) {
                                val mediaItems = radios.map {
                                    val description = MediaDescriptionCompat.Builder()
                                            .setMediaId(it.id)
                                            .setTitle(it.name)
                                            .setSubtitle(it.slogan)
                                            .setIconUri(Uri.parse(it.logo))
                                            .setMediaUri(Uri.parse(it.source))
                                            .build()
                                    MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_PLAYABLE)
                                }
                                result.sendResult(mediaItems)
                            } else {
                                result.sendResult(arrayListOf())
                            }
                        } else {
                            result.sendResult(arrayListOf())
                        }
                        model.radios.removeObserver(this)
                    }
                })
            }
        }
    }

    private fun enforceAuth() {
        val firebaseAuth = FirebaseAuth.getInstance()
        if (firebaseAuth.currentUser == null) {
            firebaseAuth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    notifyChildrenChanged(RADIO_BROWSER_SERVICE_ROOT)
                } else {
                    Logger.e(TAG, task.exception as Throwable, "Firebase signInAnonymously failure")
                }
            }
        }
    }

    private fun initMediaSession() {

        // Build a PendingIntent that can be used to launch the UI.
        val sessionIntent = packageManager?.getLaunchIntentForPackage(packageName)
        val sessionActivityPendingIntent = PendingIntent.getActivity(this, 0, sessionIntent, 0)

        // Create a new media session.
        mediaSession = MediaSessionCompat(this, TAG)
                .apply {
                    setSessionActivity(sessionActivityPendingIntent)
                    isActive = true
                }

        // Set the session's token so that client activities can communicate with it.
        sessionToken = mediaSession.sessionToken

        // Receiver for handling the becoming noisy event.
        becomingNoisyReceiver =
                BecomingNoisyReceiver(this, mediaSession.sessionToken)

        // Set up notifications.
        notificationHelper = NotificationHelper(this)
        notificationManager = NotificationManagerCompat.from(this)

        // Register the media session callback
        mediaSession.controller.registerCallback(MediaControllerCallback())

        // Connect the media session and the player.
        mediaSessionConnector = MediaSessionConnector(mediaSession).also {
            // Produces DataSource instances through which media data is loaded.
            val dataSourceFactory = DefaultDataSourceFactory(
                    this, Util.getUserAgent(this, EXPECT_RADIO_USER_AGENT), null)

            // Create the PlaybackPreparer of the media session connector.
            playbackPreparer = PlaybackPreparer(
                    this,
                    ServiceLocator.instance(this).getRepository(),
                    exoPlayer,
                    dataSourceFactory)
            it.setPlayer(exoPlayer, playbackPreparer)

            // Set up queue navigator
            queueNavigator = QueueNavigator(mediaSession)
            it.setQueueNavigator(queueNavigator)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {

            val updatedState = state?.state ?: return

            Logger.e(TAG, "RadioService::onPlaybackStateChanged => ${state.stateName}")

            val description = mediaSession.controller.metadata.description

            val imageRequest = ImageRequest.fromUri(description.iconUri)
            val imagePipeline = Fresco.getImagePipeline()
            val dataSource = imagePipeline.fetchDecodedImage(imageRequest, null)
            dataSource.subscribe(object:BaseBitmapDataSubscriber() {
                    override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                        processUpdatedState(updatedState, null)
                    }
                    override fun onNewResultImpl(bitmap: Bitmap?) {
                        processUpdatedState(updatedState, bitmap)
                    }
                }, UiThreadImmediateExecutorService.getInstance())
        }

        private fun processUpdatedState(updatedState: Int, bitmap: Bitmap?) {
            when (updatedState) {
                PlaybackStateCompat.STATE_BUFFERING,
                PlaybackStateCompat.STATE_PLAYING -> {
                    becomingNoisyReceiver.register()

                    startForeground(NOW_PLAYING_NOTIFICATION_ID,
                            notificationHelper.createNotification(mediaSession.sessionToken, bitmap))
                    isForegroundService = true
                }
                PlaybackStateCompat.STATE_NONE,
                PlaybackStateCompat.STATE_ERROR,
                PlaybackStateCompat.STATE_STOPPED -> {
                    becomingNoisyReceiver.unregister()

                    if (isForegroundService) {
                        stopForeground(true)
                        isForegroundService = false
                    }
                }
                else -> {
                    becomingNoisyReceiver.unregister()

                    if (isForegroundService) {
                        stopForeground(false)
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION_ID,
                                notificationHelper.createNotification(mediaSession.sessionToken, bitmap))
                        isForegroundService = false
                    }
                }
            }
        }
    }
}

private class BecomingNoisyReceiver(private val context: Context,
                                    sessionToken: MediaSessionCompat.Token)
    : BroadcastReceiver() {

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
