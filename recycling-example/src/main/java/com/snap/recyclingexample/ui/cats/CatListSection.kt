package com.snap.recyclingexample.ui.cats

import com.snap.recyclingexample.ui.cats.data.CatData
import com.snap.recyclingexample.ui.cats.data.CatPageState
import com.snap.recyclingexample.ui.cats.data.CatType
import com.snap.recyclingexample.ui.cats.data.CatsDatabase
import com.snap.recyclingexample.ui.cats.view.CatViewModel
import com.snap.recyclingexample.ui.cats.view.HeaderViewModel
import com.snap.ui.recycling.ObservableSectionController
import com.snap.ui.recycling.viewmodel.AdapterViewModel
import com.snap.ui.seeking.Seekable
import com.snap.ui.seeking.Seekables
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
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
                            .filter { catFilter(it, pageState) }
                            .map { CatViewModel(it) }

                    val medium = if (!pageState.showMediumCats) listOf() else cats
                            .filter { it.type == CatType.Medium }
                            .filter { catFilter(it, pageState) }
                            .map { CatViewModel(it) }

                    val small = if (!pageState.showSmallCats) listOf() else cats
                            .filter { it.type == CatType.Small }
                            .filter { catFilter(it, pageState) }
                            .map { CatViewModel(it) }

                    val bigModels: Seekable<AdapterViewModel> = if (big.isEmpty()) Seekables.empty() else Seekables.copyOf(
                            big.toMutableList<AdapterViewModel>().apply {
                                add(0, HeaderViewModel("Big Cats"))
                            }
                    )

                    val mediumModels: Seekable<AdapterViewModel> = if (medium.isEmpty()) Seekables.empty() else Seekables.copyOf(
                            medium.toMutableList<AdapterViewModel>().apply {
                                add(0, HeaderViewModel("Medium Cats"))
                            }
                    )

                    val smallModels: Seekable<AdapterViewModel> = if (small.isEmpty()) Seekables.empty() else Seekables.copyOf(
                            small.toMutableList<AdapterViewModel>().apply {
                                add(0, HeaderViewModel("Small Cats"))
                            }
                    )

                    Seekables.concat(bigModels, Seekables.concat(mediumModels, smallModels))
                })
    }

    private fun catFilter(cat: CatData, pageState: CatPageState): Boolean {
        if (!pageState.showStripedCats) {
            if (cat.name == "Tiger" || cat.name == "Tabby" || cat.name == "Ocicat") {
                return false
            }
        }

        if (!pageState.showFluffyCats) {
            if (cat.name == "Lion" || cat.name == "Ragdoll" || cat.name == "Himalayan" || cat.name == "Persian") {
                return false
            }
        }

        return true
    }

    override fun dispose() {
        disposed.set(true)
    }

    override fun isDisposed(): Boolean = disposed.get()
}
