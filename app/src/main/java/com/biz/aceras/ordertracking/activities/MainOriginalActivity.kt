package com.biz.aceras.ordertracking.activities

import android.R
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.NetworkInfo
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.telephony.TelephonyManager
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TabHost
import android.widget.Toast
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.image_slider.ItemClickListener
import com.biz.aceras.ordertracking.image_slider.SlideModel
import com.biz.aceras.ordertracking.serializer_class.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main_original.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainOriginalActivity : AppCompatActivity(), View.OnClickListener {

    private val retrofit = NetworkClient.getRetrofitClient()
    private val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)
    private var activeNetwork: NetworkInfo? = null
    private var progressDialog: ProgressDialog? = null
    val OTP_INFO = "com.biz.aceras.ordertracking.OTP"

    private val gsonOrderList = ArrayList<OrderHeaderInfo>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val lastVisibleItemPosition: Int
        get() = linearLayoutManager.findLastVisibleItemPosition()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.biz.aceras.ordertracking.R.layout.activity_main_original)

        progressDialog = StandardObjects.showProgressDialog(this@MainOriginalActivity, "Loading...")

        // Assign values to the product type drop down
        setProductTypeOption()

        // Set the Dropdown option and product description text to be disable by default as the radio
        // button selected on default is for order number
        spinnerProductType.isEnabled = false
        etProductDescription.isEnabled = false

        // Set the image view of the logo to be white
        ivSalesOrderTracking.setColorFilter(Color.parseColor("#FFFFFF"))

        // Action Bar configuration
        StandardObjects.actionBarConfig(this@MainOriginalActivity)

        var imageList = ArrayList<SlideModel>()

        imageList.add(SlideModel(com.biz.aceras.ordertracking.R.drawable.progress_animation))
        image_slider.setImageList(imageList)

        // Get Image URL from cache to display for Image Slider
        val cacheBannerManagement: CacheManagement = CacheManagement(applicationContext, getString(com.biz.aceras.ordertracking.R.string.cache_banner_swipeable))
        var bannersJsonResponse = cacheBannerManagement.readDataFromCache()
        activeNetwork = StandardObjects.checkActiveNetwork(this@MainOriginalActivity)
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
        } else {
            if (activeNetwork != null) {

                val call = accountAPIs.getBanner()

                APIHelper.enqueueWithRetry(call, 5, object : Callback<List<BannerInfo>> {
                    override fun onResponse(call: Call<List<BannerInfo>>, response: Response<List<BannerInfo>>) {

                        if (response.isSuccessful) {
                            // Convert GSON response to JSON String
                            val gson = Gson()
                            val jsonInString = gson.toJson(response.body())
                            // Store JSON Response String to Cache
                            cacheBannerManagement.writeDataToCache(jsonInString)

                            for (bannerInfo in response.body()!!) {
                                gsonBannerlist.add(bannerInfo)
                                imageList.add(SlideModel(bannerInfo.imageUrl))
                            }

                            image_slider.reset()
                            image_slider.setImageList(imageList)
                        }
                    }

                    override fun onFailure(call: Call<List<BannerInfo>>, t: Throwable) {

                    }
                })
            }
        }

        image_slider.setItemClickListener(object : ItemClickListener {
            override fun onItemSelected(position: Int) {
                val intent = Intent(applicationContext, WebViewFullscreenActivity::class.java)
                if (gsonBannerlist.isNotEmpty()) {
                    StandardObjects.webViewURL = gsonBannerlist[position].redirectUrl
                    startActivity(intent)
                }
            }
        })

        displayOustandingList()

//        radioOrderNo.setOnClickListener(this)
//        radioDescription.setOnClickListener(this)
        btnTrack.setOnClickListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(com.biz.aceras.ordertracking.R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {
            com.biz.aceras.ordertracking.R.id.menuTerms -> {
                val intent: Intent = Intent(applicationContext, WebViewFullscreenActivity::class.java)
                StandardObjects.webViewURL = "http://yahoo.com"
                startActivity(intent)
            }

            com.biz.aceras.ordertracking.R.id.menuQRScanner -> {
                val intent: Intent = Intent(applicationContext, WebViewFullscreenActivity::class.java)
                StandardObjects.webViewURL = "https://developer.android.com/guide/topics/ui/menus"
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {

//            radioOrderNo.id -> {
//                etTrackNo.isEnabled = true
//                spinnerProductType.isEnabled = false
//                etProductDescription.isEnabled = false
//            }
//
//            radioDescription.id -> {
//                etTrackNo.isEnabled = false
//                spinnerProductType.isEnabled = true
//                etProductDescription.isEnabled = true
//            }

            btnTrack.id -> {
                if (isValid()) {
                    btnTrack.isEnabled = false
                    val aleartDialog = StandardObjects.showProgressDialog(this@MainOriginalActivity, "Searching...")
                    aleartDialog.show()
                    var trackInfo: TrackInfo = TrackInfo()

                    // Retrieve from shared preference the Session Token ID to verify whether if it is still valid or not from server side
                    trackInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(com.biz.aceras.ordertracking.R.string.pref_registration_id))!!
                    trackInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(com.biz.aceras.ordertracking.R.string.pref_session_id))!!
                    trackInfo.orderNo = etTrackNo.text.toString()

                    val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
//                        trackInfo.imeiNo = tm.getImei()
                        trackInfo.imeiNo = StandardObjects.sampleImei
                    } else {
//                        trackInfo.imeiNo = StandardObjects.sampleImei
//                        trackInfo.imeiNo = tm.getDeviceId()
                    }

                    Log.d("TrackRegistrationID", trackInfo.registrationID)
                    Log.d("TrackSessionTokenID", trackInfo.sessionTokenID)
                    Log.d("TrackOrderNo", trackInfo.orderNo)
                    Log.d("TrackImeiNo", trackInfo.imeiNo)

                    // Invoke the method corresponding to the HTTP request which will return a Call object.
                    // This Call object will used to send the actual network request with the specified parameters
                    if (trackInfo.orderNo.isEmpty()) {
                        trackInfo.currentNumberOfRecord = "0"
//                        trackInfo.getNumberOfRecord = "20"
                        val call = accountAPIs.getOrderList(trackInfo)
                        // This is the line which actually sends a network request.
                        // Calling enqueue() executes a call asynchronously.
                        // It has two callback listeners which will invoked on the main thread
                        APIHelper.enqueueWithRetry(call, 5, object : Callback<List<OrderHeaderInfo>> {
                            override fun onResponse(call: Call<List<OrderHeaderInfo>>, response: Response<List<OrderHeaderInfo>>) {
                                /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of UserInfo POJO class
                        */
                                if (response.isSuccessful) {
                                    // Convert GSON response to JSON String
                                    val gson = Gson()
                                    val jsonInString = gson.toJson(response.body())
                                    // Store JSON Response String to Cache
                                    val cacheOrderListManagement: CacheManagement = CacheManagement(applicationContext, getString(com.biz.aceras.ordertracking.R.string.cache_outstanding_order_list))
                                    cacheOrderListManagement.writeDataToCache(jsonInString)
                                    aleartDialog.dismiss()
                                    btnTrack.isEnabled = true
                                    // Move to next screen showing the data of the order
                                    val intent: Intent = Intent(applicationContext, TrackListActivity::class.java)
                                    startActivity(intent)
                                } else if (response.code() == 404) {
                                    val errorMessage: JsonObject = JsonParser().parse(response.errorBody()!!.string()).getAsJsonObject()
                                    Log.d("SearchError", errorMessage.get("Message").toString())
                                    Toast.makeText(applicationContext, "No Orders Found", Toast.LENGTH_LONG).show()
                                    aleartDialog.dismiss()
                                    btnTrack.isEnabled = true
                                } else if (response.code() == 401) {
                                    val errorMessage: JsonObject = JsonParser().parse(response.errorBody()!!.string()).getAsJsonObject()
                                    Log.d("SearchError", errorMessage.get("Message").toString())
                                    Toast.makeText(applicationContext, "Response:" + errorMessage.get("Message").toString(), Toast.LENGTH_LONG).show()
                                    StandardObjects.sessoinExpiredDialog(this@MainOriginalActivity, this@MainOriginalActivity).show()
                                    aleartDialog.dismiss()
                                    btnTrack.isEnabled = true
                                }
                            }

                            override fun onFailure(call: Call<List<OrderHeaderInfo>>, t: Throwable) {
                                Toast.makeText(applicationContext, "Retry", Toast.LENGTH_LONG).show()
                                aleartDialog.dismiss()
                                btnTrack.isEnabled = true
                            }
                        })
                    } else {

                        val call = accountAPIs.getOrderDetails(trackInfo)

                        APIHelper.enqueueWithRetry(call, 5, object : Callback<OrderDetailsInfo> {
                            override fun onResponse(call: Call<OrderDetailsInfo>, response: Response<OrderDetailsInfo>) {

                                if (response.isSuccessful) {
                                    // Convert GSON response to JSON String
                                    val gson = Gson()
                                    val jsonInString = gson.toJson(response.body())
                                    // Store JSON Response String to Cache
                                    val cacheOrderDetailsManagement: CacheManagement = CacheManagement(applicationContext, getString(com.biz.aceras.ordertracking.R.string.cache_order))
                                    cacheOrderDetailsManagement.writeDataToCache(jsonInString)

                                    aleartDialog.dismiss()
                                    btnTrack.isEnabled = true

                                    // Move to next screen showing the data of the order
                                    val intent: Intent = Intent(applicationContext, TrackInfoActivity::class.java)
                                    startActivity(intent)

                                } else if (response.code() == 404) {
                                    val errorMessage: JsonObject = JsonParser().parse(response.errorBody()!!.string()).getAsJsonObject()
                                    Log.d("SearchError", errorMessage.get("Message").toString())
                                    Toast.makeText(applicationContext, "No Such Order Number Found", Toast.LENGTH_LONG).show()
                                    aleartDialog.dismiss()
                                    btnTrack.isEnabled = true
                                } else if (response.code() == 401) {
                                    //response.code()  will give you the error code. Can make use of this to navigate between screens
                                    val errorMessage: JsonObject = JsonParser().parse(response.errorBody()!!.string()).getAsJsonObject()
                                    Log.d("SearchError", errorMessage.get("Message").toString())
                                    Toast.makeText(applicationContext, "Response:" + errorMessage.get("Message").toString(), Toast.LENGTH_LONG).show()
                                    StandardObjects.sessoinExpiredDialog(this@MainOriginalActivity, this@MainOriginalActivity).show()
                                    aleartDialog.dismiss()
                                    btnTrack.isEnabled = true

                                }
                            }

                            override fun onFailure(call: Call<OrderDetailsInfo>, t: Throwable) {
                                Toast.makeText(applicationContext, "Retry", Toast.LENGTH_LONG).show()
                                aleartDialog.dismiss()
                                btnTrack.isEnabled = true
                            }
                        })
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
        val activeNetwork = StandardObjects.checkActiveNetwork(this@MainOriginalActivity)
        if (activeNetwork == null) {
            StandardObjects.connectionErrorDialog(this@MainOriginalActivity).show()
        }

        if (!StandardObjects.checkPermissionGranted(this@MainOriginalActivity).first) {
            StandardObjects.checkPermissionGranted(this@MainOriginalActivity).second!!.show()
            return false
        }

        return true
    }

    fun displayOustandingList(){
        val cacheListManagement: CacheManagement = CacheManagement(applicationContext, getString(com.biz.aceras.ordertracking.R.string.cache_outstanding_order_list))
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

            recyclerViewOustandingList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            setRecyclerViewScrollListener()

            // Click Listener when item is clicked
            recyclerViewOustandingList.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerViewOustandingList, object : RecyclerTouchListener.ClickListener {
                override fun onClick(view: View, position: Int) {
                    if (isValid()) {

                        progressDialog!!.show()
                        val orderNoSelected = gsonOrderList.get(position)

                        val trackInfo: TrackInfo = TrackInfo()

                        // Retrieve from shared preference the Session Token ID to verify whether if it is still valid or not from server side
                        trackInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(com.biz.aceras.ordertracking.R.string.pref_registration_id))!!
                        trackInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(com.biz.aceras.ordertracking.R.string.pref_session_id))!!
                        trackInfo.orderNo = orderNoSelected.orderNumber

                        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        if (Build.VERSION.SDK_INT >= 26) {
//                            trackInfo.imeiNo = tm.getImei()
                            trackInfo.imeiNo = StandardObjects.sampleImei
                        } else {
//                            trackInfo.imeiNo = tm.getDeviceId()
//                            trackInfo.imeiNo = StandardObjects.sampleImei
                        }

                        val retrofit = NetworkClient.getRetrofitClient()
                        val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)
                        val call = accountAPIs.getOrderDetails(trackInfo)
                        APIHelper.enqueueWithRetry(call, 5, object : Callback<OrderDetailsInfo> {
                            override fun onResponse(call: Call<OrderDetailsInfo>, response: Response<OrderDetailsInfo>) {

                                if (response.isSuccessful) {
                                    // Convert GSON response to JSON String
                                    val gson = Gson()
                                    val jsonInString = gson.toJson(response.body())
                                    // Store JSON Response String to Cache
                                    val cacheOrderManagement: CacheManagement = CacheManagement(applicationContext, getString(com.biz.aceras.ordertracking.R.string.cache_order))
                                    cacheOrderManagement.writeDataToCache(jsonInString)
                                    // Move to next screen showing the data of the order
                                    val intent: Intent = Intent(applicationContext, TrackInfoActivity::class.java)
                                    startActivity(intent)
                                    progressDialog!!.dismiss()
                                    //finish()
                                } else {
                                    val intent: Intent = Intent(applicationContext, ExpiredActivity::class.java)
                                    startActivity(intent)
                                    progressDialog!!.dismiss()
                                    finish()
                                    Toast.makeText(applicationContext, "Response:" + response.errorBody()!!.string(), Toast.LENGTH_LONG).show()
                                }
                            }

                            override fun onFailure(call: Call<OrderDetailsInfo>, t: Throwable) {
                                Toast.makeText(applicationContext, "Retry", Toast.LENGTH_LONG).show()
                            }
                        })
                    }
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
                    getList(totalItemCount.toString())
                }
            }
        })
    }

    private fun getList(totalItemCount: String) {
        if (isValid()) {
            val trackInfo: TrackInfo = TrackInfo()

            // Retrieve from shared preference the Session Token ID to verify whether if it is still valid or not from server side
            trackInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(com.biz.aceras.ordertracking.R.string.pref_registration_id))!!
            trackInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(com.biz.aceras.ordertracking.R.string.pref_session_id))!!
            trackInfo.currentNumberOfRecord = totalItemCount
//            trackInfo.getNumberOfRecord = "20"

            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (android.os.Build.VERSION.SDK_INT >= 26) {
//                trackInfo.imeiNo = tm.getImei()
                trackInfo.imeiNo = StandardObjects.sampleImei
            } else {
//                trackInfo.imeiNo = tm.getDeviceId()
//                trackInfo.imeiNo = StandardObjects.sampleImei
            }

            val retrofit = NetworkClient.getRetrofitClient()
            val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)
            val call = accountAPIs.getOrderList(trackInfo)
            // This is the line which actually sends a network request.
            // Calling enqueue() executes a call asynchronously.
            // It has two callback listeners which will invoked on the main thread
            APIHelper.enqueueWithRetry(call, 5, object : Callback<List<OrderHeaderInfo>> {
                override fun onResponse(call: Call<List<OrderHeaderInfo>>, response: Response<List<OrderHeaderInfo>>) {
                    /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of UserInfo POJO class
            */
                    if (response.isSuccessful) {

                        // Convert GSON response to JSON String
                        val gson = Gson()
                        val jsonInString = gson.toJson(response.body())
                        // Store JSON Response String to Cache
                        val cacheOrderListManagement: CacheManagement = CacheManagement(applicationContext, getString(com.biz.aceras.ordertracking.R.string.cache_outstanding_order_list))
                        cacheOrderListManagement.writeDataToCache(jsonInString)
                        for (orderHeaderInfo in response.body()!!) {
                            gsonOrderList.add(orderHeaderInfo)
                        }
                        recyclerViewOustandingList.adapter.notifyDataSetChanged()
//                        recyclerViewTrackList.adapter = TrackListAdapter(gsonOrderList, applicationContext)
                        // Restore state
//                        recyclerViewTrackList.getLayoutManager().onRestoreInstanceState(recyclerViewState);

                    } else if (response.code() == 404) {
                        val errorMessage: JsonObject = JsonParser().parse(response.errorBody()!!.string()).getAsJsonObject()
                        Log.d("SearchError", errorMessage.get("Message").toString())
                        Toast.makeText(applicationContext, "No Orders Found", Toast.LENGTH_LONG).show()

                    } else if (response.code() == 401) {
                        val errorMessage: JsonObject = JsonParser().parse(response.errorBody()!!.string()).getAsJsonObject()
                        Log.d("SearchError", errorMessage.get("Message").toString())
                        Toast.makeText(applicationContext, "Response:" + errorMessage.get("Message").toString(), Toast.LENGTH_LONG).show()
                        StandardObjects.sessoinExpiredDialog(this@MainOriginalActivity, this@MainOriginalActivity).show()

                    }
                }

                override fun onFailure(call: Call<List<OrderHeaderInfo>>, t: Throwable) {
                    Toast.makeText(applicationContext, "Retry", Toast.LENGTH_LONG).show()

                }
            })

        }
    }

    fun setProductTypeOption(){
        // Get Image URL from cache to display for Image Slider
        val cacheProductType: CacheManagement = CacheManagement(applicationContext, getString(com.biz.aceras.ordertracking.R.string.cache_product_type))
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

                var adapter: ArrayAdapter<String> = ArrayAdapter<String>(this, R.layout.simple_spinner_item, productTypeList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProductType.setAdapter(adapter)

            } else {
                Toast.makeText(this, "No Objects", Toast.LENGTH_LONG).show()
            }
        } else {
//            if (activeNetwork != null) {
//
//                val call = accountAPIs.getBanner()
//
//                APIHelper.enqueueWithRetry(call, 5, object : Callback<List<BannerInfo>> {
//                    override fun onResponse(call: Call<List<BannerInfo>>, response: Response<List<BannerInfo>>) {
//
//                        if (response.isSuccessful) {
//                            // Convert GSON response to JSON String
//                            val gson = Gson()
//                            val jsonInString = gson.toJson(response.body())
//                            // Store JSON Response String to Cache
//                            cacheBannerManagement.writeDataToCache(jsonInString)
//
//                            for (bannerInfo in response.body()!!) {
//                                gsonBannerlist.add(bannerInfo)
//                                imageList.add(SlideModel(bannerInfo.imageUrl))
//                            }
//
//                            image_slider.reset()
//                            image_slider.setImageList(imageList)
//                        }
//                    }
//
//                    override fun onFailure(call: Call<List<BannerInfo>>, t: Throwable) {
//
//                    }
//                })
//            }
        }

    }
}