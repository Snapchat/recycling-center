package com.snap.ui.recycling.factory

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.spy
import com.snap.ui.recycling.AdapterViewType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.TestObserver
import org.junit.After
import org.junit.Before
import org.junit.Test

class ViewFactoryTest {
    private lateinit var viewFactory: ViewFactory
    private lateinit var context: Context
    private lateinit var parent: ViewGroup

    private val disposable = CompositeDisposable()

    @Before
    fun setup() {
        val viewTypes: Set<Class<out AdapterViewType>> = setOf(
                DummyAdapterViewType1::class.java,
                DummyAdapterViewType2::class.java
        )
        context = mock()
        parent = mock()

        viewFactory = spy(ViewFactory(viewTypes)).apply {
            doReturn(mock<View>()).`when`(this).getOrCreateView(any(), any(), any())
        }
    }

    @After
    fun after() {
        disposable.clear()
    }

    @Test
    fun on_viewHolder_created_observeViewHolderCreation_subject_emits_view_type() {
        val testObserver = TestObserver<AdapterViewType>()
        viewFactory.observeViewHolderCreation()
                .subscribeWith(testObserver)
                .apply { disposable.add(this) }

        viewFactory.createViewHolder(context, 3, parent)

        testObserver.assertValue {
            it == DummyAdapterViewType2.SOME_VIEW_3
        }
    }

    @Test
    fun on_multiple_viewHolder_created_observeViewHolderCreation_subject_emits_view_types() {
        val testObserver = TestObserver<AdapterViewType>()
        viewFactory.observeViewHolderCreation()
                .subscribeWith(testObserver)
                .apply { disposable.add(this) }

        viewFactory.createViewHolder(context, 0, parent)
        viewFactory.createViewHolder(context, 3, parent)

        testObserver.assertValueAt(0) {
            it == DummyAdapterViewType1.SOME_VIEW_0
        }
        testObserver.assertValueAt(1) {
            it == DummyAdapterViewType2.SOME_VIEW_3
        }
    }

    enum class DummyAdapterViewType1(override val layoutId: Int) : AdapterViewType {
        SOME_VIEW_0(ViewFactory.DEFAULT_CONTAINER),
        SOME_VIEW_1(ViewFactory.DEFAULT_CONTAINER)
    }

    enum class DummyAdapterViewType2(override val layoutId: Int) : AdapterViewType {
        SOME_VIEW_2(ViewFactory.DEFAULT_CONTAINER),
        SOME_VIEW_3(ViewFactory.DEFAULT_CONTAINER)
    }
}
