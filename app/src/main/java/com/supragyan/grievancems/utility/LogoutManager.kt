package com.supragyan.grievancems.utility

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.supragyan.grievancems.ui.LoginActivity

object LogoutManager {

    private var isLoggingOut = false

    fun callLogoutApi(context: Context, url: String, token: String) {
        if (isLoggingOut) return
        isLoggingOut = true

        val request = object : StringRequest(Method.POST, url,
            Response.Listener {
                println("success logout")
                clearSession(context)
            },
            Response.ErrorListener {
                clearSession(context)
                //println("error")
            }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Authorization"] = "Bearer $token"
                headers["Accept"] = "application/json"

                return headers
            }
        }

        Volley.newRequestQueue(context).add(request)
    }

    private fun clearSession(context: Context) {
        val sharedPreferenceClass = SharedPreferenceClass(context)
        sharedPreferenceClass.clearData()
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)

        isLoggingOut = false
    }
}