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
import mu.KLogging
import ro.expectations.radio.media.extensions.toMediaSource

class PlaybackPreparer(private val exoPlayer: ExoPlayer,
                       private val dataSourceFactory: DataSource.Factory)
    : MediaSessionConnector.PlaybackPreparer {

    companion object : KLogging()

    override fun getSupportedPrepareActions(): Long {
        logger.info { "getSupportedPrepareActions" }

        return ACTIONS
    }

    override fun onPrepare() = Unit

    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        logger.info { "onPrepareFromMediaId -> mediaId: $mediaId, extras: $extras" }

        val metadataList = listOf(MediaMetadataCompat.Builder()
            .putText(METADATA_KEY_MEDIA_ID, mediaId)
            .putText(METADATA_KEY_DISPLAY_TITLE, "Europa FM")
            .putText(METADATA_KEY_DISPLAY_SUBTITLE, "Pe aceeași frecvență cu tine!")
            .putText(METADATA_KEY_DISPLAY_DESCRIPTION, "Ascultă online Europa FM și află primul știrile care contează și relaxează-te cu cea mai bună muzică!")
            .putText(METADATA_KEY_DISPLAY_ICON_URI, "https://storage.googleapis.com/expect-radio.appspot.com/logos%2Fum7shzY1bGNuuzA9ZJoD.jpg?GoogleAccessId=firebase-adminsdk-j842w%40expect-radio.iam.gserviceaccount.com&Expires=16725225600&Signature=IxnMnxirZun8DG%2FFM0hwISt9XcNq5YfwQu7%2B8UNXGLyD4vud3p5lqw9ccs2qcGEKlMYrsQoKC2Y%2BWT1PEKczZJUcvJk3tfPwkuGLg2w1UGIRbrBjwGFYcCLL%2BdemJDo9W16KIZYtHWXcLQwoxcWf2npheo%2FSfAEemumvCwLNnxqxNNmrQc0eseNZgDKwTh0GLyhD0zj6WGB9sg3AU%2FxSL9XuKd972beRI7SuO8xAyCXdAKaFruCn9tGnUw0p2ihdAcRGpnTxls2fkeM%2F1QLxDkUm24jgF0rk3dZZDUyzES0%2FokLzAENkriRzoamwnbgOA2u5GQ2vrfpC%2BJJfquGFQQ%3D%3D")
            .putText(METADATA_KEY_MEDIA_URI, "http://astreaming.europafm.ro:8000/europafm_mp3_64k")
            .build()
        )
        val mediaSource = metadataList.toMediaSource(dataSourceFactory)

        exoPlayer.prepare(mediaSource)
        exoPlayer.seekTo(0, 0);
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