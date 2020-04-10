package com.biz.aceras.ordertracking.activities

import android.util.Base64
import android.content.Context
import android.content.Intent
import android.net.NetworkInfo
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.util.Log
import com.biz.aceras.ordertracking.*
import com.daimajia.numberprogressbar.OnProgressBarListener
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_welcome.*
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.support.design.widget.BaseTransientBottomBar
import android.support.v4.graphics.drawable.RoundedBitmapDrawable
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import com.biz.aceras.ordertracking.serializer_class.*
import com.github.ybq.android.spinkit.style.ChasingDots
import com.github.ybq.android.spinkit.style.DoubleBounce
import com.github.ybq.android.spinkit.sprite.Sprite
import com.google.gson.Gson
import com.squareup.okhttp.OkHttpClient
import com.squareup.okhttp.Protocol
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import org.json.JSONArray
import java.io.ByteArrayInputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*

class WelcomeActivity : AppCompatActivity(), OnProgressBarListener, View.OnClickListener {

    private val retrofit = NetworkClient.getRetrofitClient()
    private val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)
    private var tm: TelephonyManager? = null
    private var otpString = ""
    private var isTokenValid: Boolean? = null
    val OTP_INFO = "com.biz.aceras.ordertracking.OTP"

    override fun onProgressChange(current: Int, max: Int) {
        if (current == max) {
            llLoader.visibility = INVISIBLE
            pbWelcome.visibility = INVISIBLE
            btnContinue.visibility = VISIBLE
//            recyclerViewBannerList.visibility = VISIBLE
            layoutConnectWithUs.visibility = VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        pbWelcome.setOnProgressBarListener(this)

        roundCornerImages(R.drawable.facebook,10.0f, ivFacebook)
        roundCornerImages(R.drawable.instagram,10.0f, ivInstagram)
        roundCornerImages(R.drawable.youtube,10.0f, ivYouTube)
        var imageBinrary: String = ""

        val call = accountAPIs.getImageBinary()

        APIHelper.enqueueWithRetry(call, 5, object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {

                if (response.isSuccessful) {
                    imageBinrary = response.body().toString()
                    val decodedString = Base64.decode(imageBinrary, Base64.DEFAULT)
                    val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
//                    ivBinary.setImageBitmap(decodedByte)
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {

            }
        })







//        val stream = ByteArrayInputStream(data)
//        val image = BitmapFactory.decodeStream(stream)

        /*
        Progress Bar monitor for:
            1. (10%) on entering of screen
            2. (20%) check for permissions and connections
            3. (20%) checking session token validity (If Not Valid do Point 5), if valid (percentage will be 50%)
            4. (20%) Perform recyclerView for banner list
            5. (30%) getting the OTP to be stored in cache and used in verify_activity
        */

        pbWelcome.incrementProgressBy(10)
        Log.d("WelcomeLoad", "Enter Screen Load Done")
        if (isValid()) {
//            runOnUiThread {
//                android.os.Handler().postDelayed(
//                        {

            checkSessionToken()
            loadBanner()

//                    }, 5000)
//            }
        }

        btnContinue.setOnClickListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()

        StandardObjects.removeCache(applicationContext)
    }

    fun isValid(): Boolean {
//        val activeNetwork = StandardObjects.checkActiveNetwork(this@WelcomeActivity)
//        if (activeNetwork == null) {
//            StandardObjects.connectionErrorRestartDialog(this@WelcomeActivity).show()
//            return false
//        }

        if (!StandardObjects.checkPermissionGranted(this@WelcomeActivity).first) {

            val permissionDialog = StandardObjects.checkPermissionGranted(this@WelcomeActivity).second
            permissionDialog!!.show()
            permissionDialog.setOnDismissListener { finish() }
            return false
        }
        pbWelcome.incrementProgressBy(20)
        pbWelcome.incrementProgressBy(20)
        Log.d("WelcomeLoad", "Checking For Permission Done")
        return true
    }

    fun getOTP() {
        otpString = "123456"
        pbWelcome.incrementProgressBy(30)
        Log.d("WelcomeLoad", "Check Session Done With New Token")
//        var loginInfo: LoginInfo = LoginInfo()
//
//        loginInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id)).toString()
//
//        if (android.os.Build.VERSION.SDK_INT >= 26) {
//            loginInfo.imeiNo = tm!!.getImei()
//        } else {
//            loginInfo.imeiNo = tm!!.getDeviceId()
//        }
//
//        val call = accountAPIs.login(loginInfo)
//
//        APIHelper.enqueueWithRetry(call, 5, object : Callback<String> {
//            override fun onResponse(call: Call<String>, response: Response<String>) {
//                /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of UserInfo POJO class
//        */
//                if (response.isSuccessful) {
//                    Log.d("LoginResponse", response.body().toString())
//                    pbWelcome.incrementProgressBy(30)
//                    otpString = response.body().toString()
//
//
//                } else if (response.code().toString() == getString(R.string.user_not_found_code)) {
//
//                    val errorMessage: JsonObject = JsonParser().parse(response.errorBody()!!.string()).getAsJsonObject()
//                    Log.d("LoginErrorResponse", errorMessage.get("Message").toString())
//                    StandardObjects.userNotFound(this@WelcomeActivity, this@WelcomeActivity).show()
//
//                }
//            }
//
//            override fun onFailure(call: Call<String>, t: Throwable) {
//                StandardObjects.connectionErrorRestartDialog(this@WelcomeActivity).show()
//            }
//        })
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            btnContinue.id -> {

                if (isTokenValid!!){
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }else {
                    val intent = Intent(applicationContext, VerifyActivity::class.java)
                    intent.putExtra(OTP_INFO, otpString)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    fun loadBanner() {

        // Get Image URL from cache to display for Image List
        val cacheBannerManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_banner_list))
        var bannerListJsonResponse = cacheBannerManagement.readDataFromCache()
        val gsonBannerList = ArrayList<BannerInfo>()

        if (!bannerListJsonResponse.isEmpty()) {
            val bannerArray = JSONArray(bannerListJsonResponse)
            if (bannerArray.length() > 0) {
                val gson = Gson()
                var i = 0

                while (i < bannerArray.length()) {
                    gsonBannerList.add(gson.fromJson<BannerInfo>(bannerArray.getJSONObject(i).toString(), BannerInfo::class.java))
                    i++
                }
            }
//            setBannerRecyclerView(gsonBannerList)
        }
    }

//    fun setBannerRecyclerView(bannerList: ArrayList<BannerInfo>) {
//        recyclerViewBannerList.layoutManager = LinearLayoutManager(applicationContext, LinearLayout.VERTICAL, false)
//        recyclerViewBannerList.adapter = BannerListAdapter(bannerList, applicationContext)
//        recyclerViewBannerList.addItemDecoration(DividerItemDecoration(applicationContext, DividerItemDecoration.VERTICAL))
//        recyclerViewBannerList.addOnItemTouchListener(RecyclerTouchListener(applicationContext, recyclerViewBannerList, object : RecyclerTouchListener.ClickListener {
//            override fun onClick(view: View, position: Int) {
//                val imageSelected = bannerList.get(position)
//                if (!imageSelected.redirectUrl.isEmpty()) {
//                    StandardObjects.webViewURL = imageSelected.redirectUrl
//                    Log.d("welcomeImageURLSelected", imageSelected.imageUrl)
//                    Log.d("welcomeRedirectURL", imageSelected.redirectUrl)
//                    val intent = Intent(applicationContext, WebViewFullscreenActivity::class.java)
//                    intent.putExtra("ORDER_ITEMS", imageSelected.redirectUrl)
//                    startActivity(intent)
//                }
//            }
//
//            override fun onLongClick(view: View?, position: Int) {
//
//            }
//        }))
//
//        pbWelcome.incrementProgressBy(20)
//        Log.d("WelcomeLoad", "Load Banner Done")
//    }

    fun checkSessionToken() {
        val checkSessionInfo: CheckSessionInfo = CheckSessionInfo()
            checkSessionInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_session_id)).toString()

        Log.d("SessionCheckToken",checkSessionInfo.sessionTokenID)

        if (checkSessionInfo.sessionTokenID.isNullOrEmpty()){
            isTokenValid = false
            pbWelcome.incrementProgressBy(20)
            getOTP()
        }else{
            isTokenValid = true
            getProductType()
            getOustandingList()
            pbWelcome.incrementProgressBy(50)
            Log.d("WelcomeLoad", "Check Session Done With Existing Token")
        }


    }

    fun getProductType() {

        var getProductTypeInfo: GetProductTypeInfo = GetProductTypeInfo()
        getProductTypeInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id))!!
        getProductTypeInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_session_id))!!
        if (android.os.Build.VERSION.SDK_INT >= 26) {
//            getProductTypeInfo.imeiNo = tm!!.getImei()
            getProductTypeInfo.imeiNo = StandardObjects.sampleImei
        } else {
//            getProductTypeInfo.imeiNo = tm!!.getDeviceId()
            getProductTypeInfo.imeiNo = StandardObjects.sampleImei
        }

        // Store JSON Response String to Cache
        val cacheOrderListManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_product_type))
        cacheOrderListManagement.writeDataToCache(getString(R.string.cache_product_type_data))
    }

    fun getOustandingList() {

        var trackInfo: TrackInfo = TrackInfo()

        // Retrieve from shared preference the Session Token ID to verify whether if it is still valid or not from server side
        trackInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id))!!
        trackInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_session_id))!!
        trackInfo.orderNo = ""
        trackInfo.currentNumberOfRecord = "0"

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            trackInfo.imeiNo = StandardObjects.sampleImei
//            trackInfo.imeiNo = tm!!.getImei()
        } else {
//            trackInfo.imeiNo = tm!!.getDeviceId()
            trackInfo.imeiNo = StandardObjects.sampleImei
        }

        Log.d("VerifyRegistrationID", trackInfo.registrationID)
        Log.d("VerifySessionTokenID", trackInfo.sessionTokenID)
        Log.d("VerifyOrderNo", trackInfo.orderNo)
        Log.d("VerifyImeiNo", trackInfo.imeiNo)

        // Store JSON Response String to Cache
        val cacheOrderListManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_outstanding_order_list))
        cacheOrderListManagement.writeDataToCache(getString(R.string.cache_outstanding_order_list_data))
    }

    fun roundCornerImages(image: Int, radius: Float, imageView: ImageView){
        var imageBitmap: Bitmap = BitmapFactory.decodeResource(getResources(), image);
        var dr: RoundedBitmapDrawable = RoundedBitmapDrawableFactory.create(resources,imageBitmap)
        dr.setCornerRadius(radius)
        imageView.setImageDrawable(dr)
    }
}