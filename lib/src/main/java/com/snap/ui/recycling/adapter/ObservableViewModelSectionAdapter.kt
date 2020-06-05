package com.snap.ui.recycling.adapter

import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.core.os.TraceCompat
import com.snap.ui.event.AttachToRecyclerViewEvent
import com.snap.ui.event.EventDispatcher
import com.snap.ui.recycling.ObservableSectionController
import com.snap.ui.recycling.adapter.errorhandling.AdapterErrorHandler
import com.snap.ui.recycling.adapter.errorhandling.AdapterErrorInfo
import com.snap.ui.recycling.adapter.errorhandling.SectionErrorHandler
import com.snap.ui.recycling.factory.ViewFactory
import com.snap.ui.recycling.factory.ViewModelViewHolder
import com.snap.ui.recycling.viewmodel.AdapterViewModel
import com.snap.ui.seeking.ListSeekable
import com.snap.ui.seeking.Seekable
import com.snap.ui.seeking.Seekables
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

/**
 * A RecyclerView adapter that hosts [AdapterViewModel] models provided by reactive
 * section controllers. Each section is an [ObservableSectionController] that emits a logical grouping of ViewModels
 * as a [Seekable] of [AdapterViewModel]s.
 */
open class ObservableViewModelSectionAdapter
@JvmOverloads constructor(
    private val viewFactory: ViewFactory,
    private val eventDispatcher: EventDispatcher,
    private val computationScheduler: Scheduler = Schedulers.computation(),
    private val mainThreadScheduler: Scheduler = AndroidSchedulers.mainThread(),
    defaultSections: List<ObservableSectionController>? = emptyList(),
    private val errorHandler: AdapterErrorHandler? = null,
    private val sectionErrorHandler: SectionErrorHandler? = null
) : RecyclerView.Adapter<ViewModelViewHolder<in AdapterViewModel>>(), ViewModelAdapter {

    private val generation = AtomicLong(0)
    private val mainThreadData: AtomicReference<Seekable<SectionItemInfo>> = AtomicReference(Seekables.empty())

    private val sectionsSource = BehaviorSubject.createDefault(defaultSections)

    private val disposables = CompositeDisposable().apply {
        defaultSections?.forEach { add(it) }
    }

    private val emptySeekable = Seekables.copyOf(emptyList<AdapterViewModel>())
    private val emptyObservable = Observable.just(emptySeekable)

    init {
        // Rely on AdapterViewModel's item id as our stable id by default.
        // createUniqueId() is responsible for uniqifying this id across model types and sections.
        setHasStableIds(true)
    }

    fun addSection(section: ObservableSectionController) {
        disposables.add(section)
        sectionsSource.onNext(sectionsSource.value!!.toMutableList().apply { add(section) })
    }

    fun addSections(sections: List<ObservableSectionController>) {
        sections.forEach { disposables.add(it) }
        sectionsSource.onNext(sectionsSource.value!!.toMutableList().apply { addAll(sections) })
    }

    fun removeSection(section: ObservableSectionController) {
        sectionsSource.onNext(sectionsSource.value!!.toMutableList().apply { remove(section) })
    }

    fun setSections(sections: List<ObservableSectionController>) {
        sectionsSource.onNext(sections)
    }

    /**
     * True if sections should prevent rendering if previous sections have not yet rendered.
     * A section is considered 'rendered' whenever its Observable<AdapterViewModel> has emitted some results,
     * even if it emits an empty list of models.
     */
    @Volatile
    var loadSectionsInOrder = false

    fun subscribe(): Disposable {
        return sectionsSource
            .observeOn(computationScheduler)
            .switchMap(::combineSections)
            .debounce(2, TimeUnit.MILLISECONDS, computationScheduler)
            .map(::calculateDiff)
            .filter { it.diff != null }
            .observeOn(mainThreadScheduler)
            .doOnDispose {
                disposables.dispose()
                sectionsSource.onNext(emptyList())
                onDispose()
            }
            .subscribe(::applyAdapterUpdates)
    }

    private fun combineSections(sections: List<ObservableSectionController>): Observable<ItemUpdates> {
        val modelSources = sections.mapIndexed { sectionIndex, section ->
            // Associate each ViewModel with its section
            section.getViewModels()
                    .map { it as Seekable<AdapterViewModel> }
                    .startWith(emptyObservable)
                    .observeOn(computationScheduler)
                    .map { models -> trace("section:$sectionIndex") {
                        if (models === emptySeekable) {
                            emptySeekable
                        } else {
                            ListSeekable(models.map { model ->
                                val viewTypeId = viewFactory.getViewTypeId(model.type)
                                SectionItemInfo(
                                        section,
                                        model,
                                        viewTypeId,
                                        createUniqueId(model, viewTypeId, sectionIndex)
                                )
                            })
                        }
                    } }
                .doOnError { sectionErrorHandler?.onError(section.javaClass, it) }
        }

        return combineLatest(modelSources) { array ->
            var currentGen = 0L
            var currentData: Seekable<SectionItemInfo> = Seekables.empty()

            // If results have not come back from any sections, don't notify the adapter
            val hasResults = array.isNotEmpty() &&
                    if (loadSectionsInOrder) array[0] !== emptySeekable
                    else array.any { it !== emptySeekable }
            if (!hasResults) {
                return@combineLatest ItemUpdates(Seekables.empty(), Seekables.empty(), -1)
            }

            // Increment the generation counter and diff against the current main-thread data.
            // If we return to the main thread and the generation value is current, the main-thread data
            // will match the base data we used in DiffUtils.
            synchronized(generation) {
                currentGen = generation.incrementAndGet()
                currentData = mainThreadData.get()
            }

            val sources = array.toList() as List<Seekable<SectionItemInfo>>
            val included = if (loadSectionsInOrder) sources.takeWhile { (it !== emptySeekable) } else sources
            val results = Seekables.concat(included)
            ItemUpdates(currentData, results, currentGen)
        }
    }

    /**
     * Generates a unique id for a model, for use by RecyclerView.Adapter's stableIds.
     * The data model id must be unique for a given viewType and section. This means
     * a data model can have the same id as long as the viewTypes are different-- useful
     * when using a database rowId to represent different types of data in the same RecyclerView,
     * also allowing a model to be used across sections, even with matching viewType.
     */
    private fun createUniqueId(model: AdapterViewModel, viewTypeId: Int, sectionId: Int): Long {
        return model.id xor (sectionId.toLong() shl 40) xor (viewTypeId.toLong() shl 52)
    }

    private fun calculateDiff(updates: ItemUpdates): SectionUpdates {
        val diff = if (updates.generation == generation.get()) {
            val callback = DiffUtilCallback(updates.old, updates.data)
            trace("diff:calc") { DiffUtil.calculateDiff(callback, false) }
        } else {
            null
        }
        return SectionUpdates(updates.data, diff, updates.generation)
    }

    @MainThread
    private fun applyAdapterUpdates(info: SectionUpdates) {
        if (info.diff != null) {
            val current = synchronized(generation) {
                if (info.generation == generation.get()) {
                    mainThreadData.set(info.data)
                    true
                } else {
                    false
                }
            }
            if (current) {
                mainThreadData.set(info.data)
                info.diff.dispatchUpdatesTo(this)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewModelViewHolder<in AdapterViewModel> {
        try {
            return viewFactory.createViewHolder(parent.context, viewType, parent)
        } catch (e: Exception) {
            return errorHandler?.let {
                errorHandler.onError(e, null)
                errorHandler.getViewModelViewHolder(parent.context)
            } ?: throw e
        }
    }

    override fun onBindViewHolder(holder: ViewModelViewHolder<in AdapterViewModel>, position: Int) {
        try {
            TraceCompat.beginSection("RC:bind")
            val info = mainThreadData.get()[position]
            holder.bind(info.model, eventDispatcher, info.section)
        } catch (e: Exception) {
            errorHandler?.let {
                errorHandler.onError(e, AdapterErrorInfo(position))
            } ?: throw e
        } finally {
            TraceCompat.endSection()
        }
    }

    override fun getItemCount() = mainThreadData.get().size()

    override fun getItemId(position: Int) = mainThreadData.get()[position].itemUniqueId

    override fun getItemAdapterViewType(position: Int) = mainThreadData.get()[position].model.type

    override fun getItemViewType(position: Int) = mainThreadData.get()[position].itemViewType

    override fun getItemViewModel(position: Int) = mainThreadData.get()[position].model

    fun getItemSection(position: Int) = mainThreadData.get()[position].section

    override fun onViewRecycled(holder: ViewModelViewHolder<in AdapterViewModel>) {
        try {
            holder.recycle()
        } catch (e: Exception) {
            errorHandler?.let {
                errorHandler.onError(e, null)
            } ?: throw e
        }
    }

    override fun onFailedToRecycleView(holder: ViewModelViewHolder<in AdapterViewModel>): Boolean {
        try {
            return holder.onFailedToRecycleView()
        } catch (e: Exception) {
            errorHandler?.let {
                errorHandler.onError(e, null)
                return true
            } ?: throw e
        }
    }

    class SectionItemInfo(
        val section: ObservableSectionController,
        val model: AdapterViewModel,
        val itemViewType: Int,
        val itemUniqueId: Long
    )

    class ItemUpdates(
        val old: Seekable<SectionItemInfo>,
        val data: Seekable<SectionItemInfo>,
        val generation: Long
    )

    // A Null DiffResult means the section updates did not complete.
    class SectionUpdates(
        val data: Seekable<SectionItemInfo>,
        val diff: DiffUtil.DiffResult?,
        val generation: Long
    )

    private class DiffUtilCallback(
        val oldModels: Seekable<SectionItemInfo>,
        val newModels: Seekable<SectionItemInfo>
    ) : DiffUtil.Callback() {

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val oldItem = oldModels[oldItemPosition]
            val newItem = newModels[newItemPosition]
            if (oldItem.section !== newItem.section) {
                return false
            }

            return oldItem.model.areItemsTheSame(newItem.model)
        }

        override fun getOldListSize() = oldModels.size()

        override fun getNewListSize() = newModels.size()

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val first = oldModels[oldItemPosition]
            val second = newModels[newItemPosition]
            return first.model.areContentsTheSame(second.model)
        }

        override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int) = oldModels[oldItemPosition]
    }

    private val mAttachPublisher = PublishSubject.create<AttachToRecyclerViewEvent>()

    fun observeAttachToRecyclerViewEvents(): Observable<AttachToRecyclerViewEvent> {
        return mAttachPublisher
    }

    open fun onDispose() { }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mAttachPublisher.onNext(AttachToRecyclerViewEvent(AttachToRecyclerViewEvent.EventType.ATTACH, recyclerView))
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        mAttachPublisher.onNext(AttachToRecyclerViewEvent(AttachToRecyclerViewEvent.EventType.DETACH, recyclerView))
    }
}

/** Captures a given block of code using TraceCompat, for Systrace and S2R. */
private inline fun <R> trace(sectionName: String, section: () -> R): R {
    TraceCompat.beginSection(sectionName)
    try {
        return section.invoke()
    } finally {
        TraceCompat.endSection()
    }
}

inline fun <T1, R> combineLatest(sources: List<ObservableSource<T1>>, crossinline combineFunction: (Array<Any>) -> R) =
        Observable.combineLatest(sources) { t1: Array<Any> -> combineFunction(t1) }!!
