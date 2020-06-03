package com.snap.ui.recycling.prefetch

import android.util.Log
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.core.os.TraceCompat
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.android.MainThreadDisposable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.LinkedList
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "RxViewPrefetcher"

/**
 * A [ViewPrefetcher] with disposable preloading.
 */
class CompletableViewPrefetcher constructor(
    private val inflationScheduler: Scheduler,
    private val layoutInflater: LayoutInflater,
    private val parent: ViewGroup?
) : MainThreadDisposable(), ViewPrefetcher {

    private val disposed = AtomicBoolean(false)
    private val inflated = SparseArray<LinkedList<View>>()
    private val inflaterProvider = InflaterCache()

    @MainThread
    override fun getPrefetchedView(layoutId: Int): View? {
        val views = inflated.get(layoutId)
        return if (views != null && !views.isEmpty()) views.remove() else null
    }

    /**
     * Takes a mapping from layoutId to a count and preloads each view that many times.
     */
    fun load(preloads: Map<Int, Int>): Completable {
        return Observable.fromIterable(preloads.entries)
                .flatMap { entry -> Observable.just(entry.key).repeat(entry.value.toLong()) }
                .flatMap { layoutId ->
                    Observable.just(layoutId)
                            .map {
                                try {
                                    TraceCompat.beginSection("preload")
                                    val inflatedView =
                                            inflaterProvider.threadInflater(layoutInflater).inflate(layoutId,
                                                parent, false)
                                    Inflation(layoutId, inflatedView)
                                } finally {
                                    TraceCompat.endSection()
                                }
                            }
                            // Layout inflation seems to be causing a race condition in initializing the theme
                            // We can ignore the error for now since this is an optimization
                            // https://jira.sc-corp.net/browse/APP-14367
                            .doOnError { Log.e(TAG, "Failed to inflate", it) }
                            .onErrorReturnItem(FAILED_INFLATION)
                            .subscribeOn(inflationScheduler)
                }
                .subscribeOn(inflationScheduler)
                .observeOn(AndroidSchedulers.mainThread())
                .filter { it != FAILED_INFLATION }
                .map(::addPreloadedView)
                .ignoreElements()
    }

    fun <T : View> load(layoutId: Int): Single<T> {
        return Single.just(layoutId)
                .doOnSubscribe { TraceCompat.beginSection("view:load") }
                .doAfterTerminate { TraceCompat.endSection() }
                .map { inflaterProvider.threadInflater(layoutInflater).inflate(it, parent, false) as T }
                .subscribeOn(inflationScheduler)
    }

    @MainThread
    override fun onDispose() {
        disposed.set(true)
        for (i in 0 until inflated.size()) {
            inflated.get(inflated.keyAt(i)).clear()
        }
        inflated.clear()
    }

    private fun addPreloadedView(inflation: Inflation): Inflation {
        if (disposed.get()) {
            return inflation
        }
        var views = inflated.get(inflation.layoutId)
        if (views == null) {
            views = LinkedList()
            inflated.put(inflation.layoutId, views)
        }
        // We should have filtered out the inflation with null view case before this
        views.add(inflation.view!!)
        return inflation
    }

    private class Inflation constructor(val layoutId: Int, val view: View?)

    companion object {

        // Inflation contains a view and lint doesn't like that we are saving it in a static variable
        // but this is a false positive since we are passing in a null view
        private val FAILED_INFLATION = Inflation(0, null)
    }
}
