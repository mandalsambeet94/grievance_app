package com.supragyan.grievancems.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request.Method
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.supragyan.grievancems.GrievanceRepository
import com.supragyan.grievancems.R
import com.supragyan.grievancems.databinding.ActivityTotalSurveysBinding
import com.supragyan.grievancems.ui.database.GrievanceModel
import com.supragyan.grievancems.ui.database.SQLiteDB
import com.supragyan.grievancems.utility.SharedPreferenceClass
import com.supragyan.grievancems.utility.Util
import com.supragyan.grievancems.webservices.AppController
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.coroutines.resume

class OfflineSurveysListActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTotalSurveysBinding
    private var progressDialog: ProgressDialog? = null
    var sharedPreferenceClass: SharedPreferenceClass? = null
    private lateinit var db: SQLiteDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTotalSurveysBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@OfflineSurveysListActivity)
        sharedPreferenceClass = SharedPreferenceClass(this@OfflineSurveysListActivity)
        progressDialog!!.setMessage("Loading Please wait")
        progressDialog!!.setCancelable(false)
        db = SQLiteDB(this)
        binding.toolbar.toolbarTitle.text = "OFFLINE SURVEYS"
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
        binding.recyclerViewD.layoutManager = LinearLayoutManager(this)

        binding.btnSubmit.setOnClickListener{
            if(Util.isNetworkAvailable(this@OfflineSurveysListActivity)){
                val userId = sharedPreferenceClass?.getValue_string("USERID")
                val offlineCount = db.getAllGrievanceData(userId)

                if(offlineCount.size>0){
                    val workRequest = OneTimeWorkRequestBuilder<SyncAllWorker>()
                        .build()

                    WorkManager.getInstance(this)
                        .enqueueUniqueWork(
                            "sync_work",
                            ExistingWorkPolicy.KEEP,
                            workRequest
                        )

                    observeWork(workRequest.id)
                }
            }
        }

    }

    private fun observeWork(workId: UUID) {

        WorkManager.getInstance(this)
            .getWorkInfoByIdLiveData(workId)
            .observe(this) { workInfo ->

                if (workInfo != null) {

                    when (workInfo.state) {

                        WorkInfo.State.RUNNING -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.btnSubmit.isEnabled = false
                            window.setFlags(
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                            )
                        }

                        WorkInfo.State.SUCCEEDED -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSubmit.isEnabled = true
                            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            Toast.makeText(this, "Sync Completed", Toast.LENGTH_SHORT).show()
                            loadOfflineData()// 🔥 Refresh list
                        }

                        WorkInfo.State.FAILED,
                        WorkInfo.State.CANCELLED -> {
                            binding.progressBar.visibility = View.GONE
                            binding.btnSubmit.isEnabled = true
                            Toast.makeText(this, "Sync Failed", Toast.LENGTH_SHORT).show()
                        }

                        else -> {}
                    }
                }
            }
    }


    class SyncAllWorker(
        context: Context,
        workerParams: WorkerParameters
    ) : CoroutineWorker(context, workerParams) {

        override suspend fun doWork(): Result {

            setForeground(createForegroundInfo("Uploading data..."))
            return try {
                // 🔥 Call your API loop here
                val repo = GrievanceRepository(applicationContext)
                val sharedPreferenceClass = SharedPreferenceClass(applicationContext)
                val userId = sharedPreferenceClass.getValue_string("USERID")
                val db = SQLiteDB(applicationContext)
                val offlineList = db.getAllGrievanceData(userId)
                for (item in offlineList) {
                    // 1️⃣ Save grievance
                    val grievanceId = repo.saveGrievance(item)
                        ?: return Result.retry()

                    // 2️⃣ If photos exist
                    if (!item.photos.isNullOrBlank()) {

                        val uris = item.photos.split(",").map { it.toUri() }

                        val uploads = repo.getPresignedUrls(grievanceId, uris)
                            ?: return Result.retry()

                        for (i in 0 until uploads.length()) {

                            val obj = uploads.getJSONObject(i)

                            val presignedUrl = obj.getString("presignedUrl")
                            val uploadId = obj.getString("uploadId")
                            val fileName = obj.getString("fileName")

                            val fileUri = uris.first {
                                File(it.path!!).name == fileName
                            }

                            val uploaded = repo.uploadFileToS3(
                                presignedUrl,
                                fileUri,
                                repo.getMimeType(fileUri)
                            )

                            if (!uploaded) return Result.retry()

                            val confirmed = repo.confirmUpload(uploadId)
                            if (!confirmed) return Result.retry()
                        }
                    }
                    // 3️⃣ Delete row
                    db.deleteRow(item.offlineID)
                }

                showSuccessNotification()

                Result.success()

            } catch (e: Exception) {
                Result.failure()
            }
        }

        private fun createForegroundInfo(progress: String): ForegroundInfo {

            createChannel() // 🔥 VERY IMPORTANT

            val notification = NotificationCompat.Builder(applicationContext, "sync_channel")
                .setContentTitle("Sync in progress")
                .setContentText(progress)
                .setSmallIcon(R.drawable.upload_file)
                .setOngoing(true)
                .build()

            return ForegroundInfo(
                1,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC   // 🔥 IMPORTANT
            )
        }

        private fun createChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    "sync_channel",
                    "Sync Service",
                    NotificationManager.IMPORTANCE_LOW
                )
                channel.description = "Shows sync progress"

                val manager = applicationContext
                    .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

                manager.createNotificationChannel(channel)
            }
        }

        private fun showSuccessNotification() {

            // 🔥 Intent to open your app (MainActivity)
            val intent = Intent(applicationContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(applicationContext, "sync_channel")
                .setContentTitle("Sync Completed")
                .setContentText("All offline data synced successfully")
                .setSmallIcon(R.drawable.check_circle)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)   // 🔥 VERY IMPORTANT
                .build()

            val manager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            manager.notify(2454, notification)
        }
    }

    override fun onResume() {
        super.onResume()
        loadOfflineData()

    }

    private fun loadOfflineData() {

        val userId = sharedPreferenceClass?.getValue_string("USERID")
        val offlineCount = db.getAllGrievanceData(userId)

        if (offlineCount.size > 0) {
            val adapter = ItemListAdapter(offlineCount, this)
            binding.recyclerViewD.adapter = adapter
            binding.btnSubmit.visibility = View.VISIBLE
            binding.tvNoData.visibility = View.GONE
        } else {
            binding.recyclerViewD.adapter = null
            binding.tvNoData.visibility = View.VISIBLE
            binding.btnSubmit.visibility = View.GONE
        }
    }

    private class ItemListAdapter(private val notesList: ArrayList<GrievanceModel>, private val context: Context) : RecyclerView.Adapter<ItemListAdapter.MyViewHolder>() {
        private val activity: OfflineSurveysListActivity = context as OfflineSurveysListActivity
        inner class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val ivPhoto: ImageView = view.findViewById(R.id.ivPhoto)
            val tvBlock: TextView = view.findViewById(R.id.tvBlock)
            val tvSlNo: TextView = view.findViewById(R.id.tvSlNo)
            val tvGP: TextView = view.findViewById(R.id.tvGP)
            val rlLayout: RelativeLayout = view.findViewById(R.id.rlLayout)
            val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
        }

        fun getItems(): List<GrievanceModel> {
            return notesList
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.offline_survey_list_row, parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val item = notesList[position]
            val num = position + 1
            holder.tvSlNo.text = "Sl No: $num"
            holder.tvBlock.text = "Block: " + item.block
            holder.tvGP.text = "GP: " + item.gp
            if(!item.photos.isNullOrBlank()){
                val firstPath = item.photos.split(",").firstOrNull()
                println("firstPath $firstPath")
                if (isImageFile(firstPath!!)) {
                    // show actual image
                    val file = File(firstPath)
                    holder.ivPhoto.setImageURI(Uri.fromFile(file))
                } else {
                    // show document icon
                    holder.ivPhoto.setImageResource(R.drawable.documentation)
                }
            }else{
                holder.ivPhoto.setImageResource(R.drawable.no_image)
            }
            holder.rlLayout.setOnClickListener {
                val intent = Intent(context, OfflineDetailsActivity::class.java)
                intent.putExtra("IDD", item.offlineID)
                context.startActivity(intent)
            }
            holder.ivDelete.setOnClickListener {
                showAlert(item.offlineID, position)
                //activity.db.deleteRow(item.offlineID)
            }
        }

        override fun getItemCount(): Int = notesList.size

        private fun isImageFile(path: String): Boolean {
            val file = File(path)
            val extension = MimeTypeMap.getFileExtensionFromUrl(file.absolutePath)
                .lowercase()

            val mimeType = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(extension)

            return mimeType?.startsWith("image/") == true
        }

        private fun showAlert(offId:String, position:Int) {
            //capture.onPause();
            val alert = AlertDialog.Builder(context)
            alert.setTitle("DELETE")
            alert.setCancelable(false)
            alert.setMessage("Are you sure you want to delete offline grievance survey?")
            alert.setPositiveButton(
                "Yes",
                { dialog, _ -> //capture.onResume();
                    dialog.dismiss()
                    activity.db.deleteRow(offId)
                    notesList.removeAt(position)

                    // Notify adapter properly
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, notesList.size)

                    if (notesList.isEmpty()) {
                        activity.binding.tvNoData.visibility = View.VISIBLE
                    }

                })
            alert.setNegativeButton("No",{dialog, which ->
                dialog.dismiss()
            })
            alert.show()
        }
    }

    suspend fun saveGrievanceSuspend(
        context: Context,
        offlineData: GrievanceModel
    ): String? = suspendCancellableCoroutine { continuation ->
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
                try {
                    println("list response is $response")
                    val grievanceID = response.getString("grievanceId")
                    continuation.resume(grievanceID)
                    /*if(fileList.size>0){
                        syncPreSignedUrl(grievanceID)
                    }*/
                    //showAlert("Success", "New grievance created successfully")

                } catch (e: JSONException) {
                    e.printStackTrace()
                    continuation.resume(null)
                }
            },
            Response.ErrorListener { error: VolleyError ->

                println("error $error")
                val response = error.networkResponse

                if ( response != null) {
                    val statusCode = error.networkResponse.statusCode

                    if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 404) {
                        val responseBody = String(error.networkResponse.data, StandardCharsets.UTF_8)
                        val jsonObject = JSONObject(responseBody)
                        val message = jsonObject.optString("message", "Something went wrong! please try after some time")
                        val header = jsonObject.optString("error", "Authentication failed")
                        //showAlert(header,message)
                        continuation.resume(null)
                    }else{
                        println("error")
                        continuation.resume(null)
                        //showAlert("Error","Server Error!!! Please Try After Some Time.")
                    }
                } else {
                    println("error")
                    continuation.resume(null)
                    //showAlert("Error","Server Error!!! Please Try After Some Time.")
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