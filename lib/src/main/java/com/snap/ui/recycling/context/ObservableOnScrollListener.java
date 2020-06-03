package com.snap.ui.recycling.context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.OnScrollListener;
import android.util.Pair;

import java.util.HashSet;
import java.util.Set;

import com.snap.ui.recycling.BindingContext;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * BindingContext that provide helper methods for observing scrolling state
 */
public class ObservableOnScrollListener extends OnScrollListener {
    @NonNull
    private final Set<OnScrollListener> scrollListeners = new HashSet<>();

    private final PublishSubject<Integer> scrollStateSubject = PublishSubject.create();
    private final PublishSubject<Pair<Integer, Integer>> scrollOffsetSubject = PublishSubject.create();

    /**
     * Add an scroll listener
     */
    public void addOnScrollListener(@NonNull final OnScrollListener listener) {
        scrollListeners.add(listener);
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        for (OnScrollListener listener : scrollListeners) {
            listener.onScrolled(recyclerView, dx, dy);
        }
        scrollOffsetSubject.onNext(new Pair<>(dx, dy));
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        for (OnScrollListener listener : scrollListeners) {
            listener.onScrollStateChanged(recyclerView, newState);
        }
        scrollStateSubject.onNext(newState);
    }

    /**
     * Returns an observable for all state change following the subscription
     * @return
     */
    public Observable<Integer> observeScrollStateChanges() {
        return scrollStateSubject;
    }

    /**
     * Returns an observable for scrolling position
     */
    public Observable<Pair<Integer, Integer>> observeScrollOffset() {
        return scrollOffsetSubject;
    }
}
