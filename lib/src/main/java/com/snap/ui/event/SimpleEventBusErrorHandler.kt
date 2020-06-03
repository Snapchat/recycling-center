package com.snap.ui.event

/**
 * A simple implementation of [EventBusErrorHandler] which allows the [RxEventDispatcher] to continue handling events upon
 * errors, and does nothing when there are events unhandled.
 */
open class SimpleEventBusErrorHandler : EventBusErrorHandler {

    /**
     * Allows the error to be swallowed so that the [RxEventDispatcher] will continue handling events.
     */
    override fun onErrorComplete(error: Throwable) = true

    /**
     * Does nothing when there are events unhandled.
     */
    override fun onUnhandledEvent(event: Any) = Unit
}
