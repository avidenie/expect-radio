package ro.expectations.radio.application

import android.support.multidex.MultiDexApplication
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.FirebaseApp

class CustomApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        Fresco.initialize(this)
    }
}