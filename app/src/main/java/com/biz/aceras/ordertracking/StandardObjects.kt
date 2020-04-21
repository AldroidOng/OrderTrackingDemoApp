package com.biz.aceras.ordertracking

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v4.content.ContextCompat


import android.support.v7.app.AlertDialog
import android.graphics.Color
import com.biz.aceras.ordertracking.activities.SplashActivity
import android.view.Gravity
import android.graphics.Color.DKGRAY
import android.support.v7.app.AppCompatActivity
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.TextView
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import com.biz.aceras.ordertracking.R.id.btnExit
import com.biz.aceras.ordertracking.activities.MainActivity
import com.biz.aceras.ordertracking.activities.RegisterActivity
import com.biz.aceras.ordertracking.activities.VerifyActivity
import com.biz.aceras.ordertracking.serializer_class.CheckSessionInfo
import com.biz.aceras.ordertracking.serializer_class.LoginInfo
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.session_expired_dialog.*
import kotlinx.android.synthetic.main.session_expired_dialog.view.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import javax.security.auth.Destroyable


/**
 * Created by eesern_ong on 29/4/2019.
 */
object StandardObjects {

    val sampleImei = "12345"
    var webViewURL = ""

    fun checkPermissionGranted(context: Context): Pair<Boolean, AlertDialog?> {
//        if (android.os.Build.VERSION.SDK_INT >= 26) {
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            val builder = AlertDialog.Builder(context)
            builder.setTitle(context.getString(R.string.enable_permission))
            builder.setMessage(context.getString(R.string.enable_permission_text))
            builder.setCancelable(false)
            builder.setPositiveButton("OK", null)
            val alertDialog: AlertDialog = builder.create()

            return Pair(false, alertDialog)

        } else {
            return Pair(true, null)
        }
//        }else return Pair(true, null)
    }

    fun checkActiveNetwork(context: Context): NetworkInfo? {

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo
    }

    fun removeCache(context: Context) {

//        CacheManagement(context, context.getString(R.string.cache_banner_list)).deleteCache()
//        CacheManagement(context, context.getString(R.string.cache_banner_swipeable)).deleteCache()
//        CacheManagement(context, context.getString(R.string.cache_order)).deleteCache()
    }

    fun connectionErrorDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
//        val intent: Intent = Intent(appContext, SplashActivity::class.java)
        builder.setTitle(context.getString(R.string.connection_error))
        builder.setMessage(context.getString(R.string.connection_error_text))
        builder.setPositiveButton("OK", { _, _ -> connectionErrorDialog(context).dismiss() })
        builder.setCancelable(false)
        val alertDialog: AlertDialog = builder.create()

        return alertDialog
    }

    fun connectionErrorRestartDialog(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
//        val intent: Intent = Intent(appContext, SplashActivity::class.java)
        builder.setTitle(context.getString(R.string.connection_error))
        builder.setMessage(context.getString(R.string.connection_error_text))
        builder.setPositiveButton("TRY AGAIN", { _, _ ->
            val mStartActivity = Intent(context, SplashActivity::class.java)
            val mPendingIntentId = 123456
            val mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity,
                    PendingIntent.FLAG_CANCEL_CURRENT)
            val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent)
            System.exit(0)

        })
        builder.setCancelable(false)
        val alertDialog: AlertDialog = builder.create()

        return alertDialog
    }

    fun sessoinExpiredDialog(context: Context, activity: Activity): AlertDialog {
        // Creating the AlertDialog with a custom xml layout (you can still use the default Android version)
        val builder = AlertDialog.Builder(context)
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.session_expired_dialog, null)

        // If user pressed Exit:
        val exitButton = view.findViewById<View>(R.id.btnExit) as Button
        exitButton.setOnClickListener(View.OnClickListener { _ -> activity.finish() })

        // If user pressed OK:
        val okButton = view.findViewById<View>(R.id.btnOK) as Button
        okButton.setOnClickListener(View.OnClickListener { _ ->
            var loginInfo: LoginInfo = LoginInfo()
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            loginInfo.registrationID = SharedPreference(context).getValueString(context.getString(R.string.pref_registration_id))!!

            if (StandardObjects.checkPermissionGranted(context).first) {
                if (android.os.Build.VERSION.SDK_INT >= 26) {
//                    loginInfo.imeiNo = tm.getImei()
                    loginInfo.imeiNo = StandardObjects.sampleImei
                } else {
//                    loginInfo.imeiNo = StandardObjects.sampleImei
//                    loginInfo.imeiNo = tm.getDeviceId()
                }
            } else {
                StandardObjects.checkPermissionGranted(context).second!!.show()
                StandardObjects.checkPermissionGranted(context).second!!.setOnDismissListener { activity.finish() }
            }

            //Obtain an instance of Retrofit by calling the static method.
            val retrofit = NetworkClient.getRetrofitClient()

            // The main purpose of Retrofit is to create HTTP calls from the Kotlin interface based on the
            // annotation associated with each method.
            // This is achieved by just passing the interface class as parameter to the create method
            val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)

            // Invoke the method corresponding to the HTTP request which will return a Call object.
            // This Call object will used to send the actual network request with the specified parameters
            val call = accountAPIs.login(loginInfo)

            Log.d("LoginRegID", loginInfo.registrationID)
            Log.d("LoginImei", loginInfo.imeiNo)

            APIHelper.enqueueWithRetry(call, 5, object : Callback<String> {
                override fun onResponse(call: Call<String>, response: Response<String>) {
                    /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of UserInfo POJO class
            */
                    if (response.isSuccessful) {
                        Log.d("LoginResponse", response.body().toString())
                        val intent: Intent = Intent(context, VerifyActivity::class.java)

                        val OTP_INFO = "com.biz.aceras.ordertracking.OTP"
                        intent.putExtra(OTP_INFO, response.body().toString())
                        activity.startActivity(intent)
                        activity.finish()
                    } else {
                        val errorMessage: JsonObject = JsonParser().parse(response.errorBody()!!.string()).getAsJsonObject()
                        Log.d("LoginErrorResponse", errorMessage.asString)
                    }
                }

                override fun onFailure(call: Call<String>, t: Throwable) {
                    StandardObjects.connectionErrorDialog(context).show()
                }
            })
        })
        builder.setCancelable(false)
        builder.setView(view)

        // Set customise Title here
        val title = TextView(context)
        title.text = context.getString(R.string.session_expired)
        title.setBackgroundColor(Color.DKGRAY)
        title.setPadding(10, 10, 10, 10)
        title.gravity = Gravity.CENTER
        title.setTextColor(Color.WHITE)
        title.textSize = 20f
        builder.setCustomTitle(title)

        val alertDialog: AlertDialog = builder.create()

        return alertDialog
    }

    fun userNotFound(context: Context, activity: Activity): AlertDialog {
        val builder = AlertDialog.Builder(context)

//        builder.setTitle(context.getString(R.string.connection_error))
        builder.setMessage(context.getText(R.string.user_not_found_text))
        builder.setNeutralButton("OK", { dialog, _ ->
            val intent: Intent = Intent(context, RegisterActivity::class.java)
            activity.startActivity(intent)
            activity.finish()
        })
        builder.setCancelable(false)
        builder.setNegativeButton("EXIT", { _, _ -> activity.finish() })
        builder.setCancelable(false)
        val alertDialog: AlertDialog = builder.create()

        return alertDialog
    }

    fun wrongActivationCode(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        //        builder.setTitle(context.getString(R.string.connection_error))
        builder.setMessage(context.getText(R.string.wrong_activation_code_text))
        builder.setNegativeButton("OK", { _, _ -> wrongActivationCode(context).dismiss() })
        builder.setCancelable(false)
        val alertDialog: AlertDialog = builder.create()

        return alertDialog
    }

    fun incorrectOTP(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        //        builder.setTitle(context.getString(R.string.connection_error))
        builder.setMessage(context.getText(R.string.incorrect_otp_text))
        builder.setNegativeButton("OK", { _, _ -> incorrectOTP(context).dismiss() })
        builder.setCancelable(false)
        val alertDialog: AlertDialog = builder.create()

        return alertDialog
    }

    fun maxIncorrectOTP(context: Context): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setCancelable(false)
        //        builder.setTitle(context.getString(R.string.connection_error))
        builder.setMessage(context.getText(R.string.incorrect_otp_text))
        builder.setNegativeButton("OK", { _, _ -> incorrectOTP(context).dismiss() })
        builder.setCancelable(false)
        val alertDialog: AlertDialog = builder.create()

        return alertDialog
    }

    fun showProgressDialog(context: Context, message: String): ProgressDialog {

        val progressDialog = ProgressDialog(context, R.style.AppTheme_Dialog)
        progressDialog.isIndeterminate = true
        progressDialog.setMessage(message)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
//        progressDialog.show()

        return progressDialog
    }

    fun actionBarConfig(appCompatActivity: AppCompatActivity) {
        appCompatActivity.getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        appCompatActivity.getSupportActionBar()!!.setCustomView(R.layout.actionbar_replacement);
//        appCompatActivity.getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFFFFF")));
        appCompatActivity.getSupportActionBar()!!.setDisplayShowTitleEnabled(false)
    }

    fun setBackButton(appCompatActivity: AppCompatActivity) {
        // add back arrow to toolbar
        if (appCompatActivity.getSupportActionBar() != null) {
            appCompatActivity.getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
            appCompatActivity.getSupportActionBar()!!.setDisplayShowHomeEnabled(true);
        }
    }

    fun timeStampToDate(timeStamp: String): String {
        if (!timeStamp.isEmpty()) {
            var dashReplacementTimeStamp = timeStamp
            // Replace all en dash with hyphen
            dashReplacementTimeStamp = dashReplacementTimeStamp.replace("\u2013", "-")
            // Replace all em dash with hyphen
            dashReplacementTimeStamp = dashReplacementTimeStamp.replace("\u2014", "-")
            val inputTimeStampFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val outputDateFormat = SimpleDateFormat("dd MMMM yyyy")
            val parsedDateTime = inputTimeStampFormat.parse(dashReplacementTimeStamp)
            return outputDateFormat.format(parsedDateTime)
        }
        return ""
    }

}