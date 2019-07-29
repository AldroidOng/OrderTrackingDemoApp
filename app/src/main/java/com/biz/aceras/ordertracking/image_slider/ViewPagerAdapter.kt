package com.biz.aceras.ordertracking.image_slider

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.biz.aceras.ordertracking.R
import com.squareup.picasso.Picasso

/**
 * Created by eesern_ong on 23/4/2019.
 */
class ViewPagerAdapter (context: Context?, imageList: List<SlideModel>, private var radius: Int, private var errorImage: Int) : PagerAdapter() {

    private var imageList: List<SlideModel>? = imageList
    private var layoutInflater: LayoutInflater? = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater?

    private var itemClickListener: ItemClickListener? = null

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view == obj
    }

    override fun getCount(): Int {
        return imageList!!.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): View {

        val itemView = layoutInflater!!.inflate(R.layout.pager_row, container, false)

        val imageView = itemView.findViewById<ImageView>(R.id.image_view)
        val linearLayout = itemView.findViewById<LinearLayout>(R.id.linear_layout)
        val textView = itemView.findViewById<TextView>(R.id.text_view)

        if (imageList!![position].title != null){
            textView.text = imageList!![position].title
        }else{
            linearLayout.visibility = View.INVISIBLE
        }

        if(imageList!![position].imageUrl == null){
            Picasso.get()
                    .load(imageList!![position].imagePath!!) // Int
                    .fit()
                    .centerCrop()
                    .transform(RoundedTransformation(radius,0))
                    .error(errorImage)
                    .placeholder(R.drawable.progress_animation)
                    .into(imageView)
        }else{
            Picasso.get()
                    .load(imageList!![position].imageUrl!!) // String
                    .fit()
                    .centerCrop()
                    .transform(RoundedTransformation(radius,0))
                    .error(errorImage)
                    .placeholder(R.drawable.progress_animation)
                    .into(imageView)
        }

        container.addView(itemView)

        imageView.setOnClickListener{itemClickListener?.onItemSelected(position)}

        return itemView
    }

    fun setItemClickListener(itemClickListener: ItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as RelativeLayout)
    }


}