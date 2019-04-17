package ro.expectations.radio.media.repository

import android.support.v4.media.MediaMetadataCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import ro.expectations.radio.media.auth.AuthProvider

class PodcastRepository(private val authProvider: AuthProvider, private val db: FirebaseFirestore) {

    fun getPodcasts(): Task<List<MediaMetadataCompat>> = authProvider.whenAuthenticated().onSuccessTask {
        Tasks.forResult(listOf<MediaMetadataCompat>())
    }
}