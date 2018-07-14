package ro.expectations.radio.service

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import ro.expectations.radio.common.Logger
import ro.expectations.radio.service.model.Resource


private const val TAG = "PlaybackPreparer"

class PlaybackPreparer(
        var radioResource: Resource<List<MediaBrowserCompat.MediaItem>>?,
        private val exoPlayer: ExoPlayer,
        private val dataSourceFactory: DataSource.Factory
) : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

    override fun onPrepare() = Unit

    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {

        if (radioResource?.status != Resource.Status.SUCCESS) {
            return
        }

        val itemToPlay = radioResource?.data?.find {
            it.mediaId == mediaId
        }

        if (itemToPlay == null) {
            Logger.w(TAG, "Content not found: mediaID = $mediaId")
            return
        }

        val mediaSource = ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(itemToPlay.description.mediaUri)
        exoPlayer.prepare(mediaSource)
    }

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) = Unit

    override fun getCommands(): Array<String>? = null

    override fun onCommand(player: Player?, command: String?, extras: Bundle?, cb: ResultReceiver?) = Unit
}
