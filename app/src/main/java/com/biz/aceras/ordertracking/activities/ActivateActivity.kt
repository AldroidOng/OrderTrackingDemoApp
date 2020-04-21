package com.biz.aceras.ordertracking.activities

import android.app.ActionBar
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_activate.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.StandardObjects.sampleImei
import com.biz.aceras.ordertracking.serializer_class.ActivationInfo
import com.biz.aceras.ordertracking.serializer_class.ResendACInfo


class ActivateActivity : AppCompatActivity(), View.OnClickListener {
    var activationInfo: ActivationInfo = ActivationInfo()
    var resendACInfo: ResendACInfo = ResendACInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_activate)

        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()!!.setCustomView(R.layout.actionbar_replacement);
        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFFFFF")));
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)

        tvResendAC.setOnClickListener(this)
        btnActivate.setOnClickListener(this)
    }

    override fun onClick(view: View?) {

        val retrofit = NetworkClient.getRetrofitClient()
        val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        when (view!!.id) {
            btnActivate.id -> {
                if (isActivateValid()) {
                    btnActivate.isEnabled = false
                    val progressDialog: ProgressDialog = StandardObjects.showProgressDialog(this@ActivateActivity, "Authenticating...")
                    progressDialog.show()
                    if (android.os.Build.VERSION.SDK_INT >= 26) {
//                        activationInfo.imeiNo = tm.getImei()
                        activationInfo.imeiNo = sampleImei
                    } else {
//                        activationInfo.imeiNo = tm.getDeviceId()
//                        activationInfo.imeiNo = sampleImei
                    }

//                    // Get Part 1 of the activation code from shared preference
//                    activationInfo.cacheActivationCode = SharedPreference(applicationContext).getValueString(getString(R.string.pref_activation_code)).toString()
//
//                    // Get Part 2 of the activation code from user input
//                    activationInfo.smsActivationCode = etActivationCode.text.toString()

                    val call = accountAPIs.activate(activationInfo)

                    Log.d("ActivateSharedPref", activationInfo.cacheActivationCode)
                    Log.d("ActivateSMS", activationInfo.smsActivationCode)
                    Log.d("ActivateImei", activationInfo.imeiNo)


                    SharedPreference(applicationContext).removeValue(getString(R.string.pref_activation_code))
                    SharedPreference(applicationContext).save(getString(R.string.pref_registration_id), "RegIDTest")
                    val intent: Intent = Intent(applicationContext, WelcomeActivity::class.java)
                    startActivity(intent)
                    finish()

//                    APIHelper.enqueueWithRetry(call, 5, object : Callback<String> {
//                        override fun onResponse(call: Call<String>, response: Response<String>) {
//                            /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of UserInfo POJO class
//                */
//                            if (response.isSuccessful) {
//                                SharedPreference(applicationContext).removeValue(getString(R.string.pref_activation_code))
//                                SharedPreference(applicationContext).save(getString(R.string.pref_registration_id), response.body().toString())
//                                Log.d("ActivateResponse", response.body().toString())
//                                progressDialog.dismiss()
//                                val intent: Intent = Intent(applicationContext, WelcomeActivity::class.java)
//                                startActivity(intent)
//                                finish()
//                            } else if(response.code() == 409){
//                                StandardObjects.wrongActivationCode(this@ActivateActivity).show()
//                                progressDialog.dismiss()
//                                btnActivate.isEnabled = true
////                                Toast.makeText(this@ActivateActivity, response.errorBody()!!.string(), Toast.LENGTH_LONG).show()
//                            }
//                        }
//
//                        override fun onFailure(call: Call<String>, t: Throwable) {
//                            Toast.makeText(this@ActivateActivity, "Error when activating", Toast.LENGTH_LONG).show()
//                            progressDialog.dismiss()
//                            btnActivate.isEnabled = true
//                        }
//                    })
                }
            }

            tvResendAC.id -> {
//                if (isResendValid()) {
//
//                    tvResendAC!!.isClickable = false
//                    val resendACProgressDialog = StandardObjects.showProgressDialog(this@ActivateActivity, "Generating New Activation Code...")
//                    resendACProgressDialog.show()
//
//                    if (android.os.Build.VERSION.SDK_INT >= 26) {
//                        resendACInfo.imeiNo = tm.getImei()
//                    } else {
//                        resendACInfo.imeiNo = tm.getDeviceId()
//                    }
//
//                    resendACInfo.cacheActivationCode = SharedPreference(applicationContext).getValueString(getString(R.string.pref_activation_code)).toString()
//
//                    val call = accountAPIs.resendAC(resendACInfo)
//
//                    APIHelper.enqueueWithRetry(call, 5, object : Callback<String> {
//                        override fun onResponse(call: Call<String>, response: Response<String>) {
//                            /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of UserInfo POJO class
//            */
//                            if (response.isSuccessful) {
//                                SharedPreference(applicationContext).save(getString(R.string.pref_activation_code), response.body().toString())
//                                Log.d("RegisterResponse", response.body().toString())
//                                resendACProgressDialog.dismiss()
//                                tvResendAC!!.isClickable = true
//                            } else {
//                                Toast.makeText(this@ActivateActivity, response.errorBody()!!.string(), Toast.LENGTH_LONG).show()
//                                resendACProgressDialog.dismiss()
//                                tvResendAC!!.isClickable = true
//                            }
//                        }
//
//                        override fun onFailure(call: Call<String>, t: Throwable) {
//                            Toast.makeText(this@ActivateActivity, "Error when generating new Activation Code, kindly try again later", Toast.LENGTH_LONG).show()
//                            resendACProgressDialog.dismiss()
//                            tvResendAC!!.isClickable = true
//                        }
//                    })
//                }
            }
        }
    }

    fun isActivateValid(): Boolean {

//        val activeNetwork = StandardObjects.checkActiveNetwork(this@ActivateActivity)
//        if (activeNetwork == null) {
//            Toast.makeText(this@ActivateActivity, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
//            return false
//        }

        if (etActivationCode.text.toString().isEmpty()) {
            Toast.makeText(this@ActivateActivity, "Activation code cannot be empty", Toast.LENGTH_LONG).show()
            return false
        }

        if (!StandardObjects.checkPermissionGranted(this@ActivateActivity).first) {
            StandardObjects.checkPermissionGranted(this@ActivateActivity).second!!.show()
            return false
        }
        return true
    }

    fun isResendValid(): Boolean {

        val activeNetwork = StandardObjects.checkActiveNetwork(this@ActivateActivity)
        if (activeNetwork == null) {
            Toast.makeText(this@ActivateActivity, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show()
            return false
        }

        if (!StandardObjects.checkPermissionGranted(this@ActivateActivity).first) {
            StandardObjects.checkPermissionGranted(this@ActivateActivity).second!!.show()
            return false
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()

        StandardObjects.removeCache(applicationContext)
    }
}