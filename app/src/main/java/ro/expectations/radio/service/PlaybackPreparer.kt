package ro.expectations.radio.service

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.PlaybackPreparer.ACTIONS
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import ro.expectations.radio.utilities.Logger

private const val TAG = "PlaybackPreparer"

class PlaybackPreparer(
        private val exoPlayer: ExoPlayer,
        private val dataSourceFactory: DataSource.Factory
) : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long = ACTIONS

    override fun onPrepare() {
        Logger.e(TAG, "onPrepare")
    }

    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        Logger.e(TAG, "onPrepareFromMediaId: $mediaId")
    }

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) {
        Logger.e(TAG, "onPrepareFromSearch: $query")
    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) {
        Logger.e(TAG, "onPrepareFromSearch: $uri")
    }

    override fun getCommands(): Array<String>? = null

    override fun onCommand(player: Player?, command: String?, extras: Bundle?, cb: ResultReceiver?) = Unit
}
