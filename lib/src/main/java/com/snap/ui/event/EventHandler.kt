package com.snap.ui.event

import io.reactivex.Completable

/**
 * The interface for handling an user initiated event in Memories.
*/
interface EventHandler<out T : Any> {

    /**
     * Handles the event asynchronously.
     */
    fun handle(event: @UnsafeVariance T): Completable
}
