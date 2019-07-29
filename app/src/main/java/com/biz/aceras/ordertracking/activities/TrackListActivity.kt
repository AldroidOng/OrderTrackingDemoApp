package com.biz.aceras.ordertracking.activities

import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
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
import android.widget.LinearLayout
import android.widget.Toast
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.serializer_class.OrderDetailsInfo
import com.biz.aceras.ordertracking.serializer_class.OrderHeaderInfo
import com.biz.aceras.ordertracking.serializer_class.TrackInfo
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_track_list.*
import org.json.JSONArray
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackListActivity : AppCompatActivity() {

    private var activeNetwork: NetworkInfo? = null
    private val gsonOrderList = ArrayList<OrderHeaderInfo>()
    private lateinit var linearLayoutManager: LinearLayoutManager
    private val lastVisibleItemPosition: Int
        get() = linearLayoutManager.findLastVisibleItemPosition()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_list)

        // Action Bar configuration
        StandardObjects.actionBarConfig(this@TrackListActivity)
        StandardObjects.setBackButton(this@TrackListActivity)

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
            recyclerViewTrackList.layoutManager = linearLayoutManager

            // Access the RecyclerView Adapter and load the data into it
            recyclerViewTrackList.adapter = TrackListAdapter(gsonOrderList, this)

            recyclerViewTrackList.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
            setRecyclerViewScrollListener()

            // Click Listener when item is clicked
            recyclerViewTrackList.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerViewTrackList, object : RecyclerTouchListener.ClickListener {
                override fun onClick(view: View, position: Int) {
                    if (isValid()) {
                        val orderNoSelected = gsonOrderList.get(position)

                        val trackInfo: TrackInfo = TrackInfo()

                        // Retrieve from shared preference the Session Token ID to verify whether if it is still valid or not from server side
                        trackInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id))!!
                        trackInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_session_id))!!
                        trackInfo.orderNo = orderNoSelected.orderNumber

                        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                        if (android.os.Build.VERSION.SDK_INT >= 26) {
                            trackInfo.imeiNo = tm.getImei()
                        } else {
                            trackInfo.imeiNo = tm.getDeviceId()
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
                                    val cacheOrderManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_order))
                                    cacheOrderManagement.writeDataToCache(jsonInString)
                                    // Move to next screen showing the data of the order
                                    val intent: Intent = Intent(applicationContext, TrackInfoActivity::class.java)
                                    startActivity(intent)
                                    //finish()
                                } else {
                                    val intent: Intent = Intent(applicationContext, ExpiredActivity::class.java)
                                    startActivity(intent)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        if (isValid()) {
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
        }
        return super.onOptionsItemSelected(item)

    }

    // need include check permission
    fun isValid(): Boolean {
        activeNetwork = StandardObjects.checkActiveNetwork(this@TrackListActivity)
        if (activeNetwork == null) {
            StandardObjects.connectionErrorDialog(this@TrackListActivity).show()
            return false
        }
        return true
    }

    private fun setRecyclerViewScrollListener() {
        recyclerViewTrackList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                val totalItemCount = recyclerView.layoutManager!!.itemCount
                if (totalItemCount == lastVisibleItemPosition + 1) {
//                    recyclerViewState = recyclerViewTrackList.getLayoutManager().onSaveInstanceState()
                    getList(totalItemCount.toString())
                }
            }
        })
    }

    private fun getList(totalItemCount: String) {
        if (isValid()) {
            val trackInfo: TrackInfo = TrackInfo()

            // Retrieve from shared preference the Session Token ID to verify whether if it is still valid or not from server side
            trackInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id))!!
            trackInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_session_id))!!
            trackInfo.currentNumberOfRecord = totalItemCount
//            trackInfo.getNumberOfRecord = "20"

            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                trackInfo.imeiNo = tm.getImei()
            } else {
                trackInfo.imeiNo = tm.getDeviceId()
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
                        val cacheOrderListManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_outstanding_order_list))
                        cacheOrderListManagement.writeDataToCache(jsonInString)
                        for (orderHeaderInfo in response.body()!!) {
                            gsonOrderList.add(orderHeaderInfo)
                        }
                        recyclerViewTrackList.adapter.notifyDataSetChanged()
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
                        StandardObjects.sessoinExpiredDialog(this@TrackListActivity, this@TrackListActivity).show()

                    }
                }

                override fun onFailure(call: Call<List<OrderHeaderInfo>>, t: Throwable) {
                    Toast.makeText(applicationContext, "Retry", Toast.LENGTH_LONG).show()

                }
            })

        }
    }

}