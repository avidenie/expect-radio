package ro.expectations.radio.media.library

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class MediaBrowser(private val auth: FirebaseAuth, db: FirebaseFirestore) {

    companion object {
        const val EMPTY_ROOT = "__empty__"
        const val BROWSABLE_ROOT = "__root__"
    }

    private val radioBrowser = RadioBrowser(db)
    private val musicBrowser = MusicBrowser(db)
    private val podcastBrowser = PodcastBrowser(db)

    fun getRoot(): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(BROWSABLE_ROOT, null)
    }

    fun loadChildren(parentId: String) : Task<List<MediaBrowserCompat.MediaItem>> =
        whenAuthenticated()
            .continueWithTask {
                when (parentId) {
                    EMPTY_ROOT -> getEmptyRoot()
                    BROWSABLE_ROOT -> getBrowsableRoot()
                    else -> when {
                        radioBrowser.canLoadChildren(parentId) -> radioBrowser.loadChildren(parentId)
                        podcastBrowser.canLoadChildren(parentId) -> podcastBrowser.loadChildren(parentId)
                        musicBrowser.canLoadChildren(parentId) -> musicBrowser.loadChildren(parentId)
                        else -> throw RuntimeException("Invalid parent media item requested")
                    }
                }
            }

    private fun getEmptyRoot() : Task <List<MediaBrowserCompat.MediaItem>> = Tasks.forResult(mutableListOf())

    private fun getBrowsableRoot() : Task<List<MediaBrowserCompat.MediaItem>> {
        val sections = listOf(
            radioBrowser.getRoot(),
            podcastBrowser.getRoot(),
            musicBrowser.getRoot()
        )

        return Tasks.forResult(sections)
    }

    private fun whenAuthenticated() : Task<FirebaseUser> {

        return if (auth.currentUser == null) {
            auth.signInAnonymously().continueWith { it.result?.user }
        } else {
            Tasks.forResult(auth.currentUser)
        }
    }

    fun getRadios() : Task<List<MediaMetadataCompat>> =
        whenAuthenticated()
            .continueWithTask { if(it.isSuccessful) radioBrowser.getRadios() else throw it.exception!! }

    fun getRadio(id: String) : Task<MediaMetadataCompat> =
        whenAuthenticated()
            .continueWithTask { if(it.isSuccessful) radioBrowser.getRadio(id) else throw it.exception!! }
}
