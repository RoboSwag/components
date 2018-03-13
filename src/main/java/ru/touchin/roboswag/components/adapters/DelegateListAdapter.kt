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

package ru.touchin.roboswag.components.adapters

import android.support.v7.recyclerview.extensions.ListAdapter
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

import java.util.ArrayList
import java.util.Collections
import java.util.LinkedList

import io.reactivex.functions.BiConsumer
import io.reactivex.functions.Consumer
import ru.touchin.roboswag.components.utils.UiUtils
import ru.touchin.roboswag.components.utils.lifecycle.Stopable
import android.support.v7.util.DiffUtil
import ru.touchin.roboswag.core.log.Lc
import ru.touchin.roboswag.core.observables.collections.ObservableCollection
import ru.touchin.roboswag.core.observables.collections.loadable.LoadingMoreList
import ru.touchin.roboswag.core.utils.ShouldNotHappenException

/**
 * Created by Gavriil Sitnikov on 20/11/2015.
 * Adapter based on [ObservableCollection] and providing some useful features like:
 * - item-based binding method;
 * - delegates by [AdapterDelegate] over itemViewType logic;
 * - item click listener setup by [.setOnItemClickListener];
 * - allows to inform about footers/headers by overriding base create/bind methods and [.getHeadersCount] plus [.getFootersCount];
 * - by default it is pre-loading items for collections like [LoadingMoreList].
 *
 * @param <TItem>           Type of items to bind to ViewHolders;
 * @param <TItemViewHolder> Type of ViewHolders to show items.
</TItemViewHolder></TItem> */
abstract//TooManyMethods: it's ok
class DelegateListAdapter<TItem, TItemViewHolder : BindableViewHolder>(
        val stopable: Stopable,
        diffCallback: DiffUtil.ItemCallback<TItem>) : ListAdapter<TItem, TItemViewHolder>(diffCallback) {

    companion object {
        /**
         * Enables debugging features like checking concurrent delegates.
         */
        var inDebugMode: Boolean = false
    }

    private var onItemClickListener: Any? = null
    private var itemClickDelayMillis: Long = 0
    private val attachedRecyclerViews = LinkedList<RecyclerView>()
    private val delegates = ArrayList<AdapterDelegate<out BindableViewHolder>>()
    private var items: List<TItem> = listOf()

    /**
     * Headers count goes before items.
     */
    protected val headersCount: Int = 0

    /**
     * Footers count goes after items and headers.
     */
    protected val footersCount: Int = 0

    override fun submitList(list: List<TItem>?) {
        super.submitList(list)
        items = list ?: listOf()
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        attachedRecyclerViews.add(recyclerView)
    }

    private fun anyRecyclerViewShown(): Boolean = attachedRecyclerViews.any { it.isShown }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        attachedRecyclerViews.remove(recyclerView)
    }

    /**
     * Returns list of added delegates.
     *
     * @return List of [AdapterDelegate].
     */
    fun getDelegates(): List<AdapterDelegate<out BindableViewHolder>> = Collections.unmodifiableList(delegates)

    /**
     * Adds [ItemAdapterDelegate] to adapter.
     *
     * @param delegate Delegate to add.
     */
    fun addDelegate(delegate: ItemAdapterDelegate<out TItemViewHolder, out TItem>) {
        addDelegateInternal(delegate)
    }

    /**
     * Adds [PositionAdapterDelegate] to adapter.
     *
     * @param delegate Delegate to add.
     */
    fun addDelegate(delegate: PositionAdapterDelegate<out BindableViewHolder>) {
        addDelegateInternal(delegate)
    }

    private fun addDelegateInternal(delegate: AdapterDelegate<out BindableViewHolder>) {
        if (inDebugMode) {
            for (addedDelegate in delegates) {
                if (addedDelegate.itemViewType == delegate.itemViewType) {
                    Lc.assertion("AdapterDelegate with viewType=" + delegate.itemViewType + " already added")
                    return
                }
            }
        }
        delegates.add(delegate)
        notifyDataSetChanged()
    }

    /**
     * Removes [AdapterDelegate] from adapter.
     *
     * @param delegate Delegate to remove.
     */
    fun removeDelegate(delegate: AdapterDelegate<out BindableViewHolder>) {
        delegates.remove(delegate)
        notifyDataSetChanged()
    }

    private fun checkDelegates(alreadyPickedDelegate: AdapterDelegate<*>?, currentDelegate: AdapterDelegate<*>) {
        if (alreadyPickedDelegate != null) {
            throw ShouldNotHappenException("Concurrent delegates: $currentDelegate and $alreadyPickedDelegate")
        }
    }

    private fun getItemPositionInCollection(positionInAdapter: Int): Int {
        val shiftedPosition = positionInAdapter - headersCount
        return if (shiftedPosition >= 0 && shiftedPosition < items.size) shiftedPosition else -1
    }

    override//Complexity: because of debug code
    fun getItemViewType(positionInAdapter: Int): Int {
        var delegateOfViewType: AdapterDelegate<*>? = null
        val positionInCollection = getItemPositionInCollection(positionInAdapter)
        val item = if (positionInCollection >= 0) items[positionInCollection] else null
        for (delegate in delegates) {
            if (delegate is ItemAdapterDelegate<*, *>) {
                if (item != null && delegate.isForViewType(item, positionInAdapter, positionInCollection)) {
                    checkDelegates(delegateOfViewType, delegate)
                    delegateOfViewType = delegate
                    if (!inDebugMode) {
                        break
                    }
                }
            } else if (delegate is PositionAdapterDelegate<*>) {
                if (delegate.isForViewType(positionInAdapter)) {
                    checkDelegates(delegateOfViewType, delegate)
                    delegateOfViewType = delegate
                    if (!inDebugMode) {
                        break
                    }
                }
            } else {
                Lc.assertion("Delegate of type " + delegate.javaClass)
            }
        }

        return if (delegateOfViewType != null) delegateOfViewType.itemViewType else super.getItemViewType(positionInAdapter)
    }

    override fun getItemId(positionInAdapter: Int): Long {
        val result = LongContainer()
        tryDelegateAction(positionInAdapter,
                BiConsumer { itemAdapterDelegate, positionInCollection ->
                    result.value = itemAdapterDelegate.getItemId(items[positionInCollection],
                            positionInAdapter, positionInCollection)
                },
                Consumer { positionAdapterDelegate -> result.value = positionAdapterDelegate.getItemId(positionInAdapter) },
                Consumer { positionInCollection -> result.value = super.getItemId(positionInAdapter) })
        return result.value
    }

    @Suppress("UNCHECKED_CAST")
    private fun tryDelegateAction(positionInAdapter: Int,
                                  itemAdapterDelegateAction: BiConsumer<ItemAdapterDelegate<TItemViewHolder, TItem>, Int>,
                                  positionAdapterDelegateAction: Consumer<PositionAdapterDelegate<TItemViewHolder>>,
                                  defaultAction: Consumer<Int>) {
        val viewType = getItemViewType(positionInAdapter)
        val positionInCollection = getItemPositionInCollection(positionInAdapter)
        for (delegate in delegates) {
            if (delegate is ItemAdapterDelegate<*, *>) {
                if (positionInCollection >= 0 && viewType == delegate.getItemViewType()) {
                    try {
                        itemAdapterDelegateAction.accept(delegate as ItemAdapterDelegate<TItemViewHolder, TItem>, positionInCollection)
                    } catch (exception: Exception) {
                        Lc.assertion(exception)
                    }

                    return
                }
            } else if (delegate is PositionAdapterDelegate<*>) {
                if (viewType == delegate.getItemViewType()) {
                    try {
                        positionAdapterDelegateAction.accept(delegate as PositionAdapterDelegate<TItemViewHolder>)
                    } catch (exception: Exception) {
                        Lc.assertion(exception)
                    }

                    return
                }
            } else {
                Lc.assertion("Delegate of type " + delegate.javaClass)
            }
        }
        try {
            defaultAction.accept(positionInCollection)
        } catch (exception: Exception) {
            Lc.assertion(exception)
        }

    }

    override fun getItemCount(): Int = headersCount + items.size + footersCount

    @Suppress("UNCHECKED_CAST")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TItemViewHolder {
        for (delegate in delegates) {
            if (delegate.itemViewType == viewType) {
                return delegate.onCreateViewHolder(parent) as TItemViewHolder
            }
        }
        throw ShouldNotHappenException("Add some AdapterDelegate or override this method")
    }

    override fun onBindViewHolder(holder: TItemViewHolder, positionInAdapter: Int) {
        tryDelegateAction(positionInAdapter,
                BiConsumer { itemAdapterDelegate, positionInCollection ->
                    bindItemViewHolder(itemAdapterDelegate, holder, items[positionInCollection], null, positionInAdapter, positionInCollection)
                },
                Consumer { positionAdapterDelegate -> positionAdapterDelegate.onBindViewHolder(holder, positionInAdapter) },
                Consumer { positionInCollection ->
                    if (positionInCollection >= 0) {
                        bindItemViewHolder(null, holder, items[positionInCollection], null, positionInAdapter, positionInCollection)
                    }
                })
    }

    override fun onBindViewHolder(holder: TItemViewHolder, positionInAdapter: Int, payloads: List<Any>) {
        super.onBindViewHolder(holder, positionInAdapter, payloads)
        tryDelegateAction(positionInAdapter,
                BiConsumer { itemAdapterDelegate, positionInCollection ->
                    bindItemViewHolder(itemAdapterDelegate, holder, items[positionInCollection],
                            payloads, positionInAdapter, positionInCollection)
                },
                Consumer { positionAdapterDelegate -> positionAdapterDelegate.onBindViewHolder(holder, positionInAdapter) },
                Consumer { positionInCollection ->
                    if (positionInCollection >= 0) {
                        bindItemViewHolder(null, holder, items[positionInCollection],
                                payloads, positionInAdapter, positionInCollection)
                    }
                })
    }

    @Suppress("UNCHECKED_CAST")
    private fun bindItemViewHolder(itemAdapterDelegate: ItemAdapterDelegate<TItemViewHolder, TItem>?,
                                   holder: BindableViewHolder, item: TItem, payloads: List<Any>?,
                                   positionInAdapter: Int, positionInCollection: Int) {
        val itemViewHolder: TItemViewHolder
        try {
            itemViewHolder = holder as TItemViewHolder
        } catch (exception: ClassCastException) {
            Lc.assertion(exception)
            return
        }

        updateClickListener(holder, item, positionInAdapter, positionInCollection)
        if (itemAdapterDelegate != null) {
            if (payloads == null) {
                itemAdapterDelegate.onBindViewHolder(itemViewHolder, item, positionInAdapter, positionInCollection)
            } else {
                itemAdapterDelegate.onBindViewHolder(itemViewHolder, item, payloads, positionInAdapter, positionInCollection)
            }
        } else {
            if (payloads == null) {
                onBindItemToViewHolder(itemViewHolder, positionInAdapter, item)
            } else {
                onBindItemToViewHolder(itemViewHolder, positionInAdapter, item, payloads)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun updateClickListener(holder: BindableViewHolder, item: TItem,
                                    positionInAdapter: Int, positionInCollection: Int) {
        if (onItemClickListener != null && !isOnClickListenerDisabled(item, positionInAdapter, positionInCollection)) {
            UiUtils.setOnRippleClickListener(holder.itemView,
                    { any ->
                        when (onItemClickListener) {
                            is OnItemClickListener<*> -> (onItemClickListener as OnItemClickListener<TItem>).onItemClicked(item)
                            is OnItemWithPositionClickListener<*> -> (onItemClickListener as OnItemWithPositionClickListener<TItem>)
                                    .onItemClicked(item, positionInAdapter, positionInCollection)
                            else -> Lc.assertion("Unexpected onItemClickListener type " + onItemClickListener!!)
                        }
                    },
                    itemClickDelayMillis)
        }
    }

    /**
     * Method to bind item (from [.items]) to item-specific ViewHolder.
     * It is not calling for headers and footer which counts are returned by [.getHeadersCount] and @link #getFootersCount()}.
     * You don't need to override this method if you have delegates for every view type.
     *
     * @param holder            ViewHolder to bind item to;
     * @param positionInAdapter Position of ViewHolder (NOT item!);
     * @param item              Item returned by position (WITH HEADER OFFSET!).
     */
    protected fun onBindItemToViewHolder(holder: TItemViewHolder, positionInAdapter: Int, item: TItem) {
        // do nothing by default - let delegates do it
    }

    /**
     * Method to bind item (from [.items]) to item-specific ViewHolder with payloads.
     * It is not calling for headers and footer which counts are returned by [.getHeadersCount] and @link #getFootersCount()}.
     *
     * @param holder            ViewHolder to bind item to;
     * @param positionInAdapter Position of ViewHolder in adapter (NOT item!);
     * @param item              Item returned by position (WITH HEADER OFFSET!);
     * @param payloads          Payloads.
     */
    protected fun onBindItemToViewHolder(holder: TItemViewHolder, positionInAdapter: Int, item: TItem,
                                         payloads: List<Any>) {
        // do nothing by default - let delegates do it
    }

    public override fun getItem(positionInAdapter: Int): TItem? {
        val positionInCollection = getItemPositionInCollection(positionInAdapter)
        return if (positionInCollection >= 0) items[positionInCollection] else null
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener Item click listener.
     */
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<TItem>?) {
        this.setOnItemClickListener(onItemClickListener, UiUtils.RIPPLE_EFFECT_DELAY)
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener  Item click listener;
     * @param itemClickDelayMillis Delay of calling click listener.
     */
    fun setOnItemClickListener(onItemClickListener: OnItemClickListener<TItem>?, itemClickDelayMillis: Long) {
        this.onItemClickListener = onItemClickListener
        this.itemClickDelayMillis = itemClickDelayMillis
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener Item click listener.
     */
    fun setOnItemClickListener(onItemClickListener: OnItemWithPositionClickListener<TItem>?) {
        this.setOnItemClickListener(onItemClickListener, UiUtils.RIPPLE_EFFECT_DELAY)
    }

    /**
     * Sets item click listener.
     *
     * @param onItemClickListener  Item click listener;
     * @param itemClickDelayMillis Delay of calling click listener.
     */
    fun setOnItemClickListener(onItemClickListener: OnItemWithPositionClickListener<TItem>?, itemClickDelayMillis: Long) {
        this.onItemClickListener = onItemClickListener
        this.itemClickDelayMillis = itemClickDelayMillis
    }

    /**
     * Returns if click listening disabled or not for specific item.
     *
     * @param item                 Item to check click availability;
     * @param positionInAdapter    Position of clicked item in adapter (with headers);
     * @param positionInCollection Position of clicked item in inner collection;
     * @return True if click listener enabled for such item.
     */
    fun isOnClickListenerDisabled(item: TItem, positionInAdapter: Int, positionInCollection: Int): Boolean = false

    /**
     * Interface to simply add item click listener.
     *
     * @param <TItem> Type of item
    </TItem> */
    interface OnItemClickListener<TItem> {

        /**
         * Calls when item have clicked.
         *
         * @param item Clicked item.
         */
        fun onItemClicked(item: TItem)

    }

    /**
     * Interface to simply add item click listener based on item position in adapter and collection.
     *
     * @param <TItem> Type of item
    </TItem> */
    interface OnItemWithPositionClickListener<TItem> {

        /**
         * Calls when item have clicked.
         *
         * @param item                 Clicked item;
         * @param positionInAdapter    Position of clicked item in adapter (with headers);
         * @param positionInCollection Position of clicked item in inner collection.
         */
        fun onItemClicked(item: TItem, positionInAdapter: Int, positionInCollection: Int)

    }

    private data class LongContainer(var value: Long = 0)

}
