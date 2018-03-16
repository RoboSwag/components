package ru.touchin.roboswag.components.adapters

import android.support.v7.recyclerview.extensions.AsyncDifferConfig
import android.support.v7.recyclerview.extensions.AsyncListDiffer
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup

/**
 * Base adapter with delegation and diff computing on background thread.
 */
open class DelegationListAdapter<TItem>(diffCallback: DiffUtil.ItemCallback<TItem>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemClickListener: ((TItem, RecyclerView.ViewHolder) -> Unit)? = null

    private val delegatesManager = DelegatesManager()
    private var differ = AsyncListDiffer(OffsetAdapterUpdateCallback(this, ::getHeadersCount), AsyncDifferConfig.Builder<TItem>(diffCallback).build())

    open fun getHeadersCount(): Int = 0

    open fun getFootersCount(): Int = 0

    override fun getItemCount(): Int = getHeadersCount() + differ.currentList.size + getFootersCount()

    override fun getItemViewType(position: Int): Int = delegatesManager.getItemViewType(getList(), position, getCollectionPosition(position))

    override fun getItemId(position: Int): Long = delegatesManager.getItemId(getList(), position, getCollectionPosition(position))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = delegatesManager.onCreateViewHolder(parent, viewType)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: List<Any>) {
        val collectionPosition = getCollectionPosition(position)
        if (itemClickListener != null && collectionPosition in 0 until getList().size) {
            holder.itemView.setOnClickListener { itemClickListener?.invoke(getList()[collectionPosition], holder) }
        } else {
            holder.itemView.setOnClickListener(null)
        }
        delegatesManager.onBindViewHolder(holder, getList(), position, collectionPosition, payloads)
    }

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = Unit

    /**
     * Adds [ItemAdapterDelegate] to adapter.
     *
     * @param delegate Delegate to add.
     */
    fun addDelegate(delegate: ItemAdapterDelegate<*, *>) = delegatesManager.addDelegate(delegate)

    /**
     * Adds [PositionAdapterDelegate] to adapter.
     *
     * @param delegate Delegate to add.
     */
    fun addDelegate(delegate: PositionAdapterDelegate<*>) = delegatesManager.addDelegate(delegate)

    /**
     * Submits a new list to be diffed, and displayed.
     *
     * If a list is already being displayed, a diff will be computed on a background thread, which
     * will dispatch Adapter.notifyItem events on the main thread.
     *
     * @param list The new list to be displayed.
     */
    fun submitList(list: List<TItem>) = differ.submitList(list)

    /**
     * Get the current List - any diffing to present this list has already been computed and
     * dispatched via the ListUpdateCallback.
     * <p>
     * If a <code>null</code> List, or no List has been submitted, an empty list will be returned.
     * <p>
     * The returned list may not be mutated - mutations to content must be done through
     * {@link #submitList(List)}.
     *
     * @return current List.
     */
    fun getList(): List<TItem> = differ.currentList

    fun getCollectionPosition(adapterPosition: Int): Int = adapterPosition - getHeadersCount()

}
