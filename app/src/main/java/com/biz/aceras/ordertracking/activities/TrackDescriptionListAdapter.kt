package com.biz.aceras.ordertracking.activities

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.biz.aceras.ordertracking.R
import com.biz.aceras.ordertracking.StandardObjects
import com.biz.aceras.ordertracking.serializer_class.OrderDescriptionInfo
import com.biz.aceras.ordertracking.serializer_class.OrderHeaderInfo
import kotlinx.android.synthetic.main.recyclerview_track_description_list.view.*

/**
 * Created by eesern_ong on 10/4/2019.
 */
class TrackDescriptionListAdapter(val items : List<OrderDescriptionInfo>, val context: Context) : RecyclerView.Adapter<TrackDescriptionListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recyclerview_track_description_list, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.setOrderNo?.text = items.get(position).orderNumber
        holder.setItemNo?.text = items.get(position).itemNumber
        holder.setItemDesc?.text = items.get(position).productDesc
        holder.setQuantity?.text = items.get(position).productQty
        holder.setCurrentStatus?.text = items.get(position).status
        holder.setCurrentStatusDate?.text = StandardObjects.timeStampToDate(items.get(position).statusDate)
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val setOrderNo = view.tvOrderNo
        val setItemNo = view.tvItemNo
        val setItemDesc = view.tvitemDesc
        val setQuantity = view.tvQuantity
        val setCurrentStatus = view.tvCurrentStatus
        val setCurrentStatusDate = view.tvCurrentStatusDate
//        val setStatus = view.tvStatus
    }
}