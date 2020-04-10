package com.biz.aceras.ordertracking.activities

import android.app.ActionBar
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.StandardObjects.sampleImei
import com.biz.aceras.ordertracking.serializer_class.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_verify.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class VerifyActivity : AppCompatActivity(), View.OnClickListener {
    private var verifyInfo: VerifyInfo = VerifyInfo()
    private val size: Int = 1
    private var wrongInputCount = 0
    private var dispOTP = ""
    private var tm: TelephonyManager? = null
    private val retrofit = NetworkClient.getRetrofitClient()
    private val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)
    private var getProductTypeDone: Boolean = false
    private var getOustandingList: Boolean = false
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)

        progressDialog = StandardObjects.showProgressDialog(this@VerifyActivity, "Verifying...")

        tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        // Action Bar configuration
        StandardObjects.actionBarConfig(this@VerifyActivity)

        tvIncorrectCount.text = "0"
        val getIntent: Intent = getIntent()

        tvOTP1.text = getIntent.getStringExtra(WelcomeActivity().OTP_INFO)[0].toString()
        tvOTP2.text = getIntent.getStringExtra(WelcomeActivity().OTP_INFO)[1].toString()
        tvOTP3.text = getIntent.getStringExtra(WelcomeActivity().OTP_INFO)[2].toString()
        tvOTP4.text = getIntent.getStringExtra(WelcomeActivity().OTP_INFO)[3].toString()
        tvOTP5.text = getIntent.getStringExtra(WelcomeActivity().OTP_INFO)[4].toString()
        tvOTP6.text = getIntent.getStringExtra(WelcomeActivity().OTP_INFO)[5].toString()

        etOTP1.requestFocus();

        etOTP1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etOTP1.text.toString().length == size)     //size as per your requirement
                {
                    etOTP2.requestFocus();
                }
            }

        })

        etOTP2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etOTP2.text.toString().length == size)     //size as per your requirement
                {
                    etOTP3.requestFocus();
                }
            }

        })

        etOTP3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etOTP3.text.toString().length == size)     //size as per your requirement
                {
                    etOTP4.requestFocus();
                }
            }

        })

        etOTP4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etOTP4.text.toString().length == size)     //size as per your requirement
                {
                    etOTP5.requestFocus();
                }
            }

        })

        etOTP5.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (etOTP5.text.toString().length == size)     //size as per your requirement
                {
                    etOTP6.requestFocus();
                }
            }

        })

        btnVerify.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            btnVerify.id -> {
                if (isValid()) {
                    btnVerify.isEnabled = false

                    progressDialog!!.show()
                    if (isMatch()) {
                        // Get Imei No.
                        if (android.os.Build.VERSION.SDK_INT >= 26) {
//                            verifyInfo.imeiNo = tm!!.getImei()
                            verifyInfo.imeiNo = sampleImei
                        } else {
                            verifyInfo.imeiNo = sampleImei
//                            verifyInfo.imeiNo = tm!!.getDeviceId()
                        }
                        // Get User ID from shared preference
                        verifyInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id)).toString()
                        // Concatenate all the edit text of OTP into a single string
                        verifyInfo.otp = etOTP1.text.toString() + etOTP2.text.toString() + etOTP3.text.toString() +
                                etOTP4.text.toString() + etOTP5.text.toString() + etOTP6.text.toString()

                        Log.d("VerifyRegID", verifyInfo.registrationID)
                        Log.d("VerifyOTP", verifyInfo.otp)
                        Log.d("VerifyImei", verifyInfo.imeiNo)

                        SharedPreference(applicationContext).save(getString(R.string.pref_session_id), "123456789")
                        getOustandingList()
                        getProductType()
                    } else {
                        wrongInputCount = tvIncorrectCount.text.toString().toInt() + 1
                        tvIncorrectCount.text = wrongInputCount.toString()
                        if (wrongInputCount < 3) {
                            StandardObjects.incorrectOTP(this@VerifyActivity)
                            progressDialog!!.dismiss()
                            btnVerify.isEnabled = true
                        } else if (wrongInputCount == 3) {
                            wrongInputCount = 0
                            tvIncorrectCount.text = "0"
                            btnVerify.isEnabled = false
                            dispOTP = ""
                            tvOTP1.text = ""
                            tvOTP2.text = ""
                            tvOTP3.text = ""
                            tvOTP4.text = ""
                            tvOTP5.text = ""
                            tvOTP6.text = ""
                            tvGenerateOTP.visibility = VISIBLE

                            var loginInfo: LoginInfo = LoginInfo()
                            val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                            loginInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id))!!

                            if (android.os.Build.VERSION.SDK_INT >= 26) {
//                                loginInfo.imeiNo = tm.getImei()
                                loginInfo.imeiNo = StandardObjects.sampleImei
                            } else {
//                                loginInfo.imeiNo = tm.getDeviceId()
                                loginInfo.imeiNo = StandardObjects.sampleImei
                            }

                            val min = 0
                            val max = 999999
                            val random: Int = Random().nextInt((max - min) + 1) + min

                            tvGenerateOTP.visibility = INVISIBLE
                            tvOTP1.text = random.toString()[0].toString()
                            tvOTP2.text = random.toString()[1].toString()
                            tvOTP3.text = random.toString()[2].toString()
                            tvOTP4.text = random.toString()[3].toString()
                            tvOTP5.text = random.toString()[4].toString()
                            tvOTP6.text = random.toString()[5].toString()
                            progressDialog!!.dismiss()
                            btnVerify.isEnabled = true

                        }
                    }
                }
            }
        }
    }

    fun isValid(): Boolean {

        if (!StandardObjects.checkPermissionGranted(this@VerifyActivity).first) {
            StandardObjects.checkPermissionGranted(this@VerifyActivity).second!!.show()
            return false
        }

        return true
    }

    fun isMatch(): Boolean {
        dispOTP = tvOTP1.text.toString() + tvOTP2.text.toString() + tvOTP3.text.toString() +
                tvOTP4.text.toString() + tvOTP5.text.toString() + tvOTP6.text.toString()

        val userInputOTP = etOTP1.text.toString() + etOTP2.text.toString() + etOTP3.text.toString() +
                etOTP4.text.toString() + etOTP5.text.toString() + etOTP6.text.toString()

        if (dispOTP != userInputOTP) {
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        StandardObjects.removeCache(applicationContext)
    }

    fun getOustandingList() {

        var trackInfo: TrackInfo = TrackInfo()

        // Retrieve from shared preference the Session Token ID to verify whether if it is still valid or not from server side
        trackInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id))!!
        trackInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_session_id))!!
        trackInfo.orderNo = ""
        trackInfo.currentNumberOfRecord = "0"

        if (android.os.Build.VERSION.SDK_INT >= 26) {
            trackInfo.imeiNo = sampleImei
//            trackInfo.imeiNo = tm!!.getImei()
        } else {
            trackInfo.imeiNo = sampleImei
//            trackInfo.imeiNo = tm!!.getDeviceId()
        }

        Log.d("VerifyRegistrationID", trackInfo.registrationID)
        Log.d("VerifySessionTokenID", trackInfo.sessionTokenID)
        Log.d("VerifyOrderNo", trackInfo.orderNo)
        Log.d("VerifyImeiNo", trackInfo.imeiNo)

        // Store JSON Response String to Cache
        val cacheOrderListManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_outstanding_order_list))
        cacheOrderListManagement.writeDataToCache(getString(R.string.cache_outstanding_order_list_data))
        getOustandingList = true
        if (isCallComplete()) {
            progressDialog!!.dismiss()
            val intent: Intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    fun getProductType() {

        var getProductTypeInfo: GetProductTypeInfo = GetProductTypeInfo()
        getProductTypeInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id))!!
        getProductTypeInfo.sessionTokenID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_session_id))!!
        if (android.os.Build.VERSION.SDK_INT >= 26) {
//            getProductTypeInfo.imeiNo = tm!!.getImei()
            getProductTypeInfo.imeiNo = sampleImei
        } else {
//            getProductTypeInfo.imeiNo = tm!!.getDeviceId()
            getProductTypeInfo.imeiNo = sampleImei
        }

        // Store JSON Response String to Cache
        val cacheOrderListManagement: CacheManagement = CacheManagement(applicationContext, getString(R.string.cache_product_type))
        cacheOrderListManagement.writeDataToCache(getString(R.string.cache_product_type_data))
        getProductTypeDone = true
        if (isCallComplete()) {
            progressDialog!!.dismiss()
            val intent: Intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun isCallComplete(): Boolean {
        if (getOustandingList && getProductTypeDone) {
            return true
        } else {
            return false
        }
    }
}
