package ro.expectations.radio.application

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.FirebaseApp

class CustomApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        Fresco.initialize(this)
    }
}