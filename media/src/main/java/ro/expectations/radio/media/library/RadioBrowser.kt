package ro.expectations.radio.media.library

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class RadioBrowser(private val db: FirebaseFirestore) {

    private val rootId = "__radio__"

    fun getRoot(): MediaBrowserCompat.MediaItem {

        val description = MediaDescriptionCompat.Builder()
            .setMediaId(rootId)
            .setTitle("Radios")
            .build()

        return MediaBrowserCompat.MediaItem(description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
    }

    fun canLoadChildren(parentId: String): Boolean = parentId.startsWith(rootId, true)

    fun loadChildren(parentId: String) : Task<MutableList<MediaBrowserCompat.MediaItem>> =
        when (parentId) {
            rootId -> getRadios()
            else -> throw RuntimeException("Invalid parent media item requested")
        }

    fun getRadios() : Task<MutableList<MediaBrowserCompat.MediaItem>> =
        db.collection("radios")
            .orderBy("name")
            .get()
            .continueWith {
                if (it.isSuccessful) {
                    val radios = mutableListOf<MediaBrowserCompat.MediaItem>()
                    it.result?.documents?.forEach { document ->
                        val description = MediaDescriptionCompat.Builder()
                            .setMediaId(document.id)
                            .setTitle(document.getString("name"))
                            .setSubtitle(document.getString("tagline"))
                            .setIconUri(Uri.parse(document.getString("icon")))
                            .setMediaUri(Uri.parse(document.getString("src")))
                            .build()
                        radios.add(MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE))
                    }
                    radios
                } else {
                    logger.warn(it.exception) { "Error retrieving radios" }
                    mutableListOf()
                }
            }
}
