package ro.expectations.radio

import android.content.ComponentName
import android.content.Context
import ro.expectations.radio.media.PlaybackService

object InjectorUtils {

    fun provideMainActivityViewModel(context: Context): MediaSessionViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return MediaSessionViewModel.Factory(mediaSessionConnection)
    }

    private fun provideMediaSessionConnection(context: Context): MediaSessionConnection {
        return MediaSessionConnection.getInstance(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
    }

}