package com.snap.recyclingexample.ui.cats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.snap.recyclingexample.R
import com.snap.recyclingexample.ui.cats.data.CatPageStateProvider
import com.snap.recyclingexample.ui.cats.data.CatsDatabase
import com.snap.ui.recycling.adapter.ObservableViewModelSectionAdapter
import com.snap.ui.recycling.factory.ViewFactory
import io.reactivex.disposables.CompositeDisposable

class CatPageFragment : Fragment(R.layout.fragment_cat_page) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var observableAdapter: ObservableViewModelSectionAdapter
    private val sessionDisposable = CompositeDisposable()

    private val catsDatabase = CatsDatabase()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recycler_view)

        val pageStateSource = CatPageStateProvider()
        val eventDispatcher = CatPageEventDispatcher(pageStateSource)
        val viewFactory = ViewFactory(CatPageViewTypes::class.java)

        val pageState = pageStateSource.observe()
        observableAdapter = ObservableViewModelSectionAdapter(
                viewFactory,
                eventDispatcher
        ).apply {
            addSections(listOf(
                    CatLabelsSection(
                            viewFactory,
                            eventDispatcher,
                            pageState
                    ),
                    CatListSection(
                            catsDatabase,
                            pageState
                    )
            ))
        }
        recyclerView.adapter = observableAdapter
        recyclerView.layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
        )
    }

    override fun onResume() {
        super.onResume()
        observableAdapter.subscribe().also {
            sessionDisposable.add(it)
        }
    }

    override fun onPause() {
        super.onPause()
        sessionDisposable.dispose()
    }
}
