package com.snap.ui.recycling.adapter

import android.view.View
import com.nhaarman.mockito_kotlin.mock
import com.snap.ui.event.EventDispatcher
import com.snap.ui.recycling.ObservableSectionController
import com.snap.ui.recycling.factory.ViewFactory
import com.snap.ui.recycling.viewmodel.AdapterViewModel
import com.snap.ui.seeking.Seekable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.ObjectAssert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ObservableViewModelSectionAdapterTest {

    private val viewFactory = mock<ViewFactory>()
    private val eventDispatcher = mock<EventDispatcher>()
    private val sectionController1 = TestSectionController()
    private val sectionController2 = TestSectionController()

    private val subject = ObservableViewModelSectionAdapter(
        viewFactory,
        eventDispatcher,
        Schedulers.trampoline(),
        Schedulers.trampoline(),
        listOf(sectionController1, sectionController2)
    )

    @Test
    fun subscribe_subscribesToSectionControllers() {
        assertThat(sectionController1)
            .isNotSubscribedTo()
        assertThat(sectionController1)
            .isNotDisposed()
        assertThat(sectionController2)
            .isNotSubscribedTo()
        assertThat(sectionController2)
            .isNotDisposed()

        subject.subscribe()

        assertThat(sectionController1)
            .isSubscribedTo()
        assertThat(sectionController1)
            .isNotDisposed()
        assertThat(sectionController2)
            .isSubscribedTo()
        assertThat(sectionController2)
            .isNotDisposed()
    }

    @Test
    fun subscribe_disposingOfReturnedDisposable_disposesAdapter_andControllers() {
        assertThat(sectionController1)
            .isNotSubscribedTo()
        assertThat(sectionController1)
            .isNotDisposed()
        assertThat(sectionController2)
            .isNotSubscribedTo()
        assertThat(sectionController2)
            .isNotDisposed()

        val d = subject.subscribe()

        assertThat(sectionController1)
            .isSubscribedTo()
        assertThat(sectionController1)
            .isNotDisposed()
        assertThat(sectionController2)
            .isSubscribedTo()
        assertThat(sectionController2)
            .isNotDisposed()

        d.dispose()

        assertThat(sectionController1)
            .isNotSubscribedTo()
        assertThat(sectionController1)
            .isDisposed()
        assertThat(sectionController2)
            .isNotSubscribedTo()
        assertThat(sectionController2)
            .isDisposed()
        assertThat(d)
            .isDisposed()
    }

    private fun ObjectAssert<out Disposable>.isDisposed() = checkDisposeStatus(true)
    private fun ObjectAssert<out Disposable>.isNotDisposed() = checkDisposeStatus(false)

    private fun ObjectAssert<out Disposable>.checkDisposeStatus(disposed: Boolean) {
        satisfies {
            assertThat(it.isDisposed)
                .describedAs("was disposed")
                .isEqualTo(disposed)
        }
    }

    private fun ObjectAssert<TestSectionController>.isSubscribedTo() = checkSubscribeStatus(true)
    private fun ObjectAssert<TestSectionController>.isNotSubscribedTo() = checkSubscribeStatus(false)

    private fun ObjectAssert<TestSectionController>.checkSubscribeStatus(subscribed: Boolean) {
        satisfies {
            assertThat(it.subscribed)
                .describedAs("was subscribed")
                .isEqualTo(subscribed)
        }
    }

    class TestSectionController : ObservableSectionController {
        private val disposable = Disposables.empty()
        var subscribed = false
        override fun isDisposed() = disposable.isDisposed

        override fun onViewRecycled(itemView: View, viewModel: AdapterViewModel) {
        }

        override fun getViewModels(): Observable<out Seekable<AdapterViewModel>> {
            return Observable.never<Seekable<AdapterViewModel>>()
                .doOnSubscribe {
                    subscribed = true
                }.doOnDispose {
                    subscribed = false
                }
        }

        override fun onViewBound(itemView: View, model: AdapterViewModel) {
        }

        override fun dispose() {
            disposable.dispose()
        }
    }
}
