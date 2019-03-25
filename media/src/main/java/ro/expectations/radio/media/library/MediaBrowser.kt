package ro.expectations.radio.media.library

import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MediaBrowser(private val auth: FirebaseAuth, db: FirebaseFirestore) {

    companion object {
        const val EMPTY_ROOT = "__empty__"
        const val BROWSABLE_ROOT = "__root__"
    }

    private val radioLibrary = RadioBrowser(db)
    private val musicLibrary = MusicBrowser(db)
    private val podcastLibrary = PodcastBrowser(db)

    fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): MediaBrowserServiceCompat.BrowserRoot? {
        return MediaBrowserServiceCompat.BrowserRoot(BROWSABLE_ROOT, null)
    }

    fun onLoadChildren(parentId: String, result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {

        result.detach()

        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener {
                if (it.isSuccessful) {
                    onAuth(parentId, result)
                } else {
                    result.sendResult(null)
                }
            }
        } else {
            onAuth(parentId, result)
        }
    }

    private fun onEmptyRoot(result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        result.sendResult(mutableListOf())
    }

    private fun onBrowsableRoot(result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>) {
        val sections = mutableListOf(
            radioLibrary.getRoot(),
            podcastLibrary.getRoot(),
            musicLibrary.getRoot()
        )

        result.sendResult(sections)
    }

    private fun onAuth(
        parentId: String,
        result: MediaBrowserServiceCompat.Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        return when (parentId) {
            EMPTY_ROOT -> onEmptyRoot(result)
            BROWSABLE_ROOT -> onBrowsableRoot(result)
            else -> when {
                radioLibrary.canLoadChildren(parentId) -> radioLibrary.onLoadChildren(parentId, result)
                podcastLibrary.canLoadChildren(parentId) -> podcastLibrary.onLoadChildren(parentId, result)
                musicLibrary.canLoadChildren(parentId) -> musicLibrary.onLoadChildren(parentId, result)
                else -> result.sendResult(null)
            }
        }
    }
}
