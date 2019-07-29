package com.biz.aceras.ordertracking.activities

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TabHost
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.serializer_class.OrderDetailsInfo
import com.biz.aceras.ordertracking.serializer_class.OrderItemInfo
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_track_info.*

class TrackInfoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_info)

        // Action Bar configuration
        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()!!.setCustomView(R.layout.actionbar_replacement);
//        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFFFFF")));
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)
        StandardObjects.setBackButton(this@TrackInfoActivity)
        val cacheManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_order))
        val orderJsonResponse = cacheManagement.readDataFromCache()
        val orderGsonResponse = Gson().fromJson(orderJsonResponse, OrderDetailsInfo::class.java)

        val orderItems: List<OrderItemInfo> = orderGsonResponse.salesOrderItem
        var openItems: MutableList<OrderItemInfo> = arrayListOf()
        var inProcessItems: MutableList<OrderItemInfo> = arrayListOf()
        var shippedItems: MutableList<OrderItemInfo> = arrayListOf()

        for (workarea in orderItems) {

            var noLeadingZeroItemNo = workarea.itemNumber.replaceFirst("0", "")
            workarea.intItemNumber = noLeadingZeroItemNo.toInt()

            if (workarea.actualDelivered.isNullOrEmpty() == false) {
                workarea.status = "Delivered"
                workarea.overallStatus = "Shipped"
                workarea.statusDate = workarea.actualDelivered
                shippedItems.add(workarea)

            } else if (workarea.actualShipped.isNullOrEmpty() == false) {
                workarea.status = "Shipped"
                workarea.overallStatus = "Shipped"
                workarea.statusDate = workarea.actualShipped
                shippedItems.add(workarea)

            } else if (workarea.actualProduced.isNullOrEmpty() == false) {
                workarea.status = "Produced"
                workarea.overallStatus = "In Process"
                workarea.statusDate = workarea.actualProduced
                inProcessItems.add(workarea)

            } else if (workarea.actualConfirmed.isNullOrEmpty() == false) {
                workarea.status = "Confirmed"
                workarea.overallStatus = "In Process"
                workarea.statusDate = workarea.actualConfirmed
                inProcessItems.add(workarea)

            } else if (workarea.actualTaken.isNullOrEmpty() == false) {
                workarea.status = "Taken"
                workarea.overallStatus = "Open"
                workarea.statusDate = workarea.actualTaken
                openItems.add(workarea)
            }
        }

        /*Start (Bind header data)*/
        tvCustomerName.text = orderGsonResponse.salesOrderHeader.customerName
        tvConsignee.text = orderGsonResponse.salesOrderHeader.consigneeName
        tvPONumber.text = orderGsonResponse.salesOrderHeader.pONumber
        tvIncoterms.text = orderGsonResponse.salesOrderHeader.incoterms
        tvProductionOrder.text = orderGsonResponse.salesOrderHeader.productionOrder
        tvSONumber.text = orderGsonResponse.salesOrderHeader.orderNumber
        /*End (Bind header data)*/

        /*Start (Sorting the list by item number ascending)*/
        fun selector(p: OrderItemInfo): Int = p.intItemNumber
        val sortedOrderItems = orderItems.sortedBy { selector(it) }
        val sortedOpenItems = openItems.sortedBy { selector(it) }
        val sortedInProcessItems = inProcessItems.sortedBy { selector(it) }
        val sortedShippedItems = shippedItems.sortedBy { selector(it) }
        /*End (Sorting the list by item number ascending)*/

        //Setting up the tab and it's respective route to the views for each tab selected)
        inflateTabSlider()

        // Creates a vertical Layout Manager
        recyclerViewAllItems.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        recyclerViewOpenItems.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        recyclerViewInProcessItems.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)
        recyclerViewShippedItems.layoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

        // Access the RecyclerView Adapter and load the data into it
        recyclerViewAllItems.adapter = TrackItemAdapter(sortedOrderItems, this)
        recyclerViewOpenItems.adapter = TrackItemAdapter(sortedOpenItems, this)
        recyclerViewInProcessItems.adapter = TrackItemAdapter(sortedInProcessItems, this)
        recyclerViewShippedItems.adapter = TrackItemAdapter(sortedShippedItems, this)

        // Adding divider using decorator
        recyclerViewAllItems.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerViewOpenItems.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerViewInProcessItems.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        recyclerViewShippedItems.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // Click Listener when item is clicked
        recyclerViewAllItems.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerViewAllItems, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {
                val orderItemSelected = sortedOrderItems.get(position)

                val intent = Intent(applicationContext, TrackItemInfoActivity::class.java)
                intent.putExtra("ORDER_ITEMS", orderItemSelected)
                startActivity(intent)
            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))

        recyclerViewOpenItems.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerViewOpenItems, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {
                val orderItemSelected = sortedOpenItems.get(position)

                val intent = Intent(applicationContext, TrackItemInfoActivity::class.java)
                intent.putExtra("ORDER_ITEMS", orderItemSelected)
                startActivity(intent)
            }

            override fun onLongClick(view: View?, position: Int) {

            }

        }))

        recyclerViewInProcessItems.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerViewInProcessItems, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {
                val orderItemSelected = sortedInProcessItems.get(position)

                val intent = Intent(applicationContext, TrackItemInfoActivity::class.java)
                intent.putExtra("ORDER_ITEMS", orderItemSelected)
                startActivity(intent)
            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))

        recyclerViewShippedItems.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerViewShippedItems, object : RecyclerTouchListener.ClickListener {
            override fun onClick(view: View, position: Int) {
                val orderItemSelected = sortedShippedItems.get(position)

                val intent = Intent(applicationContext, TrackItemInfoActivity::class.java)
                intent.putExtra("ORDER_ITEMS", orderItemSelected)
                startActivity(intent)
            }

            override fun onLongClick(view: View?, position: Int) {

            }
        }))
    }

    fun inflateTabSlider(){ //Setting up the tab and it's respective route to the views for each tab selected)
        tabHost.setup()
        //Add the first Tab
        var mSpec: TabHost.TabSpec = tabHost.newTabSpec("All Tab")
        mSpec.setContent(R.id.tabAll)
        mSpec.setIndicator("All")
        tabHost.addTab(mSpec)
        //Add the second Tab
        mSpec = tabHost.newTabSpec("Open Tab")
        mSpec.setContent(R.id.tabOpen)
        mSpec.setIndicator("Open")
        tabHost.addTab(mSpec)
        //Add the third Tab
        mSpec = tabHost.newTabSpec("In Process Tab")
        mSpec.setContent(R.id.tabInProcess)
        mSpec.setIndicator("In Process")
        tabHost.addTab(mSpec)
        //Add the Forth Tab
        mSpec = tabHost.newTabSpec("Shipped Tab")
        mSpec.setContent(R.id.tabShipped)
        mSpec.setIndicator("Shipped")
        tabHost.addTab(mSpec)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
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

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()

        StandardObjects.removeCache(applicationContext)
    }
}