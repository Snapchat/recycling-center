package com.snap.ui.recycling.adapter;

import androidx.annotation.GuardedBy;
import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.snap.ui.event.AttachToRecyclerViewEvent;
import com.snap.ui.event.EventDispatcher;
import com.snap.ui.recycling.AdapterViewType;
import com.snap.ui.recycling.ObservableSectionController;
import com.snap.ui.recycling.adapter.errorhandling.AdapterErrorHandler;
import com.snap.ui.recycling.factory.ViewFactory;
import com.snap.ui.recycling.factory.ViewModelViewHolder;
import com.snap.ui.recycling.viewmodel.AdapterViewModel;
import com.snap.ui.seeking.Seekable;
import com.snap.ui.seeking.Seekables;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.functions.Consumer;

/**
 * Wrapper of {@link SectionedRecyclerViewAdapter} that adds support for Observable sections.
 * When this adapter is disposed, each section and the Observable from
 * {@link ObservableSectionController#getViewModels()} are also disposed.
 *
 * Deprecated in favor of {@link ObservableViewModelSectionAdapter}, which runs DiffUtils on a background thread
 * and has thread-safe methods for updating sections.
 */
@Deprecated
public class LegacyObservableViewModelSectionAdapter implements ViewModelAdapter, Disposable {

    final SectionedRecyclerViewAdapter adapter;
    final Scheduler computationScheduler;

    @GuardedBy("this")
    private boolean subscribed;

    private final AtomicBoolean disposed = new AtomicBoolean(false);
    private final LinkedHashMap<ObservableSectionController, Disposable> sections = new LinkedHashMap<>();
    private final SectionErrorHandler sectionErrorHandler;

    /**
     * <p>If a Section's Observable experiences an error, there's not much the Adapter can do about it.
     * That Observable will no longer be able to emit updates, since it has reached a terminal state.
     *
     * <p>Errors can be handled in an Observable with {@link io.reactivex.Observable#onErrorResumeNext(ObservableSource)}
     * or similar, in which case the Observable is transformed before this Adapter ever sees the errror.
     *
     * <p>Alternatively, this error handler routes control back to the caller, which could then re-activate the section via
     * {@link #updateSection(ObservableSectionController)}, remove the failed section, or drop the error.
     */
    public interface SectionErrorHandler {
        void onError(LegacyObservableViewModelSectionAdapter adapter, ObservableSectionController section, Throwable error);
    }

    public LegacyObservableViewModelSectionAdapter(ViewFactory viewFactory,
            EventDispatcher eventDispatcher,
            Scheduler computationScheduler) {
        this(viewFactory, eventDispatcher, computationScheduler, Collections.<ObservableSectionController>emptyList());
    }

    public LegacyObservableViewModelSectionAdapter(ViewFactory viewFactory,
                                             EventDispatcher eventDispatcher,
                                             Scheduler computationScheduler,
                                             List<ObservableSectionController> controllers) {
        this(viewFactory, eventDispatcher, computationScheduler, controllers, null, null);
    }

    public LegacyObservableViewModelSectionAdapter(ViewFactory viewFactory,
            EventDispatcher eventDispatcher,
            Scheduler computationScheduler,
            List<ObservableSectionController> controllers,
            @Nullable SectionErrorHandler errorHandler,
            @Nullable AdapterErrorHandler adapterErrorHandler) {
        this.computationScheduler = computationScheduler;
        sectionErrorHandler = errorHandler;
        adapter = new SectionedRecyclerViewAdapter(viewFactory, eventDispatcher, adapterErrorHandler, controllers);
        for (ObservableSectionController controller : controllers) {
            sections.put(controller, Disposables.disposed());
            if (subscribed) {
                subscribeToSection(controller);
            }
        }
    }

    /**
     * Ideally ViewBinding should be under 1ms per RecyclerView entry. In practice this
     * can be difficult to achieve. Asychronous binding attempts to schedule initial bindings
     * on different main thread cycles, to try and preserve user interactivity and animations.
     */
    public void setAsyncBinding(boolean asyncBinding) {
        adapter.setAsyncBinding(asyncBinding);
    }

    public RecyclerView.Adapter<ViewModelViewHolder> getRecyclerViewAdapter() {
        return adapter.getRecyclerViewAdapter();
    }

    @MainThread
    public synchronized void addSections(List<ObservableSectionController> controllers) {
        for (ObservableSectionController controller : controllers) {
            addSection(controller);
        }
    }

    @MainThread
    public synchronized void addSection(ObservableSectionController controller) {
        sections.put(controller, Disposables.disposed());
        adapter.updateSection(controller, Seekables.<AdapterViewModel>empty());
        if (subscribed) {
            subscribeToSection(controller);
        }
    }

    public Set<ObservableSectionController> getSections() {
        return sections.keySet();
    }

    /**
     * Re-subscribes the given section.
     */
    @MainThread
    public synchronized void updateSection(ObservableSectionController controller) {
        disposeSection(controller);
        if (subscribed) {
            subscribeToSection(controller);
        }
    }

    /**
     * Removes the given section and its views from the adapter.
     */
    @MainThread
    public synchronized void removeSection(ObservableSectionController controller) {
        disposeSection(controller);
        controller.dispose();
        sections.remove(controller);
        adapter.removeSection(controller);
    }

    private void disposeSection(ObservableSectionController controller) {
        Disposable existing = sections.get(controller);
        if (existing == null) {
            throw new IllegalArgumentException("Section not registered");
        }
        existing.dispose();
    }

    /**
     * Clears the given section in the adapter.
     */
    @MainThread
    public synchronized void clearSection(ObservableSectionController controller) {
        adapter.updateSection(controller, Seekables.<AdapterViewModel>empty());
    }

    public synchronized Disposable subscribe() {
        subscribed = true;
        for (ObservableSectionController controller : sections.keySet()) {
            subscribeToSection(controller);
        }
        return this;
    }

    private synchronized void subscribeToSection(final ObservableSectionController controller) {
        sections.put(
                controller,
                controller.getViewModels()
                    .subscribeOn(computationScheduler)
                    .toFlowable(BackpressureStrategy.LATEST)
                    .observeOn(AndroidSchedulers.mainThread(), false, 1)
                    .subscribe(new Consumer<Seekable<AdapterViewModel>>() {
                        @Override
                        public void accept(Seekable<AdapterViewModel> adapterViewModels) {
                            adapter.updateSection(controller, adapterViewModels);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            if (sectionErrorHandler != null) {
                                sectionErrorHandler.onError(LegacyObservableViewModelSectionAdapter.this, controller, throwable);
                            } else {
                                throw new RuntimeException(throwable);
                            }
                        }
                    })
        );
    }

    @Override
    public synchronized void dispose() {
        if (disposed.compareAndSet(false, true)) {
            for (Map.Entry<ObservableSectionController, Disposable> entry : sections.entrySet()) {
                entry.getKey().dispose();
                entry.getValue().dispose();
            }
            subscribed = false;
        }
    }

    @Override
    public synchronized boolean isDisposed() {
        return disposed.get();
    }

    @Override
    public AdapterViewType getItemAdapterViewType(int position) {
        return adapter.getItemAdapterViewType(position);
    }

    @Override
    public AdapterViewModel getItemViewModel(int position) {
        return adapter.getItemViewModel(position);
    }

    @Override
    public int getItemCount() {
        return adapter.getItemCount();
    }

    public Observable<AttachToRecyclerViewEvent> observeAttachToRecyclerViewEvents() {
        return adapter.observeAttachToRecyclerViewEvents();
    }
}
