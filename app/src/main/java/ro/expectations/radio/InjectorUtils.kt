package ro.expectations.radio

import android.content.ComponentName
import android.content.Context
import ro.expectations.radio.media.PlaybackService
import ro.expectations.radio.viewmodels.MediaSessionViewModel
import ro.expectations.radio.viewmodels.MediaItemViewModel

object InjectorUtils {

    fun provideMediaSessionViewModel(context: Context): MediaSessionViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return MediaSessionViewModel.Factory(mediaSessionConnection)
    }

    fun provideMediaItemViewModel(context: Context, parentId: String): MediaItemViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return MediaItemViewModel.Factory(parentId, mediaSessionConnection)
    }

    private fun provideMediaSessionConnection(context: Context): MediaSessionConnection {
        return MediaSessionConnection.getInstance(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
    }

}