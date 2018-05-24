package ro.expectations.radio.service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.RemoteException
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaButtonReceiver
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import ro.expectations.radio.R
import ro.expectations.radio.service.RadioService
import ro.expectations.radio.service.playback.LocalPlayback
import ro.expectations.radio.ui.HomeActivity
import ro.expectations.radio.utilities.Logger


class MediaNotificationManager(private val radioService: RadioService) {

    companion object {
        private val TAG = MediaNotificationManager::class.java.simpleName

        private const val CHANNEL_ID = "ro.expectations.radio.notifications.channel"
        const val NOTIFICATION_ID = 412
        private const val REQUEST_CODE = 501
    }

    private val playAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_play_arrow_black_24dp,
            radioService.getString(R.string.label_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    radioService,
                    PlaybackStateCompat.ACTION_PLAY))
    private val pauseAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_pause_black_24dp,
            radioService.getString(R.string.label_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    radioService,
                    PlaybackStateCompat.ACTION_PAUSE))
    private val nextAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_skip_next_black_24dp,
            radioService.getString(R.string.label_next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    radioService,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
    private val previousAction: NotificationCompat.Action = NotificationCompat.Action(
            R.drawable.ic_skip_previous_black_24dp,
            radioService.getString(R.string.label_previous),
            MediaButtonReceiver.buildMediaButtonPendingIntent(
                    radioService,
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))

    private val stopIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(
            radioService,
            PlaybackStateCompat.ACTION_STOP)

    private val notificationManager = radioService.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager

    private val mediaControllerCallback = MediaControllerCallback()

    private var mediaSessionToken: MediaSessionCompat.Token? = null
    private var mediaController: MediaControllerCompat? = null
    private var playbackState: PlaybackStateCompat? = null
    private var mediaMetadata: MediaMetadataCompat? = null

    init {
        updateSessionToken()

        mediaMetadata = mediaController?.metadata
        playbackState = mediaController?.playbackState

        // Cancel all notifications to handle the case where the service was killed and
        // restarted by the system.
        notificationManager.cancelAll()
    }

    private fun updateSessionToken() {
        val freshToken = radioService.sessionToken
        if (mediaSessionToken == null && freshToken != null || mediaSessionToken != null && mediaSessionToken != freshToken) {
            mediaController?.unregisterCallback(mediaControllerCallback)
            mediaSessionToken = freshToken
            if (freshToken != null) {
                mediaController = MediaControllerCompat(radioService, freshToken)
                mediaController?.registerCallback(mediaControllerCallback)
            }
        }
    }

    private fun createNotification(): Notification? {

        if (mediaMetadata == null || playbackState == null) {
            Logger.d(TAG, "createNotification returned NULL")
            return null
        }

        val description = mediaMetadata?.description

        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        val notificationBuilder = NotificationCompat.Builder(radioService, CHANNEL_ID)

        val playPauseButtonPosition = addActions(notificationBuilder)

        notificationBuilder
                .setStyle(android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(playPauseButtonPosition)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(stopIntent)
                        .setMediaSession(mediaSessionToken))
                .setDeleteIntent(stopIntent)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent())
                .setContentTitle(description?.title)
                .setContentText(description?.subtitle)

        return notificationBuilder.build()
    }

    private fun addActions(notificationBuilder: NotificationCompat.Builder): Int {
        Logger.d(TAG, "updatePlayPauseAction")

        var playPauseButtonPosition = 0

        // If skip to previous action is enabled
        if (playbackState?.actions?.and(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS) != 0L) {
            notificationBuilder.addAction(previousAction)
            playPauseButtonPosition = 1
        }

        // Play or pause button, depending on the current state.
        val isPlaying = playbackState?.state == PlaybackStateCompat.STATE_PLAYING
        notificationBuilder.addAction(if (isPlaying) pauseAction else playAction)

        // If skip to next action is enabled
        if (playbackState?.actions?.and(PlaybackStateCompat.ACTION_SKIP_TO_NEXT) != 0L) {
            notificationBuilder.addAction(nextAction)
        }

        return playPauseButtonPosition
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                    "Notifications",
                    NotificationManager.IMPORTANCE_LOW)

            notificationChannel.description = "Notifications for radio playback"

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createContentIntent(): PendingIntent {
        val openUI = Intent(radioService, HomeActivity::class.java)
        openUI.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        return PendingIntent.getActivity(
                radioService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat) {
            Logger.e(TAG, "onPlayerStateChanged => playbackState: $state")
            playbackState = state
            when(state.state) {
                PlaybackStateCompat.STATE_PLAYING,
                PlaybackStateCompat.STATE_PAUSED -> {
                    val notification = createNotification()
                    if (notification != null) {
                        radioService.startForeground(NOTIFICATION_ID, notification)
                    }
                    if (state.state == PlaybackStateCompat.STATE_PAUSED) {
                        radioService.stopForeground(false)
                    }
                }
                PlaybackStateCompat.STATE_NONE,
                PlaybackStateCompat.STATE_STOPPED -> {
                    radioService.stopForeground(true)
                }
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaMetadata = metadata
            val notification = createNotification()
            if (notification != null) {
                notificationManager.notify(NOTIFICATION_ID, notification)
            }
        }

        override fun onSessionDestroyed() {
            super.onSessionDestroyed()
            Logger.d(TAG, "Session was destroyed, resetting to the new session token")
            try {
                updateSessionToken()
            } catch (e: RemoteException) {
                Logger.e(TAG, e, "could not connect media controller")
            }
        }
    }
}