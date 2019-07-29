package com.biz.aceras.ordertracking.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.webkit.WebViewClient
import com.biz.aceras.ordertracking.R
import com.biz.aceras.ordertracking.StandardObjects
import kotlinx.android.synthetic.main.activity_webview_fullscreen.*

class WebViewFullscreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_fullscreen)

        StandardObjects.actionBarConfig(this@WebViewFullscreenActivity)
        StandardObjects.setBackButton(this@WebViewFullscreenActivity)

//        val getIntent: Intent = getIntent()
//        wvBanner.loadUrl(getIntent.getStringExtra(MainActivity().WEBVIEW_BANNER_INFO))
        wvWebpage.loadUrl(StandardObjects.webViewURL)
        wvWebpage.getSettings().setJavaScriptEnabled(true)
        wvWebpage.setWebViewClient(WebViewClient())
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.getItemId()) {

            getString(R.string.back_button_id).toInt() -> {
                finish()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}