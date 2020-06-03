package com.snap.ui.event

/**
 * The interface for handling errors occurred in the [RxEventDispatcher].
 */
interface EventBusErrorHandler {

    /**
     * Gets invoked when an event handler emits an error.
     * @return Indicates whether the error should be swallowed so that the [RxEventDispatcher] will continue handling events.
     *         When it's true, the error will be swallowed, and the [EventBus] will continue handling events.
     *         When it's false, the error will be thrown, and the [EventBus] will stop handling events.
     */
    fun onErrorComplete(error: Throwable): Boolean

    /**
     * Gets invoked when an event is dispatched but can't be handled by the [RxEventDispatcher].
     */
    fun onUnhandledEvent(event: Any)
}
