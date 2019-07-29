package com.biz.aceras.ordertracking.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.serializer_class.BannerInfo
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    private val retrofit = NetworkClient.getRetrofitClient()
    private val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Save swipeable banner to cache
        val cacheManagementSwipeBanner: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_banner_swipeable))
        cacheManagementSwipeBanner.writeDataToCache(getString(R.string.cache_banner_swipeable_data))

        // Save list banner to cache
        val cacheManagementListBanner: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_banner_list))
        cacheManagementListBanner.writeDataToCache(getString(R.string.cache_banner_list_data))

        Thread.sleep(5000)

        val registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id))
            if (registrationID.isNullOrEmpty()) {
                val intent: Intent = Intent(applicationContext, RegisterActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                val intent: Intent = Intent(applicationContext, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
            }

//
//        val intent: Intent = Intent(applicationContext, RegisterActivity::class.java)
//        startActivity(intent)
//        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        StandardObjects.removeCache(applicationContext)
    }

    // Get images for Image Slider
    fun getBannerSwipeable(){
        val call = accountAPIs.getBanner()

        APIHelper.enqueueWithRetry(call, 5, object : Callback<List<BannerInfo>> {
            override fun onResponse(call: Call<List<BannerInfo>>, response: Response<List<BannerInfo>>) {

                if (response.isSuccessful) {
                    // Convert GSON response to JSON String
                    val gson = Gson()
                    val jsonInString = gson.toJson(response.body())
                    val cacheManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_banner_swipeable))
                    cacheManagement.writeDataToCache(jsonInString)
                }
            }

            override fun onFailure(call: Call<List<BannerInfo>>, t: Throwable) {

            }
        })
    }

    // Get images for Image List
    fun getBannerList(){
        val call = accountAPIs.getBanner()

        APIHelper.enqueueWithRetry(call, 5, object : Callback<List<BannerInfo>> {
            override fun onResponse(call: Call<List<BannerInfo>>, response: Response<List<BannerInfo>>) {

                if (response.isSuccessful) {
                    // Convert GSON response to JSON String
                    val gson = Gson()
                    val jsonInString = gson.toJson(response.body())
                    val cacheManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_banner_list))
                    cacheManagement.writeDataToCache(jsonInString)
                }
            }

            override fun onFailure(call: Call<List<BannerInfo>>, t: Throwable) {

            }
        })
    }
}