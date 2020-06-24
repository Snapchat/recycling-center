package com.snap.recyclingexample.ui.cats

import com.snap.recyclingexample.ui.cats.data.CatPageState
import com.snap.recyclingexample.ui.cats.data.CatType
import com.snap.recyclingexample.ui.cats.data.CatsDatabase
import com.snap.recyclingexample.ui.cats.view.CatViewModel
import com.snap.recyclingexample.ui.cats.view.TitleViewModel
import com.snap.ui.recycling.ObservableSectionController
import com.snap.ui.recycling.viewmodel.AdapterViewModel
import com.snap.ui.seeking.ListSeekable
import com.snap.ui.seeking.Seekable
import com.snap.ui.seeking.Seekables
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.atomic.AtomicBoolean

class CatListSection(
        val catsDatabase: CatsDatabase,
        val pageStateObservable: Observable<CatPageState>
) : ObservableSectionController {
    private val disposed = AtomicBoolean(false)

    override fun getViewModels(): Observable<out Seekable<AdapterViewModel>> {
        return Observable.combineLatest(
                catsDatabase.observeTheCats(),
                pageStateObservable,
                BiFunction { cats, pageState ->

                    val big = if (!pageState.showBigCats) listOf() else cats
                            .filter { it.type == CatType.Big }
                            .map { CatViewModel(it) }

                    val medium = if (!pageState.showMediumCats) listOf() else cats
                            .filter { it.type == CatType.Medium }
                            .map { CatViewModel(it) }

                    val small = if (!pageState.showSmallCats) listOf() else cats
                            .filter { it.type == CatType.Small }
                            .map { CatViewModel(it) }

                    val bigModels: Seekable<AdapterViewModel> = if (big.isEmpty()) Seekables.empty() else ListSeekable(
                            big.toMutableList<AdapterViewModel>().apply {
                                add(0, TitleViewModel("Big Cats"))
                            }
                    )

                    val mediumModels: Seekable<AdapterViewModel> = if (medium.isEmpty()) Seekables.empty() else ListSeekable(
                            medium.toMutableList<AdapterViewModel>().apply {
                                add(0, TitleViewModel("Medium Cats"))
                            }
                    )

                    val smallModels: Seekable<AdapterViewModel> = if (small.isEmpty()) Seekables.empty() else ListSeekable(
                            small.toMutableList<AdapterViewModel>().apply {
                                add(0, TitleViewModel("Small Cats"))
                            }
                    )

                    Seekables.concat(bigModels, Seekables.concat(mediumModels, smallModels))
                })
    }


    override fun dispose() {
        disposed.set(true)
    }

    override fun isDisposed(): Boolean = disposed.get()
}