package ro.expectations.radio.media.library

import android.net.Uri
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import androidx.media.MediaBrowserServiceCompat
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

    fun onLoadChildren(
        parentId: String,
        result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        return when (parentId) {
            rootId -> onGetRadios(result)
            else -> result.sendResult(null)
        }
    }

    private fun onGetRadios(result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        db.collection("radios")
            .orderBy("name")
            .get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val recommended = mutableListOf<MediaBrowserCompat.MediaItem>()
                    it.result?.documents?.forEach { document ->
                        val description = MediaDescriptionCompat.Builder()
                            .setMediaId(document.id)
                            .setTitle(document.getString("name"))
                            .setSubtitle(document.getString("slogan"))
                            .setIconUri(Uri.parse(document.getString("logo")))
                            .setMediaUri(Uri.parse(document.getString("source")))
                            .build()
                        recommended.add(MediaBrowserCompat.MediaItem(description, FLAG_PLAYABLE))
                    }
                    result.sendResult(recommended)
                } else {
                    logger.error(it.exception) { "Error while retrieving radios" }
                    result.sendResult(mutableListOf())
                }
            }
    }
}
