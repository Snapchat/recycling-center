package com.snap.ui.event

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import java.util.concurrent.atomic.AtomicReference
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

/**
 * Test cases for [RxEventDispatcher].
*/
class EventBusTest {

    @Suppress("MemberVisibilityCanBePrivate")
    @get:Rule
    val thrown: ExpectedException = ExpectedException.none()

    private val eventHandlers = mutableMapOf<Class<out Any>, EventHandler<*>>()
    private val errorHandler = mock<EventBusErrorHandler>()
    private val subject = RxEventDispatcher(eventHandlers, errorHandler)

    @Test
    fun subscribeToEvents_returnDisposable() {
        val disposable = subject.subscribeToEvents()
        assertThat(disposable.isDisposed).isFalse()
    }

    @Test
    fun subscribeToEvents_withMoreThanOnceAndPreviousSubscriptionNotDisposedOf_throw() {
        subject.subscribeToEvents()

        thrown.expect(IllegalStateException::class.java)
        thrown.expectMessage(
            "Please ensure the previous subscription is disposed of before subscribeToEvents() is called again."
        )

        subject.subscribeToEvents()
    }

    @Test
    fun subscribeToEvents_withMoreThanOnceAndPreviousSubscriptionDisposedOf_doNotThrow() {
        val disposable1 = subject.subscribeToEvents()
        disposable1.dispose()

        val disposable2 = subject.subscribeToEvents()
        assertThat(disposable2.isDisposed).isFalse()
    }

    @Test
    fun dispatch_withCorrespondingEventHandler_handleEvent() {
        val handledEvent = AtomicReference<String>()
        val eventHandler = object : EventHandler<String> {
            override fun handle(event: String) = Completable.fromAction { handledEvent.set(event) }
        }
        eventHandlers.putIfAbsent(String::class.java, eventHandler)
        subject.subscribeToEvents()

        val event = "Event"
        subject.dispatch(event)

        assertThat(handledEvent.get()).isEqualTo(event)
    }

    @Test
    fun dispatch_withErrorFromCorrespondingEventHandlerAndNotContinue_cancelSubscription() {
        doReturn(false).whenever(errorHandler).onErrorComplete(any())
        val eventHandler = object : EventHandler<String> {
            override fun handle(event: String) = Completable.error(IllegalStateException("Error"))
        }
        eventHandlers.putIfAbsent(String::class.java, eventHandler)
        val disposable = subject.subscribeToEvents()

        subject.dispatch("Event")

        assertThat(disposable.isDisposed).isTrue()
    }

    @Test
    fun dispatch_withErrorFromCorrespondingEventHandlerAndContinue_continueSubscription() {
        doReturn(true).whenever(errorHandler).onErrorComplete(any())
        val eventToTriggerSuccess = "Success"
        val handledEvent = AtomicReference<String>()
        val eventHandler = object : EventHandler<String> {
            override fun handle(event: String) =
                if (event == eventToTriggerSuccess) {
                    Completable.fromAction { handledEvent.set(event) }
                } else {
                    Completable.error(IllegalStateException("Error"))
                }
        }
        eventHandlers.putIfAbsent(String::class.java, eventHandler)
        val disposable = subject.subscribeToEvents()

        val eventToTriggerFailure = "Failure"
        subject.dispatch(eventToTriggerFailure)
        assertThat(handledEvent.get()).isNull()
        assertThat(disposable.isDisposed).isFalse()

        subject.dispatch("Success")
        assertThat(handledEvent.get()).isEqualTo(eventToTriggerSuccess)
        assertThat(disposable.isDisposed).isFalse()
    }

    @Test
    fun dispatch_withNoCorrespondingEventHandler_invokeOnUnhandledEvent() {
        val disposable = subject.subscribeToEvents()
        val event = "Event"
        subject.dispatch(event)

        verify(errorHandler).onUnhandledEvent(event)
        assertThat(disposable.isDisposed).isFalse()
    }

    @Test
    fun dispatch_withNoSubscriptionYet_doNotImpactSubsequentSubscriptions() {
        val event = "Event"
        subject.dispatch(event)

        val handledEvent = AtomicReference<String>()
        val eventHandler = object : EventHandler<String> {
            override fun handle(event: String) = Completable.fromAction { handledEvent.set(event) }
        }
        eventHandlers.putIfAbsent(String::class.java, eventHandler)
        subject.subscribeToEvents()
        assertThat(handledEvent.get()).isNull()

        subject.dispatch(event)
        assertThat(handledEvent.get()).isEqualTo(event)
    }
}
