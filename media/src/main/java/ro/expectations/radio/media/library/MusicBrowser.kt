package ro.expectations.radio.media.library

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.firebase.firestore.FirebaseFirestore

class MusicBrowser(private val db: FirebaseFirestore) {

    private val rootId = "__music__"

    fun getRoot(): MediaBrowserCompat.MediaItem {

        val description = MediaDescriptionCompat.Builder()
            .setMediaId(rootId)
            .setTitle("Music")
            .build()

        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    fun canLoadChildren(parentId: String): Boolean = parentId.startsWith(rootId, true)

    fun onLoadChildren(
        parentId: String,
        result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        return when (parentId) {
            rootId -> onGetMusic(result)
            else -> result.sendResult(null)
        }
    }

    private fun onGetMusic(result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(mutableListOf())
    }
}
