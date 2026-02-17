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
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.supragyan.grievancems.R
import com.supragyan.grievancems.databinding.ActivityOfflineDetailsBinding
import com.supragyan.grievancems.databinding.RowPhotosBinding
import com.supragyan.grievancems.databinding.TodayGrievanceDetailsBinding
import com.supragyan.grievancems.ui.OfflineDetailsActivity.ImageAdapter
import com.supragyan.grievancems.ui.OfflineDetailsActivity.TopicAdapter
import com.supragyan.grievancems.ui.database.SQLiteDB
import com.supragyan.grievancems.ui.home.ViewImageActivity
import com.supragyan.grievancems.utility.SharedPreferenceClass
import org.json.JSONArray
import java.io.File

class TodayGrievanceDetailsActivity: AppCompatActivity() {
    private lateinit var binding: TodayGrievanceDetailsBinding
    private var progressDialog: ProgressDialog? = null
    var sharedPreferenceClass: SharedPreferenceClass? = null
    private lateinit var db: SQLiteDB
    var offlineId = ""
    var grievanceID = ""
    val fileList = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = TodayGrievanceDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progressDialog = ProgressDialog(this@TodayGrievanceDetailsActivity)
        sharedPreferenceClass = SharedPreferenceClass(this@TodayGrievanceDetailsActivity)
        progressDialog!!.setMessage("Loading Please wait")
        progressDialog!!.setCancelable(false)
        db = SQLiteDB(this)
        binding.toolbar.toolbarTitle.text = "GRIEVANCE DETAILS"

        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }


        /*intent.putExtra("BLOCK", item.block)
        intent.putExtra("GP", item.gp)
        intent.putExtra("VILLAGE", item.village)
        intent.putExtra("ADDRESS", item.address)
        intent.putExtra("ADDRESS", item.address)
        intent.putExtra("WARDNO", item.wardNo)
        intent.putExtra("NAME", item.name)
        intent.putExtra("FATHERNAME", item.fatherName)
        intent.putExtra("TOPIC", item.topic)
        intent.putExtra("GMATTER", item.grievanceMatter)
        intent.putExtra("REMARK", item.remark)
        intent.putExtra("PHOTOS", item.attachments.toString())*/
        val blockD = intent.getStringExtra("BLOCK")
        if(!blockD.isNullOrBlank()){
            binding.spinnerBlock.text = blockD
        }
        val gpD = intent.getStringExtra("GP")
        if(!gpD.isNullOrBlank()){
            binding.spinnerGP.text = gpD
        }
        val villageD = intent.getStringExtra("VILLAGE")
        if(!villageD.isNullOrBlank()){
            binding.spinnerVS.text = villageD
        }
        val addD = intent.getStringExtra("ADDRESS")
        if(!addD.isNullOrBlank()){
            binding.etAddress.text = addD
        }
        val wardD = intent.getStringExtra("WARDNO")
        if(!wardD.isNullOrBlank()){
            binding.spinnerWN.text = wardD
        }
        val nameD = intent.getStringExtra("NAME")
        if(!nameD.isNullOrBlank()){
            binding.etName.text = nameD
        }
        val fatherD = intent.getStringExtra("FATHERNAME")
        if(!fatherD.isNullOrBlank()){
            binding.etFather.text = fatherD
        }
        val contactD = intent.getStringExtra("CONTACT")
        if(!contactD.isNullOrBlank()){
            binding.etContact.text = contactD
        }
        val topicD = intent.getStringExtra("TOPIC")
        val topicList = topicD!!.split(",").map { it.trim() }
        if(topicList.size>0){
            if(topicList.size == 1){
                binding.llTopic1.visibility = View.VISIBLE
                binding.etTopic1.text = topicList[0]
            }else if(topicList.size == 2){
                binding.llTopic1.visibility = View.VISIBLE
                binding.etTopic1.text = topicList[0]
                binding.llTopic2.visibility = View.VISIBLE
                binding.etTopic2.text = topicList[1]
            }else if(topicList.size == 3){
                binding.llTopic1.visibility = View.VISIBLE
                binding.etTopic1.text = topicList[0]
                binding.llTopic2.visibility = View.VISIBLE
                binding.etTopic2.text = topicList[1]
                binding.llTopic3.visibility = View.VISIBLE
                binding.etTopic3.text = topicList[2]
            }else if(topicList.size == 4){
                binding.llTopic1.visibility = View.VISIBLE
                binding.etTopic1.text = topicList[0]
                binding.llTopic2.visibility = View.VISIBLE
                binding.etTopic2.text = topicList[1]
                binding.llTopic3.visibility = View.VISIBLE
                binding.etTopic3.text = topicList[2]
                binding.llTopic4.visibility = View.VISIBLE
                binding.etTopic4.text = topicList[3]
            }else if(topicList.size == 5){
                binding.llTopic1.visibility = View.VISIBLE
                binding.etTopic1.text = topicList[0]
                binding.llTopic2.visibility = View.VISIBLE
                binding.etTopic2.text = topicList[1]
                binding.llTopic3.visibility = View.VISIBLE
                binding.etTopic3.text = topicList[2]
                binding.llTopic4.visibility = View.VISIBLE
                binding.etTopic4.text = topicList[3]
                binding.llTopic5.visibility = View.VISIBLE
                binding.etTopic5.text = topicList[4]
            }
        }

        val gmD = intent.getStringExtra("GMATTER")
        if(!gmD.isNullOrBlank()){
            binding.etGrievanceMatter.text = gmD
        }
        val remarkD = intent.getStringExtra("REMARK")
        if(!remarkD.isNullOrBlank()){
            binding.etRemark.text = remarkD
        }
        val atD = intent.getStringExtra("PHOTOS")
        if(!atD.isNullOrBlank()){
            val attach = JSONArray(atD)
            if(attach.length()>0){
                val photoList = ArrayList<String>()
                for(i in 0 until attach.length()){
                    val firstObj = attach.optJSONObject(i)
                    val filePath = firstObj?.optString("s3Url")
                    photoList.add(filePath!!)
                }
                binding.recycleViewPhotos.apply {
                    layoutManager = LinearLayoutManager(
                        this@TodayGrievanceDetailsActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    //isNestedScrollingEnabled = false
                    adapter = ImageAdapter(this@TodayGrievanceDetailsActivity,photoList)
                }
            }else{
                binding.ivPhoto.setImageResource(R.drawable.no_image)
            }
        }else{
            binding.ivPhoto.visibility = View.VISIBLE
        }
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
               /* val file = File(images[position])
                holder.binding.imageView.setImageURI(Uri.fromFile(file))*/
                Glide.with(holder.itemView.context)
                    .load(images[position])
                    .into(holder.binding.imageView)
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
}