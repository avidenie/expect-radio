package ro.expectations.radio.media.repository

import android.support.v4.media.MediaMetadataCompat
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ro.expectations.radio.media.auth.AuthProvider

class RadioRepository(private val authProvider: AuthProvider, private val db: FirebaseFirestore) {

    fun getRadios() : Task<List<MediaMetadataCompat>> = authProvider.whenAuthenticated().onSuccessTask {
        db.collection("radios")
            .orderBy("name")
            .get()
            .continueWith {
                val radios = it.result?.documents?.map { doc ->
                    toMediaMetadata(doc)
                }
                radios ?: mutableListOf()
            }
    }

    fun getRadio(id: String): Task<MediaMetadataCompat?> = authProvider.whenAuthenticated().onSuccessTask {
        db.collection("radios")
            .document(id)
            .get()
            .continueWith {
                val docSnapshot = it.result
                if (docSnapshot != null) {
                    toMediaMetadata(docSnapshot)
                } else {
                    null
                }
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