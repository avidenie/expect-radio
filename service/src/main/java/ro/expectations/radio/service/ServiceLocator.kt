package ro.expectations.radio.service

import android.app.Application
import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import ro.expectations.radio.service.db.RadioDatabase
import ro.expectations.radio.service.model.RadioModel
import ro.expectations.radio.service.repository.RadioRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

interface ServiceLocator {

    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(context: Context): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(
                            app = context.applicationContext as Application)
                }
                return instance!!
            }
        }
    }

    fun getModel(): RadioModel

    fun getRepository(): RadioRepository

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor
}

class DefaultServiceLocator(val app: Application) : ServiceLocator {

    private val diskIOExecutor = Executors.newSingleThreadExecutor()

    private val networkIOExecutor = Executors.newFixedThreadPool(5)

    override fun getModel(): RadioModel = RadioModel(getRepository())

    override fun getRepository(): RadioRepository = RadioRepository(
            db = RadioDatabase.create(app),
            firestore = FirebaseFirestore.getInstance(),
            ioExecutor = getDiskIOExecutor())

    override fun getNetworkExecutor(): Executor = networkIOExecutor

    override fun getDiskIOExecutor(): Executor = diskIOExecutor
}