package ro.expectations.radio.media.browser

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import ro.expectations.radio.media.auth.AuthProvider
import ro.expectations.radio.media.extensions.toMediaItem
import ro.expectations.radio.media.repository.MusicRepository
import ro.expectations.radio.media.repository.PodcastRepository
import ro.expectations.radio.media.repository.RadioRepository

class MediaBrowser(
    private val authProvider: AuthProvider,
    private val radioRepository: RadioRepository,
    private val musicRepository: MusicRepository,
    private val podcastRepository: PodcastRepository
) {

    companion object {
        const val EMPTY_ROOT = "_empty_"
        const val BROWSABLE_ROOT = "_root_"
        const val RADIO_ROOT = "_radio_"
        const val PODCAST_ROOT = "_podcast_"
        const val MUSIC_ROOT = "_music_"
    }

    fun getRoot(): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(BROWSABLE_ROOT, null)
    }

    fun loadChildren(parentId: String): Task<List<MediaBrowserCompat.MediaItem>> =
        authProvider.whenAuthenticated().continueWithTask {
            when (parentId) {
                EMPTY_ROOT -> Tasks.forResult(listOf())
                BROWSABLE_ROOT -> Tasks.forResult(getTopMediaItems())
                RADIO_ROOT -> radioRepository.getRadios().continueWith { it.result?.toMediaItem() }
                PODCAST_ROOT -> podcastRepository.getPodcasts().continueWith { it.result?.toMediaItem() }
                MUSIC_ROOT -> musicRepository.getSongs().continueWith { it.result?.toMediaItem() }
                else -> throw RuntimeException("Invalid parent media item requested")
            }
        }

    private fun getTopMediaItems(): List<MediaBrowserCompat.MediaItem> =
        listOf(
            getBrowableMediaItem(RADIO_ROOT, "Radios"),
            getBrowableMediaItem(PODCAST_ROOT, "Podcasts"),
            getBrowableMediaItem(MUSIC_ROOT, "Music")
        )

    private fun getBrowableMediaItem(description: MediaDescriptionCompat): MediaBrowserCompat.MediaItem {
        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    private fun getBrowableMediaItem(parentId: String, name: String): MediaBrowserCompat.MediaItem =
        getBrowableMediaItem(
            MediaDescriptionCompat.Builder()
                .setMediaId(parentId)
                .setTitle(name)
                .build()
        )
}
