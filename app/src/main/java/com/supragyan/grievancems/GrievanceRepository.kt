package com.supragyan.grievancems

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.android.volley.DefaultRetryPolicy
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.supragyan.grievancems.ui.database.GrievanceModel
import com.supragyan.grievancems.utility.SharedPreferenceClass
import com.supragyan.grievancems.webservices.AppController
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import kotlin.coroutines.resume

class GrievanceRepository(private val context: Context) {
    private val sharedPref = SharedPreferenceClass(context)
    private val requestQueue = AppController.getInstance().requestQueue

    // =========================================================
    // 1️⃣ SAVE GRIEVANCE
    // =========================================================
    suspend fun saveGrievance(data: GrievanceModel): String? =
        suspendCancellableCoroutine { continuation ->

            val json = JSONObject()

            try {
                json.put("block", data.block)
                json.put("gp", data.gp)
                json.put("villageSahi", data.village)
                json.put("address", data.address)
                json.put("wardNo", data.wardNo)
                json.put("name", data.name)
                json.put("fatherSpouseName", data.fatherName)
                json.put("contact", data.contact)
                json.put("grievanceDetails", data.grievanceMatter)
                json.put("agentName", sharedPref.getValue_string("AGENT_NAME"))
                json.put("agentRemarks", data.remark)
            } catch (e: Exception) {
                continuation.resume(null)
            }

            val request = object : JsonObjectRequest(
                Method.POST,
                context.getString(R.string.main_url) +
                        context.getString(R.string.submit_grievance_url),
                json,
                { response ->
                    continuation.resume(response.optString("grievanceId"))
                },
                { continuation.resume(null) }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    return hashMapOf(
                        "Authorization" to "Bearer ${sharedPref.getValue_string("TOKEN")}",
                        "Accept" to "application/json"
                    )
                }
            }

            requestQueue.add(request)
        }

    // =========================================================
    // 2️⃣ GET PRESIGNED URL
    // =========================================================
    suspend fun getPresignedUrls(
        grievanceId: String,
        fileUris: List<Uri>
    ): JSONArray? = suspendCancellableCoroutine { continuation ->

        val json = JSONObject()
        val filesArray = JSONArray()

        try {
            json.put("grievanceId", grievanceId)

            fileUris.forEach { uri ->
                val fileObj = JSONObject()
                fileObj.put("fileName", getFileName(uri))
                fileObj.put("fileType", getFileType(getMimeType(uri)))
                fileObj.put("contentType", getMimeType(uri))
                filesArray.put(fileObj)
            }

            json.put("files", filesArray)

        } catch (e: Exception) {
            continuation.resume(null)
        }

        val request = object : JsonObjectRequest(
            Method.POST,
            context.getString(R.string.main_url) +
                    context.getString(R.string.pre_signed_url),
            json,
            { response ->
                continuation.resume(response.getJSONArray("uploads"))
            },
            { continuation.resume(null) }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                return hashMapOf(
                    "Authorization" to "Bearer ${sharedPref.getValue_string("TOKEN")}",
                    "Accept" to "application/json"
                )
            }
        }

        requestQueue.add(request)
    }

    // =========================================================
    // 3️⃣ UPLOAD FILE TO S3
    // =========================================================
    suspend fun uploadFileToS3(
        presignedUrl: String,
        fileUri: Uri,
        contentType: String
    ): Boolean = suspendCancellableCoroutine { continuation ->

        try {
            val file = File(fileUri.path!!)
            val bytes = file.readBytes()

            val request = object : com.android.volley.toolbox.StringRequest(
                Method.PUT,
                presignedUrl,
                { continuation.resume(true) },
                { continuation.resume(false) }
            ) {
                override fun getBody(): ByteArray {
                    return bytes
                }

                override fun getBodyContentType(): String {
                    return contentType
                }
            }

            requestQueue.add(request)

        } catch (e: Exception) {
            continuation.resume(false)
        }
    }

    // =========================================================
    // 4️⃣ CONFIRM API
    // =========================================================
    suspend fun confirmUpload(uploadId: String): Boolean =
        suspendCancellableCoroutine { continuation ->

            val json = JSONObject()
            json.put("uploadId", uploadId)

            val request = object : JsonObjectRequest(
                Method.POST,
                context.getString(R.string.main_url) +
                        context.getString(R.string.confirm_url),
                json,
                { continuation.resume(true) },
                { continuation.resume(false) }
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    return hashMapOf(
                        "Authorization" to "Bearer ${sharedPref.getValue_string("TOKEN")}",
                        "Accept" to "application/json"
                    )
                }
            }

            requestQueue.add(request)
        }

    // =========================================================
    // Helpers
    // =========================================================
     fun getFileName(uri: Uri): String =
        File(uri.path!!).name

     fun getMimeType(uri: Uri): String {
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        return MimeTypeMap.getSingleton()
            .getMimeTypeFromExtension(extension.lowercase())
            ?: "application/octet-stream"
    }

     fun getFileType(type: String): String =
        if (type.startsWith("image")) "IMAGE" else "DOCUMENT"
}