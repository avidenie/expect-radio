package ro.expectations.radio.media.playback

import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import com.google.android.exoplayer2.ControlDispatcher
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector.PlaybackPreparer.ACTIONS
import mu.KotlinLogging
import ro.expectations.radio.media.repository.MusicRepository
import ro.expectations.radio.media.repository.PodcastRepository
import ro.expectations.radio.media.repository.RadioRepository

private val logger = KotlinLogging.logger {}

class PlaybackPreparer(
    private val queueManager: QueueManager,
    private val radioRepository: RadioRepository,
    private val musicRepository: MusicRepository,
    private val podcastRepository: PodcastRepository
) : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long {
        logger.debug { "getSupportedPrepareActions" }

        return ACTIONS
    }

    override fun onPrepare() = Unit

    override fun onPrepareFromMediaId(mediaId: String, extras: Bundle?) {
        logger.debug { "onPrepareFromMediaId -> mediaId: $mediaId, extras: $extras" }

        radioRepository
            .getRadios()
            .addOnSuccessListener { radios ->
                queueManager.publishQueue(radios, mediaId)
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