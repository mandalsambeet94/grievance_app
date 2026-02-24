package com.supragyan.grievancems.ui.home

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.supragyan.grievancems.R
import com.supragyan.grievancems.ui.TodayGrievanceActivity
import com.supragyan.grievancems.databinding.FragmentHomeBinding
import com.supragyan.grievancems.ui.CreateGrievanceActivity
import com.supragyan.grievancems.ui.MainActivity
import com.supragyan.grievancems.ui.OfflineSurveysListActivity
import com.supragyan.grievancems.ui.database.SQLiteDB
import com.supragyan.grievancems.utility.SharedPreferenceClass
import com.supragyan.grievancems.utility.Util
import com.supragyan.grievancems.webservices.AppController
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.nio.charset.StandardCharsets

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    var i = 0
    private var recentActivityArr: JSONArray? = null
    private lateinit var db: SQLiteDB
    private var progressDialog: ProgressDialog? = null
    var sharedPreferenceClass: SharedPreferenceClass? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        /*val homeViewModel =
            ViewModelProvider(this)[HomeViewModel::class.java]*/

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        progressDialog =  ProgressDialog(requireActivity())
        sharedPreferenceClass= SharedPreferenceClass(requireActivity())
        progressDialog!!.setMessage("Loading Please wait")
        progressDialog!!.setCancelable(false)
        db = SQLiteDB(requireActivity())
       /* val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/


        binding.llOffline.setOnClickListener {
            val userId = sharedPreferenceClass?.getValue_string("USERID")
            println("userId $userId")
            val offlineCount = db.getAllGrievanceData(userId)
            if(offlineCount.isNotEmpty()){
                val intent = Intent(requireActivity(), OfflineSurveysListActivity::class.java)
                startActivity(intent)
            }
        }

        binding.llSurveyToday.setOnClickListener {
            if(Util.isNetworkAvailable(requireActivity())){
                val intent = Intent(requireActivity(), TodayGrievanceActivity::class.java)
                intent.putExtra("ARRAY_DATA", recentActivityArr.toString())
                startActivity(intent)
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        if(Util.isNetworkAvailable(requireActivity())){
            getDashboardData()
        }
        val userId = sharedPreferenceClass?.getValue_string("USERID")

        val offlineCount = db.getAllGrievanceData(userId)
        binding.tvTotalCountOffline.text = offlineCount.size.toString()
    }

    private fun getDashboardData() {
        if (progressDialog != null) {
            progressDialog!!.show()
        }
        val tag = "user_login"
        val jObj = JSONObject()
        println("params are: $jObj")

        val data: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET,
            resources.getString(R.string.main_url) +
                    resources.getString(R.string.dashboard_url),
            jObj,
            Response.Listener<JSONObject> { response: JSONObject ->
                if (progressDialog != null) {
                    progressDialog!!.dismiss()
                }
                try {
                    println("list response is $response")
                    val total = response.getString("totalGrievances")
                    if(!total.isNullOrBlank()){
                        binding.tvTotalCount.text = total
                    }
                    recentActivityArr = response.optJSONArray("recentActivity")
                    if (recentActivityArr != null) {
                        if(recentActivityArr!!.length()>0){
                            binding.tvTodayCount.text = recentActivityArr!!.length().toString()
                        }
                    }
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
                println("response $response")
                if ( response != null) {
                    val statusCode = error.networkResponse.statusCode
                    println("statusCode $statusCode")
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
        val alert = AlertDialog.Builder(requireActivity())
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