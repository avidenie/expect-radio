package ro.expectations.radio.di

import android.content.ComponentName
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import ro.expectations.radio.MediaSessionConnection
import ro.expectations.radio.media.playback.PlaybackService
import ro.expectations.radio.viewmodels.MediaItemViewModel
import ro.expectations.radio.viewmodels.MediaSessionViewModel

val viewModelsModule: Module = module {

    single { MediaSessionConnection(androidContext(), ComponentName(androidContext(), PlaybackService::class.java)) }

    viewModel { MediaSessionViewModel(get()) }

    viewModel { (parentId: String) -> MediaItemViewModel(parentId, get()) }
}
