package com.biz.aceras.ordertracking.activities

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.biz.aceras.ordertracking.R
import kotlinx.android.synthetic.main.recyclerview_track_item.view.*
import com.biz.aceras.ordertracking.serializer_class.OrderItemInfo


/**
 * Created by eesern_ong on 10/4/2019.
 */
class TrackItemAdapter(val items : List<OrderItemInfo>, val context: Context) : RecyclerView.Adapter<TrackItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recyclerview_track_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.setItemNo?.text = items.get(position).intItemNumber.toString()
        holder.setItemDesc?.text = items.get(position).productDesc
        holder.setProdQty?.text = items.get(position).productQty + "MT"
        holder.setStatus?.text = items.get(position).status + ", " + items.get(position).statusDate
    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {

        val setItemNo = view.tvItemNo
        val setItemDesc = view.tvitemDesc
        val setProdQty = view.tvProdQty
        val setStatus = view.tvStatus

    }
}