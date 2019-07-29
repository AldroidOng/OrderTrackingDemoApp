package com.biz.aceras.ordertracking.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.biz.aceras.ordertracking.image_slider.ItemClickListener
import com.biz.aceras.ordertracking.image_slider.SlideModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import android.net.NetworkInfo
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TabHost
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.serializer_class.*
import com.google.gson.JsonObject
import com.google.gson.JsonParser


class MainActivity : AppCompatActivity(), View.OnClickListener {

    // Put extra intent
    val product_type_search_input = "com.biz.aceras.ordertracking.PRODUCT_TYPE_SEARCH"
    val product_desc_search_input = "com.biz.aceras.ordertracking.PRODUCT_DESC_SEARCH"

    private var tm: TelephonyManager? = null
    private val retrofit = NetworkClient.getRetrofitClient()
    private val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)
    private var activeNetwork: NetworkInfo? = null
    val OTP_INFO = "com.biz.aceras.ordertracking.OTP"

    private val gsonOrderList = ArrayList<OrderHeaderInfo>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val lastVisibleItemPosition: Int
        get() = linearLayoutManager.findLastVisibleItemPosition()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        inflateSearchTab()
        tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // Assign values to the product type drop down
        setProductTypeOption()

        // Set the Dropdown option and product description text to be disable by default as the radio
        // button selected on default is for order number
        spinnerProductType.isEnabled = false
        etProductDescription.isEnabled = false

//        // Set the image view of the logo to be white
//        ivSalesOrderTracking.setColorFilter(Color.parseColor("#FFFFFF"))

        // Action Bar configuration
        StandardObjects.actionBarConfig(this@MainActivity)

        var imageList = ArrayList<SlideModel>()

        imageList.add(SlideModel(R.drawable.progress_animation))
        image_slider.setImageList(imageList)

        // Get Image URL from cache to display for Image Slider
        val cacheBannerManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_banner_swipeable))
        var bannersJsonResponse = cacheBannerManagement.readDataFromCache()
        activeNetwork = StandardObjects.checkActiveNetwork(this@MainActivity)
        val gsonBannerlist = ArrayList<BannerInfo>()
        imageList = ArrayList<SlideModel>()

        if (!bannersJsonResponse.isEmpty()) {
            val bannerArray = JSONArray(bannersJsonResponse)
            if (bannerArray.length() > 0) {
                val gson = Gson()
                var i = 0

                while (i < bannerArray.length()) {
                    gsonBannerlist.add(gson.fromJson<BannerInfo>(bannerArray.getJSONObject(i).toString(), BannerInfo::class.java))
                    i++
                }
                for (bannerUrl in gsonBannerlist) {
                    imageList.add(SlideModel(bannerUrl.imageUrl))
                }
                image_slider.reset()
                image_slider.setImageList(imageList)

            } else {
                Toast.makeText(this, "No Objects", Toast.LENGTH_LONG).show()
            }
        }

        image_slider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                val intent: Intent = Intent(applicationContext, WebViewFullscreenActivity::class.java)
                if (gsonBannerlist.isNotEmpty()) {
                    StandardObjects.webViewURL = gsonBannerlist[position].redirectUrl
                    startActivity(intent)
                }
            }
        })

        displayOustandingList()

        radioOrderNo.setOnClickListener(this)
        radioDescription.setOnClickListener(this)
        btnTrack.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {
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

    override fun onClick(view: View?) {
        when (view!!.id) {

            radioOrderNo.id -> {
                etTrackNo.isEnabled = true
                spinnerProductType.isEnabled = false
                etProductDescription.isEnabled = false
            }

            radioDescription.id -> {
                etTrackNo.isEnabled = false
                spinnerProductType.isEnabled = true
                etProductDescription.isEnabled = true
            }

            btnTrack.id -> {
                if (isValid()) {

                    if (rgSearch.checkedRadioButtonId == radioOrderNo.id) {
                        val aleartDialog = StandardObjects.showProgressDialog(this@MainActivity, "Searching...")
                        aleartDialog.show()
                        getOrderDetails(etTrackNo.text.toString(), aleartDialog)
                    } else if (rgSearch.checkedRadioButtonId == radioDescription.id) {
                        val loadingProgressDialog: ProgressDialog = StandardObjects.showProgressDialog(this@MainActivity, "Loading...")
                        searchOrderDesc(spinnerProductType.selectedItem.toString(), etProductDescription.text.toString(), loadingProgressDialog)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        StandardObjects.removeCache(applicationContext)
    }

    fun isValid(): Boolean {

        if (!StandardObjects.checkPermissionGranted(this@MainActivity).first) {
            StandardObjects.checkPermissionGranted(this@MainActivity).second!!.show()
            return false
        }

        return true
    }

    fun displayOustandingList() {
        val cacheListManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_outstanding_order_list))
        val orderListJsonResponse = cacheListManagement.readDataFromCache()

        if (!orderListJsonResponse.isEmpty()) {
            val orderListArray = JSONArray(orderListJsonResponse)
            if (orderListArray.length() > 0) {
                val gson = Gson()
                var i = 0

                while (i < orderListArray.length()) {
                    gsonOrderList.add(gson.fromJson<OrderHeaderInfo>(orderListArray.getJSONObject(i).toString(), OrderHeaderInfo::class.java!!))
                    i++
                }
            }

            linearLayoutManager = LinearLayoutManager(this, LinearLayout.VERTICAL, false)

            // Creates a vertical Layout Manager
            recyclerViewOustandingList.layoutManager = linearLayoutManager

            // Access the RecyclerView Adapter and load the data into it
            recyclerViewOustandingList.adapter = TrackListAdapter(gsonOrderList, this)

//            recyclerViewOustandingList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            setRecyclerViewScrollListener()

            // Click Listener when item is clicked
            recyclerViewOustandingList.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerViewOustandingList, object : RecyclerTouchListener.ClickListener {
                override fun onClick(view: View, position: Int) {
                    val loadingProgressDialog: ProgressDialog = StandardObjects.showProgressDialog(this@MainActivity, "Loading...")
                    loadingProgressDialog.show()
                    if (isValid()) {
                        getOrderDetails(etTrackNo.text.toString(), loadingProgressDialog)
                    }
                    loadingProgressDialog.dismiss()
                }

                override fun onLongClick(view: View?, position: Int) {

                }
            }))
        }
    }

    private fun setRecyclerViewScrollListener() {
        recyclerViewOustandingList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                if (totalItemCount == lastVisibleItemPosition + 1) {

                }
            }
        })
    }

    fun setProductTypeOption() {
        // Get Image URL from cache to display for Image Slider
        val cacheProductType: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_product_type))
        var productTypeJsonResponse = cacheProductType.readDataFromCache()
        val gsonProductType = ArrayList<ProductTypeInfo>()
        var productTypeList: MutableList<String> = mutableListOf()

        if (!productTypeJsonResponse.isEmpty()) {
            val productTypeArray = JSONArray(productTypeJsonResponse)
            if (productTypeArray.length() > 0) {
                val gson = Gson()
                var i = 0

                while (i < productTypeArray.length()) {
                    gsonProductType.add(gson.fromJson<ProductTypeInfo>(productTypeArray.getJSONObject(i).toString(), ProductTypeInfo::class.java))
                    i++
                }
                for (productType in gsonProductType) {
                    productTypeList.add(productType.productType)
                }

                var adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, productTypeList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProductType.setAdapter(adapter)

            } else {
                Toast.makeText(this, "No Objects", Toast.LENGTH_LONG).show()
            }
        }
    }

//    fun inflateSearchTab(){ //Setting up the tab and it's respective route to the views for each tab selected)
//        tabHostSearch.setup()
//        //Add the first Tab
//        var mSpec: TabHost.TabSpec = tabHostSearch.newTabSpec("Sales Order No.")
//        mSpec.setContent(R.id.tabSearchSO)
//        mSpec.setIndicator("Sales Order No.")
//        tabHostSearch.addTab(mSpec)
//        //Add the second Tab
//        mSpec = tabHostSearch.newTabSpec("Product Description")
//        mSpec.setContent(R.id.tabSearchProduct)
//        mSpec.setIndicator("Product Description")
//        tabHostSearch.addTab(mSpec)
//    }

    fun getOrderDetails(orderNumber: String, progressDialog: ProgressDialog) {

        val jsonInString = getString(R.string.cache_order_data)
        // Store JSON Response String to Cache
        val cacheOrderManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_order))
        cacheOrderManagement.writeDataToCache(jsonInString)
        // Move to next screen showing the data of the order
        val intent: Intent = Intent(applicationContext, TrackInfoActivity::class.java)
        startActivity(intent)
        //finish()

        progressDialog.dismiss()
        btnTrack.isEnabled = true

    }

    fun searchOrderDesc(productType: String, productDesc: String, progressDialog: ProgressDialog) {

        progressDialog.show()

        // Convert GSON response to JSON String
        val gson = Gson()
        val jsonInString = getString(R.string.cache_order_description_list_data)
        // Store JSON Response String to Cache
        val cacheOrderManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_order_description_list))
        cacheOrderManagement.writeDataToCache(jsonInString)
        // Move to next screen showing the data of the order
        val intent: Intent = Intent(applicationContext, TrackDescriptionListActivity::class.java)
        intent.putExtra(product_type_search_input, productType)
        intent.putExtra(product_desc_search_input, productDesc)
        startActivity(intent)
        //finish()
        progressDialog.dismiss()
    }
}
