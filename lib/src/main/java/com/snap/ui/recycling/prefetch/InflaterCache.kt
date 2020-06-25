package com.snap.ui.recycling.prefetch

import android.util.LongSparseArray
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import java.lang.ref.WeakReference

/**
 * An ActivityScoped version will be provided by default, you should not create new ones.
 */
class InflaterCache {

    private val cache: LongSparseArray<WeakReference<LayoutInflater>> = LongSparseArray()

    /**
     * Returns an inflater that is cached for the current thread.
     */
    fun threadInflater(layoutInflater: LayoutInflater): LayoutInflater {
        // LayoutInflater acquires an instance lock during inflate
        // https://android.googlesource.com/platform/frameworks/base/+/master/core/java/android/view/LayoutInflater.java#452
        val tid = Thread.currentThread().id
        val inflater: LayoutInflater?
        synchronized(this) {
            inflater = cache.get(tid)?.get()
        }
        return if (inflater != null) {
            inflater
        } else {
            val oldContext = layoutInflater.context
            val config = oldContext.resources.configuration
            val newContext = ContextThemeWrapper(oldContext.createConfigurationContext(config), oldContext.theme)
            val newLayoutInflater = layoutInflater.cloneInContext(newContext)
            synchronized(this) {
                cache.put(tid, WeakReference(newLayoutInflater))
            }
            newLayoutInflater
        }
    }
}
