package ro.expectations.radio.utils

import android.content.ComponentName
import android.content.Context
import ro.expectations.radio.MediaSessionConnection
import ro.expectations.radio.service.RadioService
import ro.expectations.radio.viewmodels.HomeActivityViewModel
import ro.expectations.radio.viewmodels.MediaItemFragmentViewModel


object InjectorUtils {

    private fun provideMediaSessionConnection(context: Context): MediaSessionConnection {
        return MediaSessionConnection.getInstance(context,
                ComponentName(context, RadioService::class.java))
    }

    fun provideHomeActivityViewModel(context: Context): HomeActivityViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return HomeActivityViewModel.Factory(mediaSessionConnection)
    }

    fun provideMediaItemFragmentViewModel(context: Context, mediaId: String)
            : MediaItemFragmentViewModel.Factory {
        val applicationContext = context.applicationContext
        val mediaSessionConnection = provideMediaSessionConnection(applicationContext)
        return MediaItemFragmentViewModel.Factory(mediaId, mediaSessionConnection)
    }
}