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
import com.biz.aceras.ordertracking.*
import com.biz.aceras.ordertracking.serializer_class.ActivationInfo
import com.biz.aceras.ordertracking.serializer_class.ResendACInfo

import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_test.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TestActivity : AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.image_test)
        val resultURI = "https://aprilordertracking-dev.globalnet.lcl/BannerImages/18042019111531_fe7db8d9-4a7a-46bf-a13f-20205113c4eb_Banner-4.jpg"
//        val resultURI = "http://thewowstyle.com/wp-content/uploads/2015/02/Beautiful-Wallpapers-14.jpg"
//        Glide.with(this@TestActivity).load(resultURI).into(test)
//        Picasso.get().load(resultURI).into(test);
// implementation 'com.github.bumptech.glide:glide:4.9.0'
    }


}