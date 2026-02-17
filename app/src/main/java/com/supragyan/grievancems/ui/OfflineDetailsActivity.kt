package com.supragyan.grievancems.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isNotEmpty
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Request.Method
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.textfield.TextInputEditText
import com.supragyan.grievancems.R
import com.supragyan.grievancems.databinding.ActivityOfflineDetailsBinding
import com.supragyan.grievancems.databinding.ItemTopicBinding
import com.supragyan.grievancems.databinding.RowPhotosBinding
import com.supragyan.grievancems.ui.database.GrievanceModel
import com.supragyan.grievancems.ui.database.SQLiteDB
import com.supragyan.grievancems.utility.SharedPreferenceClass
import com.supragyan.grievancems.utility.Util
import com.supragyan.grievancems.webservices.AppController
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import kotlin.text.startsWith
import androidx.core.net.toUri
import com.supragyan.grievancems.ui.home.ViewImageActivity

class OfflineDetailsActivity: AppCompatActivity() {
    private lateinit var binding: ActivityOfflineDetailsBinding
    private var progressDialog: ProgressDialog? = null
    var sharedPreferenceClass: SharedPreferenceClass? = null
    private lateinit var db: SQLiteDB
    var offlineId = ""
    var grievanceID = ""
    val fileList = mutableListOf<Uri>()
    val uploadIdList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@OfflineDetailsActivity)
        sharedPreferenceClass = SharedPreferenceClass(this@OfflineDetailsActivity)
        progressDialog!!.setMessage("Loading Please wait")
        progressDialog!!.setCancelable(false)
        db = SQLiteDB(this)
        binding.toolbar.toolbarTitle.text = "OFFLINE GRIEVANCE DETAILS"
        offlineId = intent.getStringExtra("IDD")!!
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
        binding.recyclerViewD.layoutManager = LinearLayoutManager(this)
        val offlineData = db.getGrievanceByOfflineId(offlineId)
        println("offlineData $offlineData")

        if(!offlineData.block.isNullOrBlank()){
            binding.spinnerBlock.setText(offlineData.block)
        }
        if(!offlineData.gp.isNullOrBlank()){
            binding.spinnerGP.setText(offlineData.gp)
        }
        if(!offlineData.village.isNullOrBlank()){
            binding.spinnerVS.setText(offlineData.village)
        }
        if(!offlineData.address.isNullOrBlank()){
            binding.etAddress.setText(offlineData.address)
        }
        if(!offlineData.wardNo.isNullOrBlank()){
            binding.spinnerWN.setText(offlineData.wardNo)
        }
        if(!offlineData.name.isNullOrBlank()){
            binding.etName.setText(offlineData.name)
        }
        if(!offlineData.fatherName.isNullOrBlank()){
            binding.etFather.setText(offlineData.fatherName)
        }
        if(!offlineData.contact.isNullOrBlank()){
            binding.etContact.setText(offlineData.contact)
        }
        if(!offlineData.topic.isNullOrBlank()){
            val topicList = offlineData.topic.split(",").map { it.trim() }

            binding.recyclerViewD.adapter = TopicAdapter(topicList)
        }
        if(!offlineData.grievanceMatter.isNullOrBlank()){
            binding.etGrievanceMatter.setText(offlineData.grievanceMatter)
        }
        if(!offlineData.remark.isNullOrBlank()){
            binding.etRemark.setText(offlineData.remark)
        }
        if(!offlineData.photos.isNullOrBlank()){
            binding.ivPhoto.visibility = View.GONE
            val photoList = offlineData.photos.split(",").map { it.trim() }
            photoList.forEach { path ->
                if (path.isNotEmpty()) {
                    val uri = Uri.fromFile(File(path))
                    fileList.add(uri)
                }
            }
            println("fileList $fileList")
            binding.recycleViewPhotos.apply {
                layoutManager = LinearLayoutManager(
                    this@OfflineDetailsActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                //isNestedScrollingEnabled = false
                adapter = ImageAdapter(this@OfflineDetailsActivity,photoList)
            }
            //binding.recycleViewPhotos.adapter =
        }else{
            binding.ivPhoto.visibility = View.VISIBLE
        }

        binding.btnSubmit.setOnClickListener{
            if(Util.isNetworkAvailable(this@OfflineDetailsActivity)){
                saveGrievanceData(offlineData)
            }
        }
    }

   private class TopicAdapter(
        private val topicList: List<String>
    ) : RecyclerView.Adapter<TopicAdapter.TopicViewHolder>() {

        inner class TopicViewHolder(itemView: View) :
            RecyclerView.ViewHolder(itemView) {
            val etTopic1: TextInputEditText = itemView.findViewById(R.id.etTopic1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.topic_list_row, parent, false)
            return TopicViewHolder(view)
        }

        override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
            holder.etTopic1.setText(topicList[position])
        }

        override fun getItemCount(): Int = topicList.size
    }

    private class ImageAdapter(private val context: Context, private val images: List<String>) :
        RecyclerView.Adapter<ImageAdapter.ImageVH>() {
        inner class ImageVH(val binding: RowPhotosBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                binding.imageViewDelete.setOnClickListener {
                    val position = bindingAdapterPosition
                    /*if (position != RecyclerView.NO_POSITION) {
                        onRemove(position)
                    }*/
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVH {
            val binding = RowPhotosBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ImageVH(binding)
        }

        override fun onBindViewHolder(holder: ImageVH, position: Int) {
            holder.binding.imageViewDelete.visibility = View.GONE
            if (isImageFile(images[position])) {
                // show actual image
                val file = File(images[position])
                println("fileListAdapter "+Uri.fromFile(file))
                holder.binding.imageView.setImageURI(Uri.fromFile(file))

            } else {
                // show document icon
                holder.binding.imageView.setImageResource(R.drawable.documentation)
            }

            holder.binding.imageView.setOnClickListener {
                val intent = Intent(context, ViewImageActivity::class.java)
                intent.putExtra("IMAGE_URL", images[position])
                context.startActivity(intent)
            }
        }

        override fun getItemCount() = images.size

        private fun isImageFile(path: String): Boolean {
            val file = File(path)
            val extension = MimeTypeMap.getFileExtensionFromUrl(file.absolutePath)
                .lowercase()

            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension)

            return mimeType?.startsWith("image/") == true
        }
    }

    private fun saveGrievanceData(offlineData: GrievanceModel) {
        if (progressDialog != null) {
            progressDialog!!.show()
        }
        val tag = "user_save"
        val jObj = JSONObject()
        try {
            jObj.put("block", offlineData.block)
            jObj.put("gp", offlineData.gp)
            jObj.put("villageSahi", offlineData.village)
            jObj.put("address", offlineData.address)
            jObj.put("wardNo", offlineData.wardNo)
            jObj.put("name", offlineData.name)
            jObj.put("fatherSpouseName", offlineData.fatherName)
            jObj.put("contact", offlineData.contact)
            val topics = mutableListOf<String>()
            if(!offlineData.topic.isNullOrBlank()){
                val topicList = offlineData.topic.split(",").map { it.trim() }
                for (i in 0 until topicList.size) {
                    val text = topicList[i]
                    if (text.isNotEmpty()) {
                        topics.add(text)
                    }
                }
                topics.forEachIndexed { index, value ->
                    jObj.put("topic${index + 1}", value)
                }
            }
            jObj.put("grievanceDetails", offlineData.grievanceMatter)
            jObj.put("agentName", sharedPreferenceClass?.getValue_string("AGENT_NAME"))
            jObj.put("agentRemarks", offlineData.remark)
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        println("params are: $jObj")

        val data: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            resources.getString(com.supragyan.grievancems.R.string.main_url) +
                    resources.getString(com.supragyan.grievancems.R.string.submit_grievance_url),
            jObj,
            Response.Listener<JSONObject> { response: JSONObject ->
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                try {
                    println("list response is $response")
                    grievanceID = response.getString("grievanceId")
                    if(fileList.size>0){
                        syncPreSignedUrl(grievanceID)
                    }
                    //showAlert("Success", "New grievance created successfully")

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError ->
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                println("error $error")
                val response = error.networkResponse

                if ( response != null) {
                    val statusCode = error.networkResponse.statusCode

                    if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 404) {
                        val responseBody = String(error.networkResponse.data, StandardCharsets.UTF_8)
                        val jsonObject = JSONObject(responseBody)
                        val message = jsonObject.optString("message", "Something went wrong! please try after some time")
                        val header = jsonObject.optString("error", "Authentication failed")
                        showAlert(header,message)
                    }else{
                        showAlert("Error","Server Error!!! Please Try After Some Time.")
                    }
                } else {
                    showAlert("Error","Server Error!!! Please Try After Some Time.")
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                val token = sharedPreferenceClass?.getValue_string("TOKEN")   // or wherever you store token
                headers["Authorization"] = "Bearer $token"
                headers["Accept"] = "application/json"

                return headers
            }
        }

        data.retryPolicy = DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().requestQueue.add(data).addMarker(tag)
    }

    private fun showAlert(title:String, message:String) {
        //capture.onPause();
        val alert = AlertDialog.Builder(this@OfflineDetailsActivity)
        alert.setTitle(title)
        alert.setCancelable(false)
        alert.setMessage(message)
        alert.setPositiveButton(
            "Okay",
            { dialog, _ -> //capture.onResume();
                dialog.dismiss()
                if(title == "Success" || title == "No Network"){
                    finish()
                }

            })
        alert.show()
    }

    private fun syncPreSignedUrl(gID: String) {
        if (progressDialog != null) {
            progressDialog!!.show()
        }
        val tag = "pre_url"
        val jObj = JSONObject()
        val filesArray = JSONArray()
        try {
            jObj.put("grievanceId", gID)
            fileList.forEach { uri ->
                val fileName = getFileName(uri)
                val contentType = getMimeTypeFromUri(uri)

                val fileObj = JSONObject()
                //fileObj.put("uploadId", "")
                fileObj.put("fileName", fileName)
                fileObj.put("fileType", getFileType(contentType))
                fileObj.put("contentType", contentType)

                filesArray.put(fileObj)
            }
            jObj.put("files",filesArray)

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        println("params are: $jObj")
        val data: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            resources.getString(com.supragyan.grievancems.R.string.main_url) +
                    resources.getString(com.supragyan.grievancems.R.string.pre_signed_url),
            jObj,
            Response.Listener<JSONObject> { response: JSONObject ->
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                try {
                    println("list response is $response")
                    handlePresignedResponse(response)

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError ->
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                println("error $error")
                val response = error.networkResponse

                if ( response != null) {
                    val statusCode = error.networkResponse.statusCode

                    if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 404) {
                        val responseBody = String(error.networkResponse.data, StandardCharsets.UTF_8)
                        val jsonObject = JSONObject(responseBody)
                        val message = jsonObject.optString("message", "Something went wrong! please try after some time")
                        val header = jsonObject.optString("error", "Authentication failed")
                        showAlert(header,message)
                    }else{
                        showAlert("Error","Server Error!!! Please Try After Some Time.")
                    }
                } else {
                    showAlert("Error","Server Error!!! Please Try After Some Time.")
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                val token = sharedPreferenceClass?.getValue_string("TOKEN")   // or wherever you store token
                headers["Authorization"] = "Bearer $token"
                headers["Accept"] = "application/json"

                return headers
            }
        }

        data.retryPolicy = DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().requestQueue.add(data).addMarker(tag)
    }

    fun getMimeTypeFromUri(uri: Uri): String {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return if (!fileExtension.isNullOrEmpty()) {
            MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(fileExtension.lowercase())
                ?: "application/octet-stream"
        } else {
            "application/octet-stream"
        }
    }

    fun getFileName(uri: Uri): String {
        return if (uri.scheme == "content") {
            var name = "file"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1 && cursor.moveToFirst()) {
                    name = cursor.getString(index)
                }
            }
            name
        } else {
            File(uri.path!!).name
        }
    }
    fun getFileType(mimeType: String): String {
        return if (mimeType.startsWith("image")) "PHOTO" else "DOCUMENT"
    }

    private fun handlePresignedResponse(response: JSONObject) {
        val uploads = response.getJSONArray("uploads")

        var uploadedCount = 0
        val totalFiles = uploads.length()

        for (i in 0 until uploads.length()) {
            val uploadObj = uploads.getJSONObject(i)

            val uploadIDD = uploadObj.getString("uploadId")
            val presignedUrl = uploadObj.getString("presignedUrl")
            val fileNameFromServer = uploadObj.getString("fileName")

            // Match server file with local file
            val fileUri = fileList.firstOrNull { uri ->
                getFileName(uri) == fileNameFromServer
            }

            if (fileUri == null) {
                showAlert("Error", "File not found: $fileNameFromServer")
                return
            }

            val contentType = getMimeTypeFromUri(fileUri)
            val uploadId = uploadObj.getString("fileName")
            uploadIdList.add(uploadId)
            // 🔥 ACTUAL CALL
            uploadFileToS3(
                presignedUrl = presignedUrl,
                fileUri = fileUri,
                contentType = contentType,
                onSuccess = {
                    uploadedCount++
                    syncConfirmAPI(uploadIDD)
                    if (uploadedCount == totalFiles) {
                        if (progressDialog != null) {
                            progressDialog!!.dismiss()
                        }
                        println("✅ All files uploaded successfully")
                        showAlert("Success", "All files uploaded successfully")
                        db.deleteRow(offlineId)
                        // 🔔 Call final API here if needed
                        // submitGrievance()
                    }
                },
                onError = { error ->
                    println("❌ Upload failed: $error")
                    if (progressDialog != null) {
                        progressDialog!!.dismiss()
                    }
                    showAlert("Upload Failed", error)
                }
            )
        }
        println("uploadId $uploadIdList")
    }

    private fun uploadFileToS3(
        presignedUrl: String,
        fileUri: Uri,
        contentType: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val inputStream = contentResolver.openInputStream(fileUri)
            ?: run {
                onError("Unable to open file")
                return
            }

        val fileBytes = inputStream.readBytes()
        inputStream.close()
        if (progressDialog != null) {
            progressDialog!!.show()
        }
        val request = object : Request<NetworkResponse>(
            Method.PUT,
            presignedUrl,
            Response.ErrorListener { error ->
                onError(error.message ?: "Upload failed")
            }
        ) {

            override fun getBody(): ByteArray = fileBytes

            override fun getBodyContentType(): String = contentType

            override fun parseNetworkResponse(response: NetworkResponse): Response<NetworkResponse> {
                return Response.success(
                    response,
                    HttpHeaderParser.parseCacheHeaders(response)
                )
            }

            override fun deliverResponse(response: NetworkResponse) {
                if (response.statusCode == 200 || response.statusCode == 204) {
                    onSuccess()
                } else {
                    onError("Upload failed with code ${response.statusCode}")
                }
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = contentType
                return headers
            }
        }

        request.retryPolicy = DefaultRetryPolicy(
            60000,
            0,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )

        AppController.getInstance().requestQueue.add(request)
    }

    private fun syncConfirmAPI(uploadIDD: String) {
        /*if (progressDialog != null) {
            progressDialog!!.show()
        }*/
        val tag = "user_login"
        val jObj = JSONObject()
        println("params are: $jObj")

        val data: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            resources.getString(com.supragyan.grievancems.R.string.main_url) +
                    resources.getString(com.supragyan.grievancems.R.string.confirm_url) + uploadIDD,
            jObj,
            Response.Listener<JSONObject> { response: JSONObject ->
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                try {
                    println("list response is $response")

                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error: VolleyError ->
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                println("error $error")
                val response = error.networkResponse
                if ( response != null) {
                    val statusCode = error.networkResponse.statusCode
                    if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 404) {
                        val responseBody = String(error.networkResponse.data, StandardCharsets.UTF_8)
                        val jsonObject = JSONObject(responseBody)
                        val message = jsonObject.optString("message", "Something went wrong! please try after some time")
                        val header = jsonObject.optString("error", "Authentication failed")
                        showAlert(header,message)
                    }else{
                        showAlert("Error","Server Error!!! Please Try After Some Time.")
                    }
                } else {
                    showAlert("Error","Server Error!!! Please Try After Some Time.")
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                val token = sharedPreferenceClass?.getValue_string("TOKEN")   // or wherever you store token
                headers["Authorization"] = "Bearer $token"
                headers["Accept"] = "application/json"
                return headers
            }
        }

        data.retryPolicy = DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
        AppController.getInstance().requestQueue.add(data).addMarker(tag)
    }

}