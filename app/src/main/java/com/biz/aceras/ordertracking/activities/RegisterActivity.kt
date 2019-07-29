package com.biz.aceras.ordertracking.activities

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_register.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.webkit.WebView
import android.webkit.WebViewClient
import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.telephony.TelephonyManager
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat
import android.support.v4.app.ActivityCompat
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.serializer_class.RegisterInfo

class RegisterActivity : AppCompatActivity(), View.OnClickListener {

    private val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 1
    private var registerInfo: RegisterInfo = RegisterInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        displayTermsAndPolicy(this)

        if (android.os.Build.VERSION.SDK_INT >= 26) {

            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@RegisterActivity, Manifest.permission.READ_PHONE_STATE)) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.permission_denied))
                    builder.setMessage(getString(R.string.permission_denied_text))
                    builder.setPositiveButton("OK", null)
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()
                    alertDialog.setOnDismissListener {
                        ActivityCompat.requestPermissions(this@RegisterActivity,
                                arrayOf(Manifest.permission.READ_PHONE_STATE),
                                MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                    }
                } else {
                    // No explanation needed, we can request the permission.
                    ActivityCompat.requestPermissions(this@RegisterActivity,
                            arrayOf(Manifest.permission.READ_PHONE_STATE),
                            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                }
            } else { // Permission has already been granted
                registerInfo.imeiNo = tm.getImei()

                Log.i("Log", "DeviceId = " + registerInfo.imeiNo)
            }
        } else {
            registerInfo.imeiNo = tm.getDeviceId()
            Log.i("Log", "DeviceId Before Version 26 = " + registerInfo.imeiNo)
        }

        btnRegister.setOnClickListener(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE -> {
                // If request is cancelled, the result arrays are empty.

                if (android.os.Build.VERSION.SDK_INT >= 26 && ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                    val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

                    registerInfo.imeiNo = tm.getImei()

                } else if (android.os.Build.VERSION.SDK_INT >= 26 &&
                        ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.READ_PHONE_STATE) !=
                                PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this@RegisterActivity,
                            Manifest.permission.READ_PHONE_STATE)) {
                        val builder = AlertDialog.Builder(this)
                        builder.setTitle(getString(R.string.permission_denied))
                        builder.setMessage(getString(R.string.permission_denied_text))
                        builder.setPositiveButton("OK", null)
                        val alertDialog: AlertDialog = builder.create()
                        alertDialog.show()
                        alertDialog.setOnDismissListener {
                            ActivityCompat.requestPermissions(this@RegisterActivity,
                                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE)
                        }
                    }
                }
                return
            }

        // Add other 'when' lines to check for other
        // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    override fun onClick(view: View?) {
        when (view!!.id) {
            btnRegister.id -> {
                if (!isValid()) {
                    btnRegister.isEnabled = true
                    return
                }

                btnRegister!!.isEnabled = false

                val progressDialog: ProgressDialog = StandardObjects.showProgressDialog(this@RegisterActivity,"Authenticating...")
                progressDialog.show()

                registerInfo.email = etEmail.text.toString()
                registerInfo.phoneNo = etPhoneNo.text.toString()

                val builder = AlertDialog.Builder(this)

                builder.setTitle("Error")
                builder.setPositiveButton("OK", null)

                //Obtain an instance of Retrofit by calling the static method.
                val retrofit = NetworkClient.getRetrofitClient()

                // The main purpose of Retrofit is to create HTTP calls from the Kotlin interface based on the
                // annotation associated with each method.
                // This is achieved by just passing the interface class as parameter to the create method
                val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)

                // Invoke the method corresponding to the HTTP request which will return a Call object.
                // This Call object will used to send the actual network request with the specified parameters
                val call = accountAPIs.register(registerInfo)

                Log.d("RegisterEmail", registerInfo.email)
                Log.d("RegisterImei", registerInfo.imeiNo)
                Log.d("RegisterPhone", registerInfo.phoneNo)

                val intent: Intent = Intent(applicationContext, ActivateActivity::class.java)
                startActivity(intent)
                finish()

//                APIHelper.enqueueWithRetry(call, 5, object : Callback<String> {
//                    override fun onResponse(call: Call<String>, response: Response<String>) {
//                        /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of UserInfo POJO class
//                */
//                        if (response.isSuccessful) {
//                            //SharedPreference(applicationContext).save(getString(R.string.pref_activation_code), response.body().toString())
//                            SharedPreference(applicationContext).save(getString(R.string.pref_activation_code), response.body().toString())
//                            progressDialog.dismiss()
//                            Log.d("RegisterResponse", response.body().toString())
//                            val intent: Intent = Intent(applicationContext, ActivateActivity::class.java)
//                            startActivity(intent)
//                            finish()
//                        } else if(response.code().toString() == getString(R.string.user_not_found_code)) {
//                            builder.setMessage("The email address/phone number combination is not valid")
//                            builder.setCancelable(false)
//                            val alertDialog: AlertDialog = builder.create()
//                            alertDialog.show()
//                            btnRegister.isEnabled = true
//                            progressDialog.dismiss()
//                            Log.d("Register Error Response", response.errorBody()!!.string())
//                        }
//                    }
//
//                    override fun onFailure(call: Call<String>, t: Throwable) {
//
//                        builder.setMessage("Error when trying to call API")
//                        val alertDialog: AlertDialog = builder.create()
//
//                        alertDialog.show()
//                        btnRegister.isEnabled = true
//                        progressDialog.dismiss()
//                    }
//                })
            }
        }
    }

    fun isValid(): Boolean {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Warning")
        builder.setPositiveButton("OK", null)

        if (etEmail.text.isEmpty()) {
            etEmail.error = "Email is required"
            return false
        }
        if (etPhoneNo.text.isEmpty()) {
            etPhoneNo.error = "Phone no. is required"
            return false
        }

        if(!StandardObjects.checkPermissionGranted(this@RegisterActivity).first){
            StandardObjects.checkPermissionGranted(this@RegisterActivity).second!!.show()
            return false
        }

        return true
    }

    fun displayTermsAndPolicy(context: Context) {
        val alert = AlertDialog.Builder(context)
        alert.setTitle("Terms of Service & Privacy Policy")
        val webView = WebView(context)
        webView.loadUrl("https://www.shippop.my/terms/")
        webView.settings.javaScriptEnabled = true
        webView.settings.setAppCachePath(context.getFilesDir().getPath() + "/webview")
        webView.webViewClient = WebViewClient()
        alert.setView(webView)
        alert.setCancelable(false)
        alert.setNeutralButton("Agree", { dialog, _ -> dialog.dismiss() })
        alert.setNegativeButton("Disagree", { _, _ -> finish() })
        alert.show()
    }

    override fun onDestroy() {
        super.onDestroy()

        StandardObjects.removeCache(applicationContext)
    }
}