package ro.expectations.radio.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media.session.MediaButtonReceiver
import ro.expectations.radio.media.extensions.isPlayEnabled
import ro.expectations.radio.media.extensions.isPlaying
import ro.expectations.radio.media.extensions.isSkipToNextEnabled
import ro.expectations.radio.media.extensions.isSkipToPreviousEnabled

private const val NOW_PLAYING_CHANNEL: String = "ro.expectations.radio.media.NOW_PLAYING"
const val NOW_PLAYING_NOTIFICATION: Int = 0xb095

/**
 * Helper class to encapsulate code for building notifications.
 */
class NotificationBuilder(private val context: Context) {

    private val platformNotificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val skipToPreviousAction = NotificationCompat.Action(
            R.drawable.ic_skip_previous_black_24dp,
            context.getString(R.string.notification_skip_to_previous),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_SKIP_TO_PREVIOUS))
    private val playAction = NotificationCompat.Action(
            R.drawable.ic_play_arrow_black_24dp,
            context.getString(R.string.notification_play),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_PLAY))
    private val pauseAction = NotificationCompat.Action(
            R.drawable.ic_pause_black_24dp,
            context.getString(R.string.notification_pause),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_PAUSE))
    private val skipToNextAction = NotificationCompat.Action(
            R.drawable.ic_skip_next_black_24dp,
            context.getString(R.string.notification_skip_to_next),
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_SKIP_TO_NEXT))

    private val stopPendingIntent =
            MediaButtonReceiver.buildMediaButtonPendingIntent(context, ACTION_STOP)

    fun buildNotification(sessionToken: MediaSessionCompat.Token): Notification {

        if (shouldCreateNowPlayingChannel()) {
            createNowPlayingChannel()
        }

        val controller = MediaControllerCompat(context, sessionToken)
        val description = controller.metadata?.description ?: MediaDescriptionCompat.Builder()
                .setSubtitle(context.getString(R.string.notification_connecting_to_media))
                .build()
        val playbackState = controller.playbackState

        val builder = NotificationCompat.Builder(context, NOW_PLAYING_CHANNEL)

        // Only add actions for skip back, play/pause, skip forward, based on what's enabled.
        var playPauseIndex = 0
        if (playbackState.isSkipToPreviousEnabled) {
            builder.addAction(skipToPreviousAction)
            ++playPauseIndex
        }
        if (playbackState.isPlaying) {
            builder.addAction(pauseAction)
        } else if (playbackState.isPlayEnabled) {
            builder.addAction(playAction)
        }
        if (playbackState.isSkipToNextEnabled) {
            builder.addAction(skipToNextAction)
        }

        val mediaStyle = MediaStyle()
                .setMediaSession(sessionToken)
                .setShowActionsInCompactView(playPauseIndex)

        return builder.setContentIntent(controller.sessionActivity)
                .setContentTitle(description.title)
                .setContentText(description.subtitle)
                .setSmallIcon(R.drawable.ic_stat_notify_media)
                .setLargeIcon(description.iconBitmap)
                .setOnlyAlertOnce(true)
                .setStyle(mediaStyle)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(stopPendingIntent)
                .build()
    }

    private fun shouldCreateNowPlayingChannel() =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !nowPlayingChannelExists()

    @RequiresApi(Build.VERSION_CODES.O)
    private fun nowPlayingChannelExists() =
            platformNotificationManager.getNotificationChannel(NOW_PLAYING_CHANNEL) != null

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNowPlayingChannel() {
        val notificationChannel = NotificationChannel(NOW_PLAYING_CHANNEL,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW)
                .apply {
                    description = context.getString(R.string.notification_channel_description)
                }

        platformNotificationManager.createNotificationChannel(notificationChannel)
    }
}

