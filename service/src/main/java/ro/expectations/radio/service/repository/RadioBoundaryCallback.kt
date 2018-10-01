package ro.expectations.radio.service.repository

import android.arch.paging.PagedList
import android.arch.paging.PagingRequestHelper
import android.support.annotation.MainThread
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import ro.expectations.radio.common.Logger
import ro.expectations.radio.service.db.RadioEntity
import ro.expectations.radio.service.extensions.createStatusLiveData
import java.util.concurrent.Executor

class RadioBoundaryCallback(
        private val firestore: FirebaseFirestore,
        private val handleResponse : (List<DocumentSnapshot>) -> Unit,
        private val ioExecutor: Executor,
        private val networkPageSize: Int)
    : PagedList.BoundaryCallback<RadioEntity>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    @MainThread
    override fun onZeroItemsLoaded() {
        super.onZeroItemsLoaded()

        Logger.e(TAG, "onZeroItemsLoaded")

        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            firestore.collection("radio-stations")
                    .orderBy("name")
                    .limit(networkPageSize.toLong())
                    .get()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            ioExecutor.execute {
                                handleResponse(task.result.documents)
                                it.recordSuccess()
                            }
                        } else {
                            Logger.e(TAG, task.exception as Throwable, task.exception!!.message!!)
                            it.recordFailure(task.exception as Throwable)
                        }
                    }
        }
    }

    override fun onItemAtEndLoaded(itemAtEnd: RadioEntity) {
        super.onItemAtEndLoaded(itemAtEnd)

        Logger.e(TAG, "onItemAtEndLoaded -> $itemAtEnd")

        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            firestore.collection("radio-stations")
                    .document(itemAtEnd.id)
                    .get()
                    .addOnCompleteListener { firstTask ->
                        if (firstTask.isSuccessful) {
                            firestore.collection("radio-stations")
                                    .orderBy("name")
                                    .startAfter(firstTask.result)
                                    .limit(networkPageSize.toLong())
                                    .get()
                                    .addOnCompleteListener { secondTask ->
                                        if (secondTask.isSuccessful) {
                                            ioExecutor.execute {
                                                handleResponse(secondTask.result.documents)
                                                it.recordSuccess()
                                            }
                                        } else {
                                            Logger.e(TAG, secondTask.exception as Throwable, secondTask.exception!!.message!!)
                                            it.recordFailure(secondTask.exception as Throwable)
                                        }
                                    }
                        } else {
                            Logger.e(TAG, firstTask.exception as Throwable, firstTask.exception!!.message!!)
                            it.recordFailure(firstTask.exception as Throwable)
                        }
                    }
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: RadioEntity) {
        // ignored, since we only ever append to what's in the DB

        Logger.e(TAG, "onItemAtFrontLoaded -> $itemAtFront")
    }
}

private const val TAG = "RadioBoundaryCallback"
