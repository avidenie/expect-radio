package ro.expectations.radio.media.playback

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator

class QueueNavigator(mediaSession: MediaSessionCompat?) : TimelineQueueNavigator(mediaSession) {

    private val window = Timeline.Window()

    override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat =
        player?.currentTimeline?.getWindow(windowIndex, window, true)?.tag as MediaDescriptionCompat

    override fun getSupportedQueueNavigatorActions(player: Player): Long {

        var actions: Long = 0

        val timeline = player.currentTimeline
        if (!timeline.isEmpty && !player.isPlayingAd) {

            if (timeline.windowCount > 1) {
                actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_QUEUE_ITEM
            }

            if (player.hasPrevious()) {
                actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            }

            if (player.hasNext()) {
                actions = actions or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            }
        }

        return actions
    }

    override fun onSkipToPrevious(player: Player, controlDispatcher: ControlDispatcher) {
        val timeline = player.currentTimeline
        if (timeline.isEmpty || player.isPlayingAd) {
            return
        }
        val previousWindowIndex = player.previousWindowIndex
        if (previousWindowIndex != C.INDEX_UNSET) {
            controlDispatcher.dispatchSeekTo(player, previousWindowIndex, C.TIME_UNSET)
        }
    }

    override fun onSkipToNext(player: Player, controlDispatcher: ControlDispatcher) {
        val timeline = player.currentTimeline
        if (timeline.isEmpty || player.isPlayingAd) {
            return
        }
        val nextWindowIndex = player.nextWindowIndex
        if (nextWindowIndex != C.INDEX_UNSET) {
            controlDispatcher.dispatchSeekTo(player, nextWindowIndex, C.TIME_UNSET)
        }
    }
}