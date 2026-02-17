package com.supragyan.grievancems.ui

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.supragyan.grievancems.R
import com.supragyan.grievancems.databinding.ActivityLoginBinding
import com.supragyan.grievancems.utility.SharedPreferenceClass
import com.supragyan.grievancems.utility.Util
import com.supragyan.grievancems.webservices.AppController
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets



class LoginActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    var i = 0
    private var progressDialog: ProgressDialog? = null
    var sharedPreferenceClass: SharedPreferenceClass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog =  ProgressDialog(this@LoginActivity)
        sharedPreferenceClass= SharedPreferenceClass(this@LoginActivity)
        progressDialog!!.setMessage("Loading Please wait")
        progressDialog!!.setCancelable(false)
        binding.btnLogin.setOnClickListener {
            if (Util.validateFields(binding.etMobile) &&
                Util.validateFields(binding.etPassword) && Util.isNetworkAvailable(this)) {
                validateLogin()
            }

        }
    }

    private fun validateLogin() {
        if (progressDialog != null) {
            progressDialog!!.show()
        }
        val tag = "user_login"
        val jObj = JSONObject()
        try {
            jObj.put("name", binding.etMobile.text.toString().trim())
            jObj.put("password", binding.etPassword.text.toString().trim())
            jObj.put("role", "AGENT")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        println("params are: $jObj")

        val data: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,
            resources.getString(R.string.main_url) +
                    resources.getString(R.string.login_url),
            jObj,
            Response.Listener<JSONObject> { response: JSONObject ->
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                try {
                    println("list response is $response")
                    sharedPreferenceClass?.setValue_boolean("ISLOGIN", true)
                    sharedPreferenceClass?.setValue_string("TOKEN",response.getString("token"))
                    sharedPreferenceClass?.setValue_string("USERID",response.getString("userId"))
                    sharedPreferenceClass?.setValue_string("NAME",response.getString("name"))
                    sharedPreferenceClass?.setValue_string("CONTACT",response.getString("contact"))
                    sharedPreferenceClass?.setValue_string("ROLE",response.getString("role"))
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
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
                    /*Toast.makeText(
                        this@LoginActivity,
                        "Server Error!!! Please Try After Some Time.",
                        Toast.LENGTH_LONG
                    ).show()*/
                    showAlert("Error","Server Error!!! Please Try After Some Time.")
                }
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params1: Map<String, String> = HashMap()
                // Add custom headers if needed
                println("header param: $params1")
                return params1
            }
        }

        data.setRetryPolicy(
            DefaultRetryPolicy(
                45000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )
        AppController.getInstance().requestQueue.add(data).addMarker(tag)
    }

    private fun showAlert(title:String, message:String) {
        //capture.onPause();
        val alert = AlertDialog.Builder(this@LoginActivity)
        alert.setTitle(title)
        alert.setCancelable(false)
        alert.setMessage(message)
        alert.setPositiveButton(
            "Okay",
            { dialog, _ -> //capture.onResume();
                dialog.dismiss()
            })
        alert.show()
    }

}