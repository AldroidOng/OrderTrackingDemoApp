package com.biz.aceras.ordertracking.activities

import android.app.ActionBar
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.serializer_class.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_track_description_list.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackDescriptionListActivity : AppCompatActivity() {


    private val gsonOrderDescriptionList = ArrayList<OrderDescriptionInfo>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val lastVisibleItemPosition: Int
        get() = linearLayoutManager.findLastVisibleItemPosition()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_description_list)

        val getIntent: Intent = getIntent()
//        if (!getIntent.getStringExtra(MainActivity().product_type_search_input).toString().isEmpty()){
        tvProductTypeInput.text = String.format(getString(R.string.product_type_search_input), getIntent.getStringExtra(MainActivity().product_type_search_input).toString())
//            tvProductTypeInput.visibility = VISIBLE
//        }else{
//            tvProductTypeInput.visibility = INVISIBLE
//        }
//        if (!getIntent.getStringExtra(MainActivity().product_desc_search_input).toString().isEmpty()){
        tvProductDescInput.text = String.format(getString(R.string.product_desc_search_input), getIntent.getStringExtra(MainActivity().product_desc_search_input).toString())
//            tvProductDescInput.visibility = VISIBLE
//        }else{
//            tvProductDescInput.visibility = INVISIBLE
//        }

        // Action Bar configuration
        StandardObjects.actionBarConfig(this@TrackDescriptionListActivity)
        StandardObjects.setBackButton(this@TrackDescriptionListActivity)



        val cacheListManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_order_description_list))
        val orderListJsonResponse = cacheListManagement.readDataFromCache()

        if (!orderListJsonResponse.isEmpty()) {
            val orderListArray = JSONArray(orderListJsonResponse)
            if (orderListArray.length() > 0) {
                val gson = Gson()
                var i = 0

                while (i < orderListArray.length()) {

                    var orderDescriptionList: OrderDescriptionInfo = OrderDescriptionInfo()
                    orderDescriptionList = gson.fromJson<OrderDescriptionInfo>(orderListArray.getJSONObject(i).toString(), OrderDescriptionInfo::class.java)

                        var noLeadingZeroItemNo = orderDescriptionList.itemNumber.replaceFirst("0", "")
                    orderDescriptionList.intItemNumber = noLeadingZeroItemNo.toInt()

                        if (orderDescriptionList.actualDelivered.isEmpty() == false) {
                            orderDescriptionList.status = "Delivered"
                            orderDescriptionList.statusDate = orderDescriptionList.actualDelivered
                        } else if (orderDescriptionList.actualShipped.isEmpty() == false) {
                            orderDescriptionList.status = "Shipped"
                            orderDescriptionList.statusDate = orderDescriptionList.actualShipped
                        } else if (orderDescriptionList.actualProduced.isEmpty() == false) {
                            orderDescriptionList.status = "Produced"
                            orderDescriptionList.statusDate = orderDescriptionList.actualProduced
                        } else if (orderDescriptionList.actualConfirmed.isEmpty() == false) {
                            orderDescriptionList.status = "Confirmed"
                            orderDescriptionList.statusDate = orderDescriptionList.actualConfirmed
                        } else if (orderDescriptionList.actualTaken.isEmpty() == false) {
                            orderDescriptionList.status = "Taken"
                            orderDescriptionList.statusDate = orderDescriptionList.actualTaken
                        }

                        gsonOrderDescriptionList.add(orderDescriptionList)

//                    gsonOrderDescriptionList.add(gson.fromJson<OrderDescriptionInfo>(orderListArray.getJSONObject(i).toString(), OrderDescriptionInfo::class.java))
                    i++
                }
            }

            linearLayoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

            // Creates a vertical Layout Manager
            recyclerViewTrackDescriptionList.layoutManager = linearLayoutManager

            // Access the RecyclerView Adapter and load the data into it
            recyclerViewTrackDescriptionList.adapter = TrackDescriptionListAdapter(gsonOrderDescriptionList, this)

            recyclerViewTrackDescriptionList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

            // Click Listener when item is clicked
            recyclerViewTrackDescriptionList.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerViewTrackDescriptionList, object : RecyclerTouchListener.ClickListener {
                override fun onClick(view: View, position: Int) {

                    val orderItemSelected = gsonOrderDescriptionList.get(position)
                    val orderItemInfoSelected: OrderItemInfo = OrderItemInfo()
                    orderItemInfoSelected.itemNumber = orderItemSelected.itemNumber
                    orderItemInfoSelected.productDesc = orderItemSelected.productDesc
                    orderItemInfoSelected.productQty = orderItemSelected.productQty
                    orderItemInfoSelected.productUOM = "MT"
                    orderItemInfoSelected.finalDestination = orderItemSelected.finalDestination
                    orderItemInfoSelected.targetConfirmed = orderItemSelected.targetConfirmed
                    orderItemInfoSelected.targetProduced = orderItemSelected.targetProduced
                    orderItemInfoSelected.targetShipped = orderItemSelected.targetShipped
                    orderItemInfoSelected.targetDelivered = orderItemSelected.targetDelivered
                    orderItemInfoSelected.actualTaken = orderItemSelected.actualTaken
                    orderItemInfoSelected.actualConfirmed = orderItemSelected.actualConfirmed
                    orderItemInfoSelected.actualProduced = orderItemSelected.actualProduced
                    orderItemInfoSelected.actualShipped = orderItemSelected.actualShipped
                    orderItemInfoSelected.actualDelivered = orderItemSelected.actualDelivered
                    orderItemInfoSelected.intItemNumber = orderItemSelected.intItemNumber
                    orderItemInfoSelected.status = orderItemSelected.status
                    orderItemInfoSelected.statusDate = orderItemSelected.statusDate

                    val intent = Intent(applicationContext, TrackItemInfoActivity::class.java)
                    intent.putExtra("ORDER_ITEMS", orderItemInfoSelected)
                    startActivity(intent)

                }

                override fun onLongClick(view: View?, position: Int) {

                }
            }))
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (isValid()) {

            when (item!!.getItemId()) {

                getString(R.string.back_button_id).toInt() -> {
                    finish()
                }

                R.id.menuTerms -> {
                    val intent: Intent = Intent(applicationContext, WebViewFullscreenActivity::class.java)
                    StandardObjects.webViewURL = "http://yahoo.com"
                    startActivity(intent)
                }

                R.id.menuQRScanner -> {
                    val intent: Intent = Intent(applicationContext, WebViewFullscreenActivity::class.java)
                    StandardObjects.webViewURL = "https://developer.android.com/guide/topics/ui/menus"
                    startActivity(intent)
                }
            }
        }
        return super.onOptionsItemSelected(item)

    }

    // need include check permission
    fun isValid(): Boolean {

        return true
    }
}