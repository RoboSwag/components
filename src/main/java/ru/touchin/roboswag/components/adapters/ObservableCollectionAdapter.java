/*
 *  Copyright (c) 2015 RoboSwag (Gavriil Sitnikov, Vsevolod Ivanov)
 *
 *  This file is part of RoboSwag library.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package ru.touchin.roboswag.components.adapters;

import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import ru.touchin.roboswag.components.utils.LifecycleBindable;
import ru.touchin.roboswag.components.utils.UiUtils;
import ru.touchin.roboswag.core.log.Lc;
import ru.touchin.roboswag.core.observables.collections.Change;
import ru.touchin.roboswag.core.observables.collections.ObservableCollection;
import ru.touchin.roboswag.core.observables.collections.ObservableList;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Actions;
import rx.subjects.BehaviorSubject;

/**
 * Created by Gavriil Sitnikov on 20/11/2015.
 * TODO: fill description
 */
public abstract class ObservableCollectionAdapter<TItem, TViewHolder extends ObservableCollectionAdapter.ViewHolder>
        extends RecyclerView.Adapter<BindableViewHolder> {

    private static final int PRE_LOADING_COUNT = 10;

    private static final int LIST_ITEM_TYPE = 12313212;

    @NonNull
    private final BehaviorSubject<ObservableCollection<TItem>> observableCollectionSubject
            = BehaviorSubject.create((ObservableCollection<TItem>) null);
    @NonNull
    private final LifecycleBindable lifecycleBindable;
    @Nullable
    private OnItemClickListener<TItem> onItemClickListener;
    private int lastUpdatedChangeNumber = -1;
    @NonNull
    private final Observable<?> historyPreLoadingObservable;

    @NonNull
    private final ObservableList<TItem> innerCollection = new ObservableList<>();
    private boolean anyChangeApplied;
    @NonNull
    private final List<RecyclerView> attachedRecyclerViews = new LinkedList<>();

    public ObservableCollectionAdapter(@NonNull final LifecycleBindable lifecycleBindable) {
        super();
        this.lifecycleBindable = lifecycleBindable;
        innerCollection.observeChanges().subscribe(this::onItemsChanged);
        lifecycleBindable.untilDestroy(observableCollectionSubject
                        .doOnNext(collection -> innerCollection.set(collection != null ? collection.getItems() : new ArrayList<>()))
                        .<ObservableCollection.CollectionChange<TItem>>switchMap(observableCollection -> observableCollection != null
                                ? observableCollection.observeChanges().observeOn(AndroidSchedulers.mainThread())
                                : Observable.empty()),
                changes -> {
                    anyChangeApplied = true;
                    for (final Change<TItem> change : changes.getChanges()) {
                        switch (change.getType()) {
                            case INSERTED:
                                innerCollection.addAll(change.getStart(), change.getChangedItems());
                                break;
                            case CHANGED:
                                innerCollection.update(change.getStart(), change.getChangedItems());
                                break;
                            case REMOVED:
                                innerCollection.remove(change.getStart(), change.getCount());
                                break;
                            default:
                                Lc.assertion("Not supported " + change.getType());
                                break;
                        }
                    }
                });
        historyPreLoadingObservable = observableCollectionSubject
                .switchMap(observableCollection -> {
                    final int size = observableCollection.size();
                    return observableCollection.loadRange(size, size + PRE_LOADING_COUNT);
                });
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        attachedRecyclerViews.add(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull final RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        attachedRecyclerViews.remove(recyclerView);
    }

    @NonNull
    public LifecycleBindable getLifecycleBindable() {
        return lifecycleBindable;
    }

    protected long getItemClickDelay() {
        return UiUtils.RIPPLE_EFFECT_DELAY;
    }

    public void setItems(@NonNull final List<TItem> items) {
        setObservableCollection(new ObservableList<>(items));
    }

    @Nullable
    public ObservableCollection<TItem> getObservableCollection() {
        return observableCollectionSubject.getValue();
    }

    @NonNull
    public Observable<ObservableCollection<TItem>> observeObservableCollection() {
        return observableCollectionSubject.distinctUntilChanged();
    }

    protected int getHeadersCount() {
        return 0;
    }

    protected int getFootersCount() {
        return 0;
    }

    private void refreshUpdate() {
        notifyDataSetChanged();
        lastUpdatedChangeNumber = innerCollection.getChangesCount();
    }

    public void setObservableCollection(@Nullable final ObservableCollection<TItem> observableCollection) {
        this.observableCollectionSubject.onNext(observableCollection);
        refreshUpdate();
    }

    private boolean anyRecyclerViewShown() {
        for (final RecyclerView recyclerView : attachedRecyclerViews) {
            if (recyclerView.isShown()) {
                return true;
            }
        }
        return false;
    }

    protected void onItemsChanged(@NonNull final ObservableCollection.CollectionChange<TItem> collectionChange) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            Lc.assertion("Items changes called on not main thread");
            return;
        }
        if (!anyChangeApplied || !anyRecyclerViewShown()) {
            anyChangeApplied = true;
            refreshUpdate();
            return;
        }
        if (collectionChange.getNumber() != innerCollection.getChangesCount()
                || collectionChange.getNumber() != lastUpdatedChangeNumber + 1) {
            if (lastUpdatedChangeNumber < collectionChange.getNumber()) {
                refreshUpdate();
            }
            return;
        }
        notifyAboutChanges(collectionChange.getChanges());
        lastUpdatedChangeNumber = innerCollection.getChangesCount();
    }

    private void notifyAboutChanges(@NonNull final Collection<Change<TItem>> changes) {
        for (final Change change : changes) {
            switch (change.getType()) {
                case INSERTED:
                    notifyItemRangeInserted(change.getStart() + getHeadersCount(), change.getCount());
                    break;
                case CHANGED:
                    notifyItemRangeChanged(change.getStart() + getHeadersCount(), change.getCount());
                    break;
                case REMOVED:
                    if (getItemCount() == 0) {
                        //TODO: bug of recyclerview?
                        notifyDataSetChanged();
                    } else {
                        notifyItemRangeRemoved(change.getStart() + getHeadersCount(), change.getCount());
                    }
                    break;
                default:
                    Lc.assertion("Not supported " + change.getType());
                    break;
            }
        }
    }

    public void setOnItemClickListener(@Nullable final OnItemClickListener<TItem> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
        refreshUpdate();
    }

    @Override
    public int getItemViewType(final int position) {
        return LIST_ITEM_TYPE;
    }

    @Override
    public BindableViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return onCreateItemViewHolder(parent, viewType);
    }

    public abstract TViewHolder onCreateItemViewHolder(@NonNull ViewGroup parent, int viewType);

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(final BindableViewHolder holder, final int position) {
        lastUpdatedChangeNumber = innerCollection.getChangesCount();

        if (position - getHeadersCount() >= innerCollection.size()) {
            return;
        }
        final TItem item = innerCollection.get(position - getHeadersCount());

        onBindItemToViewHolder((TViewHolder) holder, position, item);
        ((TViewHolder) holder).bindPosition(position);
        if (onItemClickListener != null && !isOnClickListenerDisabled(item)) {
            UiUtils.setOnRippleClickListener(holder.itemView, () -> onItemClickListener.onItemClicked(item, position), getItemClickDelay());
        }
    }

    protected abstract void onBindItemToViewHolder(@NonNull TViewHolder holder, int position, @NonNull TItem item);

    @Nullable
    public TItem getItem(final int position) {
        final int positionInList = position - getHeadersCount();
        return positionInList < 0 || positionInList >= innerCollection.size() ? null : innerCollection.get(positionInList);
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + innerCollection.size() + getFootersCount();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull final BindableViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.onAttachedToWindow();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull final BindableViewHolder holder) {
        holder.onDetachedFromWindow();
        super.onViewDetachedFromWindow(holder);
    }

    public boolean isOnClickListenerDisabled(@NonNull final TItem item) {
        return false;
    }

    public interface OnItemClickListener<TItem> {

        void onItemClicked(@NonNull TItem item, int position);

    }

    public class ViewHolder extends BindableViewHolder {

        @Nullable
        private Subscription historyPreLoadingSubscription;

        public ViewHolder(@NonNull final LifecycleBindable baseBindable, @NonNull final View itemView) {
            super(baseBindable, itemView);
        }

        @SuppressWarnings("unchecked")
        public void bindPosition(final int position) {
            if (historyPreLoadingSubscription != null) {
                historyPreLoadingSubscription.unsubscribe();
                historyPreLoadingSubscription = null;
            }
            if (position - getHeadersCount() > innerCollection.size() - PRE_LOADING_COUNT) {
                historyPreLoadingSubscription = bind(historyPreLoadingObservable, Actions.empty());
            }
        }

    }

}
