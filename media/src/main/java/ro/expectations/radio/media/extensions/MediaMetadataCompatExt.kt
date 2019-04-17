package ro.expectations.radio.media.extensions

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.Flags
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource

/**
 * Useful extensions for [MediaMetadataCompat].
 */

inline val MediaMetadataCompat.mediaId: String
    get() = this.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)

inline val MediaMetadataCompat.mediaUri: Uri?
    get() = Uri.parse(this.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI))

/**
 * Custom property for retrieving a [MediaDescriptionCompat] which also includes
 * all of the keys from the [MediaMetadataCompat] object in its extras.
 *
 * These keys are used by the ExoPlayer MediaSession extension when announcing metadata changes.
 */
inline val MediaMetadataCompat.fullDescription: MediaDescriptionCompat
    get() =
        description.also {
            it.extras?.putAll(bundle)
        }

/**
 * Extension method for building a [ProgressiveMediaSource] from a [MediaMetadataCompat] object.
 *
 * For convenience, place the [MediaDescriptionCompat] into the tag so it can be retrieved later.
 */
fun MediaMetadataCompat.toMediaSource(dataSourceFactory: DataSource.Factory): ProgressiveMediaSource? =
    ProgressiveMediaSource.Factory(dataSourceFactory)
        .setTag(mediaId)
        .createMediaSource(mediaUri)

/**
 * Extension method for building a [ConcatenatingMediaSource] given a [List]
 * of [MediaMetadataCompat] objects.
 */
fun List<MediaMetadataCompat>.toMediaSource(dataSourceFactory: DataSource.Factory): ConcatenatingMediaSource {

    val concatenatingMediaSource = ConcatenatingMediaSource()
    forEach {
        concatenatingMediaSource.addMediaSource(it.toMediaSource(dataSourceFactory))
    }
    return concatenatingMediaSource
}

/**
 * Extension method for building a [MediaBrowserCompat.MediaItem] from a [MediaMetadataCompat] object.
 */
fun MediaMetadataCompat.toMediaItem(@Flags flags: Int = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE) =
    MediaBrowserCompat.MediaItem(description, flags)

/**
 * Extension method for building a list of [MediaBrowserCompat.MediaItem] given a [List]
 * of [MediaMetadataCompat] objects.
 */
fun List<MediaMetadataCompat>.toMediaItem(@Flags flags: Int = MediaBrowserCompat.MediaItem.FLAG_PLAYABLE) =
    map {
        it.toMediaItem(flags)
    }
