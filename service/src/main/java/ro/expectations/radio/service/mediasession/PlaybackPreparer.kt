package ro.expectations.radio.service.mediasession

import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.Observer
import android.arch.paging.PagedList
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Util
import ro.expectations.radio.common.Logger
import ro.expectations.radio.service.db.RadioEntity
import ro.expectations.radio.service.repository.RadioRepository


class PlaybackPreparer(
        private val lifecycleOwner: LifecycleOwner,
        private val repository: RadioRepository,
        private val exoPlayer: ExoPlayer,
        private val dataSourceFactory: DataSource.Factory
) : MediaSessionConnector.PlaybackPreparer {

    override fun getSupportedPrepareActions(): Long =
            PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                    PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID

    override fun onPrepare() = Unit

    override fun onPrepareFromMediaId(mediaId: String, extras: Bundle?) {

        val itemToPlayLive = repository.radioById(mediaId)
        itemToPlayLive.observe(lifecycleOwner, object: Observer<RadioEntity?> {
            override fun onChanged(itemToPlay: RadioEntity?) {

                if (itemToPlay == null) {
                    Logger.w(TAG, "Content not found: mediaID = $mediaId")
                } else {

                    val radioList = repository.radios(30).pagedList

                    radioList.observe(lifecycleOwner, object: Observer<PagedList<RadioEntity>> {
                        override fun onChanged(radios: PagedList<RadioEntity>?) {
                            if (radios != null) {
                                if (radios.isNotEmpty()) {

                                    val mediaSource = ConcatenatingMediaSource()
                                    for(radio in radios) {

                                        val metadata = MediaMetadataCompat.Builder().apply {
                                            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, radio.id)
                                            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, radio.name)
                                            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, radio.slogan)
                                            putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, radio.logo)
                                            putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, radio.source)
                                        }.build()

                                        mediaSource.addMediaSource(buildMediaSource(metadata, dataSourceFactory))
                                    }
                                    exoPlayer.prepare(mediaSource)

                                    val initialWindowIndex = radios.indexOf(itemToPlay)
                                    exoPlayer.seekTo(initialWindowIndex, 0)

                                    radioList.removeObserver(this)
                                }
                            }
                        }
                    })
                }

                itemToPlayLive.removeObserver(this)
            }
        })


    }

    private fun buildMediaSource(metadata: MediaMetadataCompat, dataSourceFactory: DataSource.Factory): MediaSource {
        val uri = metadata.description.mediaUri
        val type = Util.inferContentType(uri)
        val tag = metadata.description.also {
            it.extras?.putAll(metadata.bundle)
        }
        return when (type) {
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory)
                    .setTag(tag)
                    .createMediaSource(uri)
            C.TYPE_OTHER -> ExtractorMediaSource.Factory(dataSourceFactory)
                    .setTag(tag)
                    .createMediaSource(uri)
            else -> {
                throw IllegalStateException("Unsupported media URI type: $type")
            }
        }
    }

    override fun onPrepareFromSearch(query: String?, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) = Unit

    override fun getCommands(): Array<String>? = null

    override fun onCommand(player: Player?, command: String?, extras: Bundle?, cb: ResultReceiver?) = Unit
}

private const val TAG = "PlaybackPreparer"

