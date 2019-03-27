package ro.expectations.radio.media.library

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
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

    fun loadChildren(parentId: String) : Task<List<MediaBrowserCompat.MediaItem>> =
        when (parentId) {
            rootId -> Tasks.forResult(listOf())
            else -> throw RuntimeException("Invalid parent media item requested")
        }
}
