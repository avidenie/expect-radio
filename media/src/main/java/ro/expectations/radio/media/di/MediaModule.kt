package ro.expectations.radio.media.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.Module
import org.koin.dsl.module
import ro.expectations.radio.media.auth.AuthProvider
import ro.expectations.radio.media.browser.MediaBrowser
import ro.expectations.radio.media.repository.MusicRepository
import ro.expectations.radio.media.repository.PodcastRepository
import ro.expectations.radio.media.repository.RadioRepository

val mediaModule: Module = module {

    single { AuthProvider(FirebaseAuth.getInstance()) }

    single { RadioRepository(get(), FirebaseFirestore.getInstance()) }
    single { PodcastRepository(get(), FirebaseFirestore.getInstance()) }
    single { MusicRepository(get(), FirebaseFirestore.getInstance()) }

    single { MediaBrowser(get(), get(), get(), get()) }
}
