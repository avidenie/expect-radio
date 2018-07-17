package ro.expectations.radio.service

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import ro.expectations.radio.common.Logger


class QueueNavigator(mediaSession: MediaSessionCompat) : TimelineQueueNavigator(mediaSession) {

    private val window = Timeline.Window()

    override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat? {
        val tag = player.currentTimeline.getWindow(windowIndex, window, true).tag
        return if (tag != null) {
            tag as MediaDescriptionCompat
        } else {
            MediaDescriptionCompat.Builder().build()
        }
    }
}

