package com.snap.ui.recycling.factory;

import android.content.Context;
import android.content.res.Resources;
import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.core.os.TraceCompat;
import com.snap.ui.recycling.AdapterViewType;
import com.snap.ui.recycling.BindingAdapterViewType;
import com.snap.ui.recycling.BindingContext;
import com.snap.ui.recycling.ContextualViewBinding;
import com.snap.ui.recycling.ViewCreatingBindingAdapterViewType;
import com.snap.ui.recycling.ViewBinding;
import com.snap.ui.recycling.prefetch.ViewHolderPrefetcher;
import com.snap.ui.recycling.prefetch.ViewPrefetcher;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Maps an {@link AdapterViewType} to a {@link View}.
 */
public class ViewFactory {
    private static final String TAG = "ViewFactory";

    /**
     * Psuedo layoutId representing a default container view, created without inflation.
     */
    public static final int DEFAULT_CONTAINER = 0;

    // getEnumConstants() creates a copy of the enum values, so eat the cost up front
    private final List<AdapterViewType[]> mViewTypeValues = new ArrayList<>();
    private final List<Class<? extends AdapterViewType>> mViewTypeClasses = new ArrayList<>();

    private ViewPrefetcher mViewPrefetcher;
    private ViewHolderPrefetcher mViewHolderPrefetcher;
    private WeakReference<LayoutInflater> mLayoutInflaterRef = new WeakReference<>(null);
    private final RecycledViewPool mRecycledViewPool = new RecycledViewPool();
    private final BindingContext mBindingContext;
    private final PublishSubject<AdapterViewType> mViewHolderCreationSubject = PublishSubject.create();

    public ViewFactory(BindingContext bindingContext, Class<? extends AdapterViewType> viewTypes) {
        this(bindingContext, Collections.<Class<? extends AdapterViewType>>singleton(viewTypes));
    }

    public ViewFactory(Class<? extends AdapterViewType> viewTypes) {
        this(new EmptyBindingContext(), Collections.<Class<? extends AdapterViewType>>singleton(viewTypes));
    }

    public ViewFactory(Collection<Class<? extends AdapterViewType>> viewTypes) {
        this(new EmptyBindingContext(), viewTypes);
    }

    public ViewFactory(BindingContext bindingContext, Collection<Class<? extends AdapterViewType>> viewTypes) {
        mBindingContext = bindingContext;
        setAvailableViewTypes(viewTypes);
    }

    public void setViewPrefetcher(ViewPrefetcher viewPrefetcher) {
        this.mViewPrefetcher = viewPrefetcher;
    }

    public ViewPrefetcher getViewPrefetcher() {
        return mViewPrefetcher;
    }

    public void setViewHolderPrefetcher(ViewHolderPrefetcher viewHolderPrefetcher) {
        mViewHolderPrefetcher = viewHolderPrefetcher;
    }

    public ViewHolderPrefetcher getViewHolderPrefetcher() {
        return mViewHolderPrefetcher;
    }

    synchronized void setAvailableViewTypes(Collection<Class<? extends AdapterViewType>> viewTypes) {
        if (viewTypes.isEmpty()) {
            throw new RuntimeException("viewTypes must not be empty");
        }

        mViewTypeValues.clear();
        mViewTypeClasses.clear();

        for (Class<? extends AdapterViewType> vt : viewTypes) {
            TraceCompat.beginSection("addViewTypes");
            addAdapterViewTypes(vt);
            TraceCompat.endSection();
        }
    }

    private void addAdapterViewTypes(Class<? extends AdapterViewType> types) {
        AdapterViewType[] typeArray = types.getEnumConstants();
        mViewTypeValues.add(typeArray);
        mViewTypeClasses.add(types);
    }

    /**
     * The item view type expected by RecyclerView.Adapter.
     */
    public int getViewTypeId(AdapterViewType viewType) {
        final int classCount = mViewTypeClasses.size();
        int seenViewTypes = 0;
        for (int i = 0; i < classCount; i++) {
            Class<? extends AdapterViewType> clazz = mViewTypeClasses.get(i);
            if (viewType.getClass().isAssignableFrom(clazz)) {
                for (AdapterViewType vt : mViewTypeValues.get(i)) {
                    if (viewType == vt) {
                        return seenViewTypes;
                    }
                    seenViewTypes++;
                }
            }

            seenViewTypes += mViewTypeValues.get(i).length;
        }
        throw new IllegalStateException("AdapterViewType not registered: " + viewType.getClass());
    }

    private AdapterViewType getAdapterViewType(int viewType) {
        final int classCount = mViewTypeClasses.size();
        int seenViewTypes = 0;
        for (int i = 0; i < classCount; i++) {
            int classViewTypeCount = mViewTypeValues.get(i).length;
            int offset = viewType - seenViewTypes;
            if (offset < classViewTypeCount) {
                return mViewTypeValues.get(i)[offset];
            }
            seenViewTypes += classViewTypeCount;
        }
        throw new IllegalStateException("Invalid viewType index " + viewType);
    }

    /**
     * Returns a shareable recycled view pool.
     */
    public RecycledViewPool getRecycledViewPool() {
        return mRecycledViewPool;
    }

    @UiThread
    public ViewModelViewHolder createViewHolder(Context context, int viewType, @NonNull ViewGroup parent) {

        try {
            AdapterViewType type = getAdapterViewType(viewType);
            if (mViewHolderCreationSubject.hasObservers()) {
                reportViewHolderCreation(type);
            }
            TraceCompat.beginSection("RC:create: " + type);
            if (mViewHolderPrefetcher != null) {
                ViewModelViewHolder existing = mViewHolderPrefetcher.getPrefetchedViewHolder(type);
                if (existing != null) {
                    return existing;
                }
            }

            View view = getOrCreateView(context, type, parent);
            return createViewHolderForType(this, type, view);
        } finally {
            TraceCompat.endSection();
        }

    }

    public static ViewModelViewHolder createViewHolderForType(ViewFactory viewFactory, AdapterViewType type, View view) {
        if (type instanceof BindingAdapterViewType) {
            try {
                Class<? extends ViewBinding> bindingClass =
                        ((BindingAdapterViewType)type).getViewBindingClass();
                if (bindingClass != null) {
                    if (ContextualViewBinding.class.isAssignableFrom(bindingClass)) {
                        ContextualViewBinding binding = (ContextualViewBinding)bindingClass.newInstance();
                        binding.create(viewFactory.mBindingContext, viewFactory, view);
                        return new BindingViewHolder(view, binding);
                    } else {
                        ViewBinding binding;
                        binding = bindingClass.newInstance();
                        binding.create(view);
                        return new BindingViewHolder(view, binding);
                    }
                } else {
                    return new ViewModelViewHolder(view);
                }
            } catch (Exception e) {
                String viewHierarchy = getViewHierarchy(view);
                throw new RuntimeException("View hierarchy: " + viewHierarchy, e);
            }
        } else {
            return new ViewModelViewHolder(view);
        }
    }

    /**
     * Obtains a view of the given type, using a pre-inflated instance if available.
     */
    @UiThread
    public View getOrCreateView(@NonNull Context context, @NonNull AdapterViewType type, @NonNull ViewGroup parent) {
        int layoutId = type.getLayoutId();
        if (mViewPrefetcher != null) {
            View view = mViewPrefetcher.getPrefetchedView(layoutId);
            if (view != null) {
                return view;
            }
        }

        return createView(context, type, parent, getLayoutInflater(context));
    }

    /**
     * Creates a view for the given {@link AdapterViewType}
     */
    public static View createView(Context context, AdapterViewType type, @NonNull ViewGroup parent, LayoutInflater inflater) {
        if (type instanceof ViewCreatingBindingAdapterViewType) {
            return ((ViewCreatingBindingAdapterViewType)type).createView(parent, inflater);
        }

        int layoutId = type.getLayoutId();
        if (layoutId == DEFAULT_CONTAINER) {
            return new FrameLayout(context);
        } else {
            try {
                return inflater.inflate(layoutId, parent, false);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed to create view for type: " + type + " On Layout " + layoutId, e);
            }
        }
    }

    @UiThread
    private LayoutInflater getLayoutInflater(Context context) {
        if (mLayoutInflaterRef.get() == null) {
                mLayoutInflaterRef = new WeakReference<>(LayoutInflater.from(context));
        }
        return mLayoutInflaterRef.get();
    }

    private static class EmptyBindingContext implements BindingContext {}

    private static String getViewHierarchy(View v) {
        StringBuilder desc = new StringBuilder();
        getViewHierarchy(v, desc, 0);
        return desc.toString();
    }

    private static void getViewHierarchy(View v, StringBuilder desc, int margin) {
        desc.append(getViewMessage(v, margin));
        if (v instanceof ViewGroup) {
            margin++;
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                getViewHierarchy(vg.getChildAt(i), desc, margin);
            }
        }
    }

    private static String getViewMessage(View v, int marginOffset) {
        String repeated = new String(new char[marginOffset]).replace("\0", "  ");
        try {
            String resourceId = v.getResources() != null ? (v.getId() > 0 ? v.getResources().getResourceName(v.getId()) : "no_id") : "no_resources";
            return repeated + '[' + v.getClass().getSimpleName() + "] " + resourceId + '\n';
        } catch (Resources.NotFoundException e) {
            return repeated + '[' + v.getClass().getSimpleName() + "] name_not_found\n";
        }
    }

    private void reportViewHolderCreation(AdapterViewType adapterViewType) {
        mViewHolderCreationSubject.onNext(adapterViewType);
    }

    /**
     * @return Observable that publishes events when a view holder is created
     */
    public Observable<AdapterViewType> observeViewHolderCreation() {
        return mViewHolderCreationSubject.hide();
    }
}
