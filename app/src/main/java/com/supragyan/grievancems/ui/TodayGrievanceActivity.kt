package com.supragyan.grievancems.ui

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.supragyan.grievancems.R
import com.supragyan.grievancems.databinding.ActivityTotalSurveysBinding
import com.supragyan.grievancems.ui.OfflineSurveysListActivity.ItemListAdapter
import com.supragyan.grievancems.ui.database.GrievanceModel
import com.supragyan.grievancems.ui.database.SQLiteDB
import com.supragyan.grievancems.utility.SharedPreferenceClass
import org.json.JSONArray
import java.io.File

class TodayGrievanceActivity: AppCompatActivity() {
    private lateinit var binding: ActivityTotalSurveysBinding
    private var progressDialog: ProgressDialog? = null
    var sharedPreferenceClass: SharedPreferenceClass? = null
    private lateinit var db: SQLiteDB
    private var recentActivityArr: JSONArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTotalSurveysBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@TodayGrievanceActivity)
        sharedPreferenceClass = SharedPreferenceClass(this@TodayGrievanceActivity)
        progressDialog!!.setMessage("Loading Please wait")
        progressDialog!!.setCancelable(false)
        db = SQLiteDB(this)
        binding.toolbar.toolbarTitle.text = "TODAY SURVEYS"
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
        binding.recyclerViewD.layoutManager = LinearLayoutManager(this)
        val arrData = intent.getStringExtra("ARRAY_DATA")
        recentActivityArr = JSONArray(arrData)
        println("recentActivityArr $recentActivityArr")

        val adapter = ItemListAdapter(getArrList(recentActivityArr!!), this@TodayGrievanceActivity)
        binding.recyclerViewD.setAdapter(adapter)

    }

    private fun getArrList(jsonArray: JSONArray): ArrayList<GrievanceModel> {
        val list = ArrayList<GrievanceModel>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.optJSONObject(i) ?: continue
            val topicsD = mutableListOf<String>()
            val model = GrievanceModel()
            model.block = obj.optString("block")
            model.gp = obj.optString("gp")
            model.village = obj.optString("villageSahi")
            model.address = obj.optString("address")
            model.wardNo = obj.optString("wardNo")
            model.name = obj.optString("name")
            model.fatherName = obj.optString("fatherSpouseName")
            model.contact = obj.optString("contact")
            if(obj.has("topic1")){
                topicsD.add(obj.optString("topic1"))
            }
            if(obj.has("topic2")){
                topicsD.add(obj.optString("topic2"))
            }
            if(obj.has("topic3")){
                topicsD.add(obj.optString("topic3"))
            }
            if(obj.has("topic4")){
                topicsD.add(obj.optString("topic4"))
            }
            if(obj.has("topic5")){
                topicsD.add(obj.optString("topic5"))
            }
            model.topic = topicsD.joinToString(",")
            model.grievanceMatter = obj.optString("grievanceDetails")
            model.remark = obj.optString("agentRemarks")
            model.grievanceID = obj.optString("grievanceId")
            model.attachments = obj.optJSONArray("attachments")

            list.add(model)
        }

        return list
    }

    private class ItemListAdapter(private val notesList: ArrayList<GrievanceModel>, private val context: Context) : RecyclerView.Adapter<ItemListAdapter.MyViewHolder>() {
        private val activity: TodayGrievanceActivity = context as TodayGrievanceActivity
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
            holder.ivDelete.visibility = View.GONE
            holder.tvSlNo.text = "Grievance ID: " + item.grievanceID
            holder.tvBlock.text = "Block: " + item.block
            holder.tvGP.text = "GP: " + item.gp
            if(item.attachments.length()>0){
                val firstObj = item.attachments.optJSONObject(0)
                val filePath = firstObj?.optString("s3Url")
                val fileType = firstObj?.optString("fileType")
                if (fileType == "IMAGE") {
                    // show actual image
                    Glide.with(holder.itemView.context)
                        .load(filePath)
                        .into(holder.ivPhoto)
                } else {
                    // show document icon
                    holder.ivPhoto.setImageResource(R.drawable.documentation)
                }
            }else{
                holder.ivPhoto.setImageResource(R.drawable.no_image)
            }
            holder.rlLayout.setOnClickListener {
                val intent = Intent(context, TodayGrievanceDetailsActivity::class.java)
                intent.putExtra("BLOCK", item.block)
                intent.putExtra("GP", item.gp)
                intent.putExtra("VILLAGE", item.village)
                intent.putExtra("ADDRESS", item.address)
                intent.putExtra("WARDNO", item.wardNo)
                intent.putExtra("NAME", item.name)
                intent.putExtra("CONTACT", item.contact)
                intent.putExtra("FATHERNAME", item.fatherName)
                intent.putExtra("TOPIC", item.topic)
                intent.putExtra("GMATTER", item.grievanceMatter)
                intent.putExtra("REMARK", item.remark)
                intent.putExtra("PHOTOS", item.attachments.toString())
                context.startActivity(intent)
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
    }
}