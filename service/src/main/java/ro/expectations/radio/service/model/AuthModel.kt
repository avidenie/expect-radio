package ro.expectations.radio.service.model

import com.google.firebase.auth.FirebaseAuth
import ro.expectations.radio.service.livedata.FirebaseAuthLiveData


class AuthModel(private val firebaseAuth: FirebaseAuth) {
    val firebaseAuthLiveData = FirebaseAuthLiveData(firebaseAuth)
}