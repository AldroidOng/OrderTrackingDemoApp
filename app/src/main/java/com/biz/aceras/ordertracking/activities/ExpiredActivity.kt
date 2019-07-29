package com.biz.aceras.ordertracking.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.serializer_class.LoginInfo
import kotlinx.android.synthetic.main.activity_expired.*

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ExpiredActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expired)

        // whether if the response is success or fail it will still go to the OTP screen for verification,
        // cause from there they can tap on a text to get another new OTP even if it failed
        btnRequestNewOTP.setOnClickListener(this)
    }

    override fun onClick(view: View?) {

        var loginInfo: LoginInfo = LoginInfo()
        loginInfo.registrationID = SharedPreference(applicationContext).getValueString(getString(R.string.pref_registration_id))!!

        //Obtain an instance of Retrofit by calling the static method.
        val retrofit = NetworkClient.getRetrofitClient()

        // The main purpose of Retrofit is to create HTTP calls from the Kotlin interface based on the
        // annotation associated with each method.
        // This is achieved by just passing the interface class as parameter to the create method
        val accountAPIs = retrofit!!.create(OrderWebAPI::class.java)

        // Invoke the method corresponding to the HTTP request which will return a Call object.
        // This Call object will used to send the actual network request with the specified parameters
        val call = accountAPIs.login(loginInfo)

        // This is the line which actually sends a network request.
        // Calling enqueue() executes a call asynchronously.
        // It has two callback listeners which will invoked on the main thread

        APIHelper.enqueueWithRetry(call, 5, object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                /*This is the success callback. Though the response type is JSON, with Retrofit we get the response in the form of UserInfo POJO class
        */
                if (response.isSuccessful) {

                } else {
                    Toast.makeText(applicationContext, response.message(), Toast.LENGTH_LONG).show()
                }

            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(applicationContext, "Error response when trying to call API", Toast.LENGTH_LONG).show()
                // might need to try again if got error, use below reference and look for "Authenticator"
                // https://medium.com/knowing-android/headers-interceptors-and-authenticators-with-retrofit-1a00fed0d5eb
            }
        })

        val intent: Intent = Intent(applicationContext, VerifyActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()

        StandardObjects.removeCache(applicationContext)
    }
}