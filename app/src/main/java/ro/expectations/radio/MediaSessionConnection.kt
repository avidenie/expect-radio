package ro.expectations.radio

import android.arch.lifecycle.MutableLiveData
import android.content.ComponentName
import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat


class MediaSessionConnection(context: Context, serviceComponent: ComponentName) {

    val isConnected = MutableLiveData<Boolean>()
            .apply { postValue(false) }

    val playbackState = MutableLiveData<PlaybackStateCompat>().apply {
            postValue(EMPTY_PLAYBACK_STATE)
        }

    val nowPlaying = MutableLiveData<MediaMetadataCompat>()
            .apply { postValue(NOTHING_PLAYING) }

    val rootMediaId: String get() = mediaBrowser.root

    val transportControls: MediaControllerCompat.TransportControls get() = mediaController.transportControls

    private val mediaBrowserConnectionCallback = MediaBrowserConnectionCallback(context)
    private val mediaBrowser = MediaBrowserCompat(context,
            serviceComponent,
            mediaBrowserConnectionCallback, null)
            .apply { connect() }
    private lateinit var mediaController: MediaControllerCompat

    fun subscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.subscribe(parentId, callback)
    }

    fun unsubscribe(parentId: String, callback: MediaBrowserCompat.SubscriptionCallback) {
        mediaBrowser.unsubscribe(parentId, callback)
    }

    private inner class MediaBrowserConnectionCallback(private val context: Context)
        : MediaBrowserCompat.ConnectionCallback() {

        override fun onConnected() {

            // Create a MediaControllerCompat
            mediaController = MediaControllerCompat(context, mediaBrowser.sessionToken).apply {
                registerCallback(MediaControllerCallback())
            }

            // Signal that we have a connection to the service
            isConnected.postValue(true)
        }

        override fun onConnectionSuspended() {
            isConnected.postValue(false)
        }

        override fun onConnectionFailed() {
            isConnected.postValue(false)
        }
    }

    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            playbackState.postValue(state ?: EMPTY_PLAYBACK_STATE)
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            nowPlaying.postValue(metadata ?: NOTHING_PLAYING)
        }

        override fun onQueueChanged(queue: MutableList<MediaSessionCompat.QueueItem>?) {
        }

        override fun onSessionDestroyed() {
            mediaBrowserConnectionCallback.onConnectionSuspended()
        }
    }

    companion object {
        // For Singleton instantiation.
        @Volatile
        private var instance: MediaSessionConnection? = null

        fun getInstance(context: Context, serviceComponent: ComponentName) =
                instance ?: synchronized(this) {
                    instance ?: MediaSessionConnection(context, serviceComponent)
                            .also { instance = it }
                }
    }
}

val EMPTY_PLAYBACK_STATE: PlaybackStateCompat = PlaybackStateCompat.Builder()
        .setState(PlaybackStateCompat.STATE_NONE, 0, 0f)
        .build()

val NOTHING_PLAYING: MediaMetadataCompat = MediaMetadataCompat.Builder()
        .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "")
        .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, 0)
        .build()