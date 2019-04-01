package ro.expectations.radio.media.library

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
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

    fun loadChildren(parentId: String) : Task<List<MediaBrowserCompat.MediaItem>> =
        when (parentId) {
            rootId -> getRadios().continueWith {
                it.result?.map { metadata ->
                    MediaBrowserCompat.MediaItem(metadata.description, MediaBrowserCompat.MediaItem.FLAG_BROWSABLE)
                }
            }
            else -> throw RuntimeException("Invalid parent media item requested")
        }

    fun getRadios() : Task<List<MediaMetadataCompat>> =
        db.collection("radios")
            .orderBy("name")
            .get()
            .continueWith {
                if (it.isSuccessful) {
                    it.result?.documents?.map { doc ->
                        toMediaMetadata(doc)
                    }
                } else {
                    logger.warn(it.exception) { "Error retrieving radios" }
                    mutableListOf()
                }
            }

    fun getRadio(id: String): Task<MediaMetadataCompat> =
        db.collection("radios")
            .document(id)
            .get()
            .continueWith {

                logger.debug { "got radio doc ${it.result}"}

                if (it.result?.data != null) {
                    toMediaMetadata(it.result!!)
                } else {
                    throw RuntimeException("No such radio")
                }
            }

    private fun toMediaMetadata(doc: DocumentSnapshot) : MediaMetadataCompat =
        doc.run {
            MediaMetadataCompat.Builder()
                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, getString("name"))
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, getString("tagline"))
                .putText(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI, getString("icon"))
                .putText(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, getString("src"))
                .build()
        }
}
