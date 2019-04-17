package ro.expectations.radio.media.auth

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

open class AuthProvider(private val auth: FirebaseAuth) {
    fun whenAuthenticated() : Task<FirebaseUser> {
        return if (auth.currentUser == null) {
            auth.signInAnonymously().continueWith { it.result?.user }
        } else {
            Tasks.forResult(auth.currentUser)
        }
    }
}
