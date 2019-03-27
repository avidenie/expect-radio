package ro.expectations.radio

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MediaSessionViewModel(private val mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    val isConnected = mediaSessionConnection.isConnected

    fun playMedia() {
        val nowPlaying = mediaSessionConnection.nowPlaying.value
        val transportControls = mediaSessionConnection.transportControls
        transportControls.playFromMediaId("7n9SPGkdMwBBYE7N8Niv", null)
    }

    class Factory(
        private val mediaSessionConnection: MediaSessionConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaSessionViewModel(mediaSessionConnection) as T
        }
    }
}