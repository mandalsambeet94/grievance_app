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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.supragyan.grievancems.R
import com.supragyan.grievancems.databinding.ActivityTotalSurveysBinding
import com.supragyan.grievancems.ui.database.GrievanceModel
import com.supragyan.grievancems.ui.database.SQLiteDB
import com.supragyan.grievancems.utility.SharedPreferenceClass
import java.io.File

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

    }

    override fun onResume() {
        super.onResume()
        val userId = sharedPreferenceClass?.getValue_string("USERID")
        val offlineCount = db.getAllGrievanceData(userId)

        if(offlineCount.size>0){
            val adapter = ItemListAdapter(offlineCount, this@OfflineSurveysListActivity)
            binding.recyclerViewD.setAdapter(adapter)
        }else{
            binding.tvNoData.visibility = View.VISIBLE
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



}