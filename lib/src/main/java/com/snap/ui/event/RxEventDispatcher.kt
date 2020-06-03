package com.snap.ui.event

import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

/**
 * Subscribes to user initiated events, and invokes the corresponding event handlers.
*/
class RxEventDispatcher constructor(
    private val eventHandlers: Map<Class<*>, EventHandler<*>>,
    private val errorHandler: EventBusErrorHandler = SimpleEventBusErrorHandler()
) : EventDispatcher {

    private val subject = PublishSubject.create<Any>()

    override fun dispatch(event: Any) {
        subject.onNext(event)
    }

    /**
     * The caller should ensure the subscription is disposed of at the right time.
     */
    fun subscribeToEvents(): Disposable {
        if (subject.hasObservers()) {
            throw IllegalStateException(
                "Please ensure the previous subscription is disposed of before subscribeToEvents() is called again."
            )
        }

        return subject
            .flatMapCompletable { event ->
                eventHandlers[event.javaClass]
                    ?.attemptToHandle(event)
                    ?.onErrorComplete(errorHandler::onErrorComplete)
                    ?: Completable.fromAction { errorHandler.onUnhandledEvent(event) }
            }
            .subscribe()
    }

    private inline fun <reified T : Any> EventHandler<*>.attemptToHandle(event: T) =
        (event as? T)?.run { handle(this) }
}
