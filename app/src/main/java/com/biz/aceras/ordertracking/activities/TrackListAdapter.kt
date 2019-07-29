package com.biz.aceras.ordertracking.activities

import android.app.Activity
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.biz.aceras.ordertracking.R
import com.biz.aceras.ordertracking.StandardObjects
import com.biz.aceras.ordertracking.serializer_class.OrderHeaderInfo
import com.biz.aceras.ordertracking.serializer_class.OrderItemInfo
import kotlinx.android.synthetic.main.recyclerview_track_list.view.*

/**
 * Created by eesern_ong on 10/4/2019.
 */
class TrackListAdapter
//, recyclerView: RecyclerView
(val items : List<OrderHeaderInfo>, val context: Context) : RecyclerView.Adapter<TrackListAdapter.ViewHolder>() {


    private val activity: Activity
    private var listener: OnItemClickListener? = null

    // for load more
    private val VIEW_TYPE_ITEM = 0
    private val VIEW_TYPE_LOADING = 1
//    private var onLoadMoreListener: OnLoadMoreListener? = null

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    private var isLoading: Boolean = false
    private val visibleThreshold = 5
    private var lastVisibleItem: Int = 0
    private var totalItemCount: Int = 0

    init {
        activity = context as Activity

//        // load more
//        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) {
//                super.onScrolled(recyclerView, dx, dy)
//                totalItemCount = linearLayoutManager.itemCount
//                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
//                if (!isLoading && totalItemCount <= lastVisibleItem + visibleThreshold) {
//                    if (onLoadMoreListener != null) {
//                        onLoadMoreListener!!.onLoadMore()
//                    }
//                    isLoading = true
//                }
//            }
//        })
    }

    interface OnItemClickListener {
        fun onItemClick(item: HashMap<String, String>)
    }
//
//    interface OnLoadMoreListener {
//        fun onLoadMore()
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recyclerview_track_list, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setOrderNumber?.text = items.get(position).orderNumber
        holder.setOrderTaken?.text = StandardObjects.timeStampToDate(items.get(position).earliestOrderTaken)
        holder.setQuantity?.text = items.get(position).totalQuantity + " MT"
        holder.setETA?.text = StandardObjects.timeStampToDate(items.get(position).estimatedDateOfArrival)


    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val setOrderNumber = view.tvOrderNumber
        val setOrderTaken = view.tvOrderTaken
        val setQuantity = view.tvQuantity
        val setETA = view.tvETA
    }
}