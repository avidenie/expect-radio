package ro.expectations.radio.service.model

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Transformations.switchMap
import ro.expectations.radio.service.db.RadioEntity
import ro.expectations.radio.service.repository.Listing
import ro.expectations.radio.service.repository.RadioRepository

class RadioModel(private val repository: RadioRepository) {

    private val repoResult = MutableLiveData<Listing<RadioEntity>>().apply {
        postValue(repository.radios(30))
    }

    val radios = switchMap(repoResult) { it.pagedList }!!
    val networkState = switchMap(repoResult) { it.networkState }!!
    val refreshState = switchMap(repoResult) { it.refreshState }!!

    fun refresh() {
        repoResult.value?.refresh?.invoke()
    }

    fun retry() {
        val listing = repoResult.value
        listing?.retry?.invoke()
    }
}