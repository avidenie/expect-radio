package ro.expectations.radio.media.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.Module
import org.koin.dsl.module
import ro.expectations.radio.media.auth.AuthProvider
import ro.expectations.radio.media.browser.MediaBrowser
import ro.expectations.radio.media.playback.PlaybackPreparer
import ro.expectations.radio.media.playback.QueueManager
import ro.expectations.radio.media.repository.MusicRepository
import ro.expectations.radio.media.repository.PodcastRepository
import ro.expectations.radio.media.repository.RadioRepository

val mediaModule: Module = module {

    single { AuthProvider(FirebaseAuth.getInstance()) }

    single { FirebaseFirestore.getInstance() }
    single { RadioRepository(get(), get()) }
    single { PodcastRepository(get(), get()) }
    single { MusicRepository(get(), get()) }

    single { MediaBrowser(get(), get(), get(), get()) }
    single { (queueManager: QueueManager) -> PlaybackPreparer(queueManager, get(), get(), get()) }
}
