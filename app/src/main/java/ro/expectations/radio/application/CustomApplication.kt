package ro.expectations.radio.application

import android.support.multidex.MultiDexApplication
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.FirebaseFirestore



class CustomApplication : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        // Initialise Firebase
        FirebaseApp.initializeApp(this)

        // Set up Firestore
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
                .setTimestampsInSnapshotsEnabled(true)
                .build()
        firestore.firestoreSettings = settings

        // Initialise Fresco
        Fresco.initialize(this)
    }
}