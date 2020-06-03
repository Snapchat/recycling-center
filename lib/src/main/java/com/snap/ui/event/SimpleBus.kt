package com.snap.ui.event

import androidx.core.os.TraceCompat
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import org.greenrobot.eventbus.EventBus

/**
 * Provides an [EventDispatcher] that publishes its events to an [EventBus].
 *
 * This class is [Disposable], resulting in the disposal of all subscriptions. Individual
 * subscribers from calls to [.subscribe] can also be disposed independently.
 */
open class SimpleBus : Disposable {

    private val eventBus = EventBus.builder()
        .strictMethodVerification(false)
        .throwSubscriberException(true)
        .logNoSubscriberMessages(false)
        .sendNoSubscriberEvent(false)
        .build()

    private val disposables = CompositeDisposable()

    open val eventDispatcher = EventDispatcher { event ->
        if (!isDisposed) {
            TraceCompat.beginSection("RxBus:post")
            try {
                eventBus.post(event)
            } finally {
                TraceCompat.endSection()
            }
        }
    }

    open fun subscribe(receiver: Any): Disposable {
        TraceCompat.beginSection("RxBus:subscribe")
        return try {
            eventBus.register(receiver)
            val disposable = Disposables.fromAction {
                eventBus.unregister(receiver)
                disposables.remove(this)
            }
            disposables.add(disposable)
            disposable
        } finally {
            TraceCompat.endSection()
        }
    }

    override fun isDisposed(): Boolean {
        return disposables.isDisposed
    }

    override fun dispose() {
        disposables.dispose()
    }
}
