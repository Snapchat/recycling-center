package com.snap.recyclingexample.ui.cats.data

import androidx.annotation.MainThread
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject

data class CatPageState(
        val showBigCats: Boolean = true,
        val showMediumCats: Boolean = true,
        val showSmallCats: Boolean = true,
        val showStripedCats: Boolean = true
)

class CatPageStateProvider {

    var latestPageState = CatPageState()
    private set

    private val subject = BehaviorSubject.create<CatPageState>().apply {
        onNext(latestPageState)
    }

    fun observe(): Observable<CatPageState> = subject

    @MainThread
    fun update(pageState: CatPageState) {
        latestPageState = pageState
        subject.onNext(latestPageState)
    }
}