package ro.expectations.radio.service

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.ServiceLifecycleDispatcher
import android.content.Intent
import android.os.IBinder
import android.support.annotation.CallSuper
import android.support.v4.media.MediaBrowserServiceCompat

abstract class LifecycleMediaBrowserService : MediaBrowserServiceCompat(), LifecycleOwner {

    private lateinit var dispatcher : ServiceLifecycleDispatcher

    @CallSuper
    override fun onCreate() {
        dispatcher = ServiceLifecycleDispatcher(this)
        dispatcher.onServicePreSuperOnCreate()
        super.onCreate()
    }

    @CallSuper
    override fun onBind(intent: Intent): IBinder? {
        dispatcher.onServicePreSuperOnBind()
        return super.onBind(intent)
    }

    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    @CallSuper
    override fun onStart(intent: Intent, startId: Int) {
        dispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    // this method is added only to annotate it with @CallSuper.
    // In usual service super.onStartCommand is no-op, but in LifecycleService
    // it results in dispatcher.onServicePreSuperOnStart() call, because
    // super.onStartCommand calls onStart().
    @CallSuper
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    @CallSuper
    override fun onDestroy() {
        dispatcher.onServicePreSuperOnDestroy()
        super.onDestroy()
    }

    override fun getLifecycle(): Lifecycle {
        return dispatcher.lifecycle
    }
}