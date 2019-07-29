package com.biz.aceras.ordertracking.activities

import android.app.ActionBar
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.biz.aceras.ordertracking.R
import com.biz.aceras.ordertracking.StandardObjects
import kotlinx.android.synthetic.main.activity_track_item_info.*
import com.biz.aceras.ordertracking.serializer_class.OrderItemInfo
import java.text.SimpleDateFormat


class TrackItemInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_item_info)

        // Action Bar configuration
        getSupportActionBar()!!.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar()!!.setCustomView(R.layout.actionbar_replacement);
//        getSupportActionBar()!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#FFFFFF")));
        getSupportActionBar()!!.setDisplayShowTitleEnabled(false)
        StandardObjects.setBackButton(this@TrackItemInfoActivity)

        val orderItemSelected = getIntent().getSerializableExtra("ORDER_ITEMS") as OrderItemInfo

        tvItemNo.text = orderItemSelected.itemNumber
        tvFinalDestination.text = orderItemSelected.finalDestination

        tvProdDesc.text = orderItemSelected.productDesc
        tvProdQty.text = orderItemSelected.productQty

        tvTargetConfirmed.text = timeStampToDate(orderItemSelected.targetConfirmed)
        tvActualConfirmed.text = timeStampToDate(orderItemSelected.actualConfirmed)
        tvTargetDelivered.text = timeStampToDate(orderItemSelected.targetDelivered)
        tvActualDelivered.text = timeStampToDate(orderItemSelected.actualDelivered)
        tvTargetShipped.text = timeStampToDate(orderItemSelected.targetShipped)
        tvActualShipped.text = timeStampToDate(orderItemSelected.actualShipped)
        tvTargetProduced.text = timeStampToDate(orderItemSelected.targetProduced)
        tvActualProduced.text = timeStampToDate(orderItemSelected.actualProduced)
        tvTargetTaken.text = timeStampToDate(orderItemSelected.actualTaken)
        tvActualTaken.text = timeStampToDate(orderItemSelected.actualTaken)
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

            when (item!!.getItemId()) {

                getString(R.string.back_button_id).toInt() -> {
                    finish()
                }

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

        return super.onOptionsItemSelected(item)

    }
}