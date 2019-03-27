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

    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        logger.debug { "onPrepareFromMediaId -> mediaId: $mediaId, extras: $extras" }

        mediaBrowser.getRadios()
            .addOnSuccessListener { radios ->

                logger.error { "got radios: $radios" }

                var initialWindowIndex = 0
                var currentIndex = 0

                val metadataList = radios.map {

                    logger.debug { "onPrepareFromMediaId -> preparing $it" }

                    currentIndex++
                    if (it.mediaId == mediaId) {
                        initialWindowIndex = currentIndex
                    }
                    MediaMetadataCompat.Builder()
                        .putText(METADATA_KEY_MEDIA_ID, it.mediaId)
                        .putText(METADATA_KEY_DISPLAY_TITLE, it.description.title)
                        .putText(METADATA_KEY_DISPLAY_SUBTITLE, it.description.subtitle)
                        .putText(METADATA_KEY_DISPLAY_ICON_URI, it.description.iconUri.toString())
                        .putText(METADATA_KEY_MEDIA_URI, it.description.mediaUri.toString())
                        .build()
                }

                val mediaSource = metadataList.toMediaSource(dataSourceFactory)

                exoPlayer.prepare(mediaSource)
                exoPlayer.seekTo(initialWindowIndex, 0)

            }
            .addOnFailureListener {

                logger.error(it) { "Failed loading radios: $it" }

                // todo: notify caller of the error
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