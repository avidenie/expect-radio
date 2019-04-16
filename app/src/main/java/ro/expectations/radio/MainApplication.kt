package ro.expectations.radio

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ro.expectations.radio.di.viewModelsModule

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {

            // inject Android context
            androidContext(this@MainApplication)

            // application modules
            modules(viewModelsModule)
        }
    }
}
