package ro.expectations.radio.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ro.expectations.radio.MediaSessionConnection

class MediaSessionViewModel(private val mediaSessionConnection: MediaSessionConnection) : ViewModel() {

    class Factory(
        private val mediaSessionConnection: MediaSessionConnection
    ) : ViewModelProvider.NewInstanceFactory() {

        @Suppress("unchecked_cast")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MediaSessionViewModel(mediaSessionConnection) as T
        }
    }
}