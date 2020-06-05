package com.snap.ui.recycling.prefetch

import android.util.ArrayMap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.TraceCompat
import com.snap.ui.recycling.AdapterViewType
import com.snap.ui.recycling.factory.ViewFactory
import com.snap.ui.recycling.factory.ViewModelViewHolder
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import java.util.LinkedList

/**
 * A [ViewHolderPrefetcher] with disposable preloading.
 */
class CompletableViewHolderPrefetcher constructor(
    private val viewFactory: ViewFactory,
    private val inflationScheduler: Scheduler,
    private val layoutInflater: LayoutInflater,
    private val parent: ViewGroup
) : ViewHolderPrefetcher {

    private val inflated = ArrayMap<AdapterViewType, LinkedList<ViewModelViewHolder<*>>>()
    private val inflaterProvider = InflaterCache()

    override fun getPrefetchedViewHolder(viewType: AdapterViewType): ViewModelViewHolder<*>? {
        synchronized(inflated) {
            val views = inflated[viewType]
            return if (!views.isNullOrEmpty()) {
                views.remove()
            } else null
        }
    }

    /**
     * Takes a mapping from layoutId to a count and preloads each view that many times.
     */
    fun load(preloads: Map<AdapterViewType, Int>): Completable {
        return Observable.fromIterable(preloads.entries)
                .flatMap { entry -> Observable.just(entry.key).repeat(entry.value.toLong()) }
                .flatMap { viewType ->
                    Observable.fromCallable {
                                try {
                                    TraceCompat.beginSection("preload:$viewType")
                                    val threadInflater = inflaterProvider.threadInflater(layoutInflater)
                                    val context = threadInflater.context
                                    val view = ViewFactory.createView(context, viewType, parent, threadInflater)
                                    val holder = ViewFactory.createViewHolderForType(
                                            viewFactory,
                                            viewType,
                                            view)

                                    Inflation(viewType, holder)
                                } finally {
                                    TraceCompat.endSection()
                                }
                            }
                            // Layout inflation seems to be causing a race condition in initializing the theme
                            // We can ignore the error for now since this is an optimization
                            .doOnError { Log.e(TAG, "Failed to inflate", it) }
                            .onErrorReturnItem(FAILED_INFLATION)
                            .subscribeOn(inflationScheduler)
                }
                .subscribeOn(inflationScheduler)
                .map { inflation -> addPreloadedView(inflation) }
                .ignoreElements()
    }

    fun <T : View> load(layoutId: Int): Single<T> {
        return Single.just(layoutId)
                .doOnSubscribe { TraceCompat.beginSection("view:load") }
                .doAfterTerminate { TraceCompat.endSection() }
                .map {
                    inflaterProvider.threadInflater(layoutInflater).inflate(it, parent, false) as T
                }
                .subscribeOn(inflationScheduler)
    }

    private fun addPreloadedView(inflation: Inflation) {
        if (inflation !== FAILED_INFLATION) {
            try {
                TraceCompat.beginSection("addPreload")
                synchronized(inflated) {
                    var views: LinkedList<ViewModelViewHolder<*>>? = inflated[inflation.viewType]
                    if (views == null) {
                        views = LinkedList()
                        inflated[inflation.viewType] = views
                    }
                    views.add(inflation.viewHolder!!)
                }
            } finally {
                TraceCompat.endSection()
            }
        }
    }

    private class Inflation internal constructor(
        internal val viewType: AdapterViewType?,
        internal val viewHolder: ViewModelViewHolder<*>?
    )
    companion object {

        private val TAG = "RxViewPrefetcher"
        // Inflation contains a view and lint doesn't like that we are saving it in a static variable
        // but this is a false positive since we are passing in a null view
        private val FAILED_INFLATION = Inflation(null, null)
    }
}
