package ro.expectations.radio.media

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.PlaybackPreparer.ACTIONS
import com.google.android.exoplayer2.upstream.DataSource
import mu.KotlinLogging
import ro.expectations.radio.media.extensions.toMediaSource
import ro.expectations.radio.media.library.MediaBrowser

private val logger = KotlinLogging.logger {}

class PlaybackPreparer(private val mediaBrowser: MediaBrowser,
                       private val exoPlayer: ExoPlayer,
                       private val dataSourceFactory: DataSource.Factory)
    : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long {
        logger.debug { "getSupportedPrepareActions" }

        return ACTIONS
    }

    override fun onPrepare() = Unit

    override fun onPrepareFromMediaId(mediaId: String, extras: Bundle?) {
        logger.debug { "onPrepareFromMediaId -> mediaId: $mediaId, extras: $extras" }

        mediaBrowser.getRadio(mediaId)
            .addOnSuccessListener { current ->
                mediaBrowser.getRadios()
                    .addOnSuccessListener { radios ->

                        var currentWindowIndex = radios?.indexOfFirst {
                            current.getText(METADATA_KEY_MEDIA_ID) == it.getText(METADATA_KEY_MEDIA_ID)
                        }
                        if (currentWindowIndex == null || currentWindowIndex == -1) {
                            currentWindowIndex = 0
                        }

                        exoPlayer.prepare(radios?.toMediaSource(dataSourceFactory))
                        exoPlayer.seekTo(currentWindowIndex, 0)
                    }
                    .addOnFailureListener {

                        exoPlayer.prepare(listOf(current).toMediaSource(dataSourceFactory))
                        exoPlayer.seekTo(0, 0)
                    }
            }
            .addOnFailureListener {
                // todo: notify caller of the error
                logger.error(it) { "Failed preparing from media ID $mediaId: $it" }
            }
    }

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) = Unit

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) = Unit

    override fun onCommand(
        player: Player?,
        controlDispatcher: ControlDispatcher?,
        command: String?,
        extras: Bundle?,
        cb: ResultReceiver?
    ) = false
}