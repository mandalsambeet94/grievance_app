package com.supragyan.grievancems.ui.home

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.supragyan.grievancems.R
import com.supragyan.grievancems.databinding.ActivityTotalSurveysBinding
import com.supragyan.grievancems.databinding.ViewImageActivityBinding
import com.supragyan.grievancems.ui.database.SQLiteDB
import com.supragyan.grievancems.utility.SharedPreferenceClass
import java.io.File
import java.net.URLEncoder
import kotlin.ranges.contains

class ViewImageActivity: AppCompatActivity() {
    private lateinit var binding: ViewImageActivityBinding
    private var progressDialog: ProgressDialog? = null
    var gId = ""
    var imageUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ViewImageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@ViewImageActivity)
        progressDialog!!.setMessage("Loading Please wait")
        progressDialog!!.setCancelable(false)
        //gId = intent.getStringExtra("GID")!!
        imageUrl = intent.getStringExtra("IMAGE_URL")!!
        binding.toolbar.toolbarTitle.text = "GRIEVANCE DETAILS"
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }

        binding.webview.webViewClient = object : WebViewClient(){
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                progressDialog!!.show()
            }

            override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
                view.loadUrl(url!!)
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                if (view!!.contentHeight == 0){
                    progressDialog!!.show()
                    view.reload()
                }else{
                    progressDialog!!.dismiss()
                }

            }
        }
        binding.webview.settings.javaScriptEnabled = true // enable javascript

        binding.webview.settings.loadWithOverviewMode = true
        binding.webview.settings.useWideViewPort = true
        binding.webview.settings.builtInZoomControls = true

        if (imageUrl.isNotBlank()) {
            if(imageUrl.contains(".pdf")||imageUrl.contains(".document")||
                imageUrl.contains(".presentation")||imageUrl.contains(".sheet")
                ||imageUrl.contains(".doc")||imageUrl.contains(".docx")
                ||imageUrl.contains(".xls")||imageUrl.contains(".xlsx")
                ||imageUrl.contains(".ppt")||imageUrl.contains(".pptx")){
                binding.imageView.visibility = View.GONE
                binding.webview.visibility = View.VISIBLE
                println("DOc $imageUrl")
                if(imageUrl.contains("https")){
                    binding.webview.loadUrl("https://docs.google.com/gview?embedded=true&url=" + URLEncoder.encode(imageUrl/*,"ISO-8859-1"*/))
                }else{
                    /*val file = File(imageUrl)
                    val uri = Uri.fromFile(file)
                    val url = "https://docs.google.com/gview?embedded=true&url=${Uri.encode(uri.toString())}"
                    binding.webview.loadUrl(url)*/
                    val file = File(imageUrl)
                    val uri = FileProvider.getUriForFile(
                        this,
                        "${packageName}.provider",
                        file
                    )

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.setDataAndType(uri, "*/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                    startActivity(intent)
                }

            }else{
                Glide.with(this)
                    .load(imageUrl)
                    .error(R.drawable.no_image)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable?>,
                            isFirstResource: Boolean
                        ): Boolean {
                            supportStartPostponedEnterTransition()
                            return false
                        }

                        override fun onResourceReady(resource: Drawable, model: Any, target: Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                            supportStartPostponedEnterTransition()
                            return false
                        }
                    })

                    .into(binding.imageView)
            }
        }
    }
}