package com.biz.aceras.ordertracking.activities

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.biz.aceras.ordertracking.R
import com.biz.aceras.ordertracking.serializer_class.BannerInfo
import com.biz.aceras.ordertracking.serializer_class.OrderHeaderInfo
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recyclerview_banner_list.view.*

/**
 * Created by eesern_ong on 10/4/2019.
 */
class BannerListAdapter(val bannerList : List<BannerInfo>, val context: Context) : RecyclerView.Adapter<BannerListAdapter.ViewHolder>() {

    private val errorImage = context.theme.obtainStyledAttributes(null, R.styleable.ImageSlider, 0, 0).getResourceId(R.styleable.ImageSlider_error_image, R.drawable.image_not_found)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.recyclerview_banner_list, parent, false))
    }

    override fun getItemCount(): Int {
        return bannerList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

//        var uri: Uri = Uri.parse(bannerList.imageUrl)
//        context = holder
            Picasso.get()
                    .load(bannerList!![position].imageUrl!!) // Int
                    .fit()
//                    .centerCrop()
                    .error(errorImage)
                    .placeholder(R.drawable.progress_animation)
                    .into(holder.imageView)


    }

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val imageView = view.ivBannerList
//        val setStatus = view.tvStatus
    }
}