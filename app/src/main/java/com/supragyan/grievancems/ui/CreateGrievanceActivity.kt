package com.supragyan.grievancems.ui

import android.R
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isNotEmpty
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.AuthFailureError
import com.android.volley.DefaultRetryPolicy
import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.JsonObjectRequest
import com.google.android.material.textfield.TextInputEditText
import com.supragyan.grievancems.databinding.ActivityCreateGrievanceBinding
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
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID

class CreateGrievanceActivity: AppCompatActivity() {
    private lateinit var binding: ActivityCreateGrievanceBinding
    var i = 0
    var topicCount: Int = 1
    private val maxTopics = 4
    var blockData = ""
    var gpData = ""
    var villageData = ""
    var wardData = ""
    var grievanceID = ""
    var uploadID = ""
    private var progressDialog: ProgressDialog? = null
    var sharedPreferenceClass: SharedPreferenceClass? = null
    val fileList = mutableListOf<Uri>()
    val uploadIdList = mutableListOf<String>()
    private lateinit var imageAdapter: ImageAdapter
    private lateinit var cameraImageUri: Uri
    val topics = mutableListOf<String>()
    private lateinit var db: SQLiteDB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateGrievanceBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setSoftInputMode(
            android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        progressDialog = ProgressDialog(this@CreateGrievanceActivity)
        sharedPreferenceClass = SharedPreferenceClass(this@CreateGrievanceActivity)
        progressDialog!!.setMessage("Loading Please wait")
        progressDialog!!.setCancelable(false)
        db = SQLiteDB(this)
        binding.toolbar.toolbarTitle.text = "NEW GRIEVANCE"
        binding.toolbar.ivBack.setOnClickListener {
            finish()
        }
        val grievanceTypes = listOf("ATHGARH", "TIGIRIA", "Tangi Chowdwar", "Athagarh NAC")
        val wordNoTypes = listOf("","1", "2", "3", "4","5", "6", "7", "8","9", "10", "11", "12","13", "14", "15", "16","17", "18", "19", "20","21", "22", "23", "24","25", "26", "27", "28","29", "30")
        val wordNoTypesNac = listOf("","1", "2", "3", "4","5", "6", "7", "8","9", "10", "11", "12","13", "14", "15", "16","17", "18")
        val gpTypeMap = mapOf(
            "ATHGARH" to listOf("Dhaipur", "Radhagovindpur", "Samsarpur", "Sathilo","Ichhapur","Anantpur","Dhurusia","Radhakishorepur","Kumarpur","Joranda","Rajanagar","Bentapada","Tarading","Kuspangi","Oranda","Mancheswar","Kandarpur","Mahakalbasta","Ghantikhal","Dorada","Bhogara","Kulailo","Megha","Katakiasahi","Badabhuin","Jenapadadesh","Khuntakata","Radhakrushnapur","Kandarei","Khuntuni","Dalabhaga","Gurudijhatia","Gobara","Chhagaon"),
            "TIGIRIA" to listOf("Achalakot", "Badanauput","Viruda","Baliput","Hatamal","Gadadharpur","Somapada","Bhogoda","Panchagaon","Nuapatana","Puruna Tigiria","Jamadeipur","Bindhanima","Nizigarh"),
            "Tangi Chowdwar" to listOf("Kakhadi","Shankarpur","Mahisalanda","Mangarajpur","Badasamntrapur","Brahmapur"),
            "Athagarh NAC" to listOf("")
        ).mapValues { (_, value) ->
            value.sortedBy { it.trim().lowercase() }}

        val villagesNacMap = mapOf(
            "1" to listOf ("Hemamalapur",	"Guhalapadia",	"Samantasahi"),
            "2" to listOf ("Talasahi",	"Tanlasahi"	),
            "3" to listOf ("Gadashi",	"Dhobasahi",	"Pathanasahi"),
            "4" to listOf ("Keutasahi",	"Muslim basti",	"Damasahi",	"Hadisahi",	"Pana sahi",	"Jharana chakka part",	"Adimata colony"	),
            "5" to listOf ("Rasarashikpur"							),
            "6" to listOf ("Sasana",	"Block colony",	"Ghoda sasana",	"Old bustand part"				),
            "7" to listOf ("Upparsahi",	"Kalubasti",	"Uttarachandisahi",	"Police colony",	"Tanlasahi"			),
            "8" to listOf ("Bautisahi",	"Tanlasahi",	"Puruna Busstand"				),
            "9" to listOf ("Birakishorepur"),
            "10" to listOf ("Bagetisahi",	"Harisaranapur",	"Satichourasahi"					),
            "11" to listOf ("Gaudasahi",	"Bramhana sasana",	"Hadisahi",	"Badheitota",	"Gudiasahi"			),
            "12" to listOf ("Jagannath sahi",	"Telisahi",	"Bhagabatasahi",	"Doulamandapsahi"				),
            "13" to listOf ("Panasahi",	"Keutasahi",	"Sadarsahi",	"Doulamandapsahi"				),
            "14" to listOf ("Jharana chaka part",	"Sabar sahi",	"Medical colony",	"Housing board"				),
            "15" to listOf ("Banikanthanagar",	"Gandhi marg"					),
            "16" to listOf ("Kangada sahi",	"Maitri nagar"						),
            "17" to listOf ("Hatasahi",	"Kantol bazar",	"Bazarsahi",	"Keutasahi",	"Sabarasahi",	"Matiasahi",	"Chandiroad sahi",	"Satichourasahi"),
            "18" to listOf ("Ashok nagar",	"Rajabati nagar"						)
        ).mapValues { (_, value) ->
            value
                .map { it.trim() }
                .sortedBy { it.lowercase() }
        }

        val villageTypeMap = mapOf("Dhaipur"  to listOf (
            "Dhaipur ",
            "Gadadhapur ",
            "Somanathpur ",
            "Chatabara ",
            "Radhaprasannapur "),

            "Radhagovindpur"  to listOf (
                "Radhamadhabpur ",
                "Gobindapur ",
                "Radhagobindpur ",
                "Radhaballavpur ",
                "Laxminarayanpur ",
                "Sudhansumohanpur ",
                "Haripriyapur ",
                "Radhakesabpur ",
            ),
            "Samsarpur" to listOf (
                "Samsarpur ",
                "Amrutamanohipatna ",
                "Ankula ",
                "Budhiapatna ",
                "Jagannathpur ",
                "Radhapatipur "

            ),

            "Sathilo"  to listOf (
                "Sathilo ",
                "Talabasta ",
                "Koranga ",
                "Kuanla ",
            ),
            "Ichhapur" to listOf (
                "Ichhapur ",
                "Gudupada ",
                "Patanda ",
                "Arakhapatna ",
                "Muktadeipur "
            ),

            "Anantpur" to listOf (
                "Anantpur ",
                "Nuasasan ",
                "Berhmapur ",
                "Srirangpur "
            ),
            "Dhurusia" to listOf (

                "Dhurusia ",
                "Bhuinbarei ",
                "Jagmohanpur ",
                "Keutapada ",
                "Jenapur ",
                "Gopalprasad ",
                "Baudhapur "
            ),

            "Radhakishorepur"  to listOf (
                "Radhakishorepur ",
                "Radhadamodarpur ",
                "Belda ",
                "Machhyapur ",
                "Purusottampur ",
                "Khamarnuagaon ",
            ),
            "Kumarpur" to listOf (
                "Kumarpur ",
                "Balipur ",
                "Kantania ",
                "Santanibati ",
                "Dampatipur "

            ),
            "Joranda" to listOf (

                "Joranda ",
                "Karikol ",
                "Indipur ",
                "Paikarapur ",
                "Naurath ",
                "Haridagahira "

            ),
            "Rajanagar" to listOf (

                "Rajanagar ",
                "Tailamal ",
                "Gariapat ",
                "Betakholi ",
                "Banakhandi "

            ),
            "Bentapada" to listOf (
                "Bentapada ",
                "Radhakantapatna ",
                "Kulagada ",
                "Kendutokoli sabar sahi ",
                "Manapur ",
                "Dhanurjaypur ",
                "Balipada "
            ),
            "Tarading" to listOf (

                "Tarading ",
                "Gajaamba ",
                "Tentulia ",
                "Bailo ",
                "Gourangpur "

            ),
            "Kuspangi" to listOf (

                "Kuspangi ",
                "Pahilabaar ",
                "Jemadeipur ",
                "Sarkoli ",
                "Baula "
            ),
            "Oranda" to listOf (
                "Oranda ",
                "Bali ",
                "Bali Sasan ",
                "Radhapriapur Sasan ",
                "Sitarampur ",
                "Kapursingh "

            ),
            "Mancheswar" to listOf (
                "Mancheswar ",
                "Paikarapur ",
                "Suniamuhan ",
                "Kalankipur ",
                "Lingapada ",
                "Prasannapur ",
                "Nuadiha ",
                "Narangabasta ",
                "Brahmanbasta ",
                "Bamanpur ",
                "Bishnupur "

            ),
            "Kandarpur" to listOf (

                "Kandarpur ",
                "Nandailo ",
                "Routrapur "

            ),
            "Mahakalbasta" to listOf (

                "Mahakalbasta ",
                "Madhabpur ",
                "Dahisara ",
                "Champapur ",
                "Radhadhabapur ",
                "Parsurampur ",
                "Haripur "

            ),
            "Ghantikhal" to listOf (
                "Ghantikhal ",
                "Nidhipur ",
                "Radhashyampur ",
                "Ramshyampur ",
                "Chandrabalishyampur "

            ),
            "Dorada" to listOf (

                "Dorada ",
                "Iswara ",
                "Chunapada ",
                "Gopinathpur ",
                "Udayapurdesh ",
                "Tarsing ",
                "Gundichapur "
            ),
            "Bhogara" to listOf (
                "Bhogara ",
                "Mathurapur ",
                "Totapada ",
                "Radhasaranpur "

            ),
            "Kulailo" to listOf (
                "Kulailo ",
                "Udayapurdal ",
                "Karakamal ",
                "Radhakrushnapur ",
                "Talagarh ",
                "Patalinga ",
                "Silapata ",
                "Birijinga "
            ),
            "Megha" to listOf (
                "Megha ",
                "Dhurukudia ",
                "Daspur ",
                "Baghera "
            ),
            "Katakiasahi" to listOf (
                "Katakiasahi ",
                "Boulpada ",
                "Bounsdanda ",
                "Bandhahata ",
                "Nuagada ",
                "Balarampur ",
                "Petenigaon "
            ),
            "Badabhuin" to listOf (
                "Badabhuindesh ",
                "Badabhindala ",
                "Gopiballavpur ",
                "Raghunathpurpatna ",
                "Jagiapada "
            ),
            "Jenapadadesh" to listOf (
                "Jenapadadesh ",
                "Jenapadadal ",
                "Bhagirathipur ",
                "Kalaragada ",
                "Matikote ",
                "JagannathBallavpurSasan "
            ),
            "Khuntakata" to listOf (
                "Khuntakata ",
                "Regedapada ",
                "Nuabandha ",
                "Mahidhapur ",
                "Radhamohnpur ",
                "Radhadarsanpur "
            ),
            "Radhakrushnapur" to listOf (
                "Radhakrushnapur ",
                "Rahangol ",
                "Khanduali ",
                "Saraswatipur "
            ),
            "Kandarei"  to listOf (
                "Kandarei ",
                "Kansar ",
                "Mahalaxmipur ",
                "Manitiri ",
                "Sabitripur ",
                "Dalua "
            ),
            "Khuntuni" to listOf (
                "Khuntuni ",
                "Rampei ",
                "Krushnashyampur ",
                "Nursinghpur ",
                "Radharamanpur "
            ),
            "Dalabhaga" to listOf (
                "Dalabhaga ",
                "Champia ",
                "Chhenakhianuagaon ",

                ),
            "Gurudijhatia" to listOf (
                "Gurudijhatia ",
                "Chhotiamba ",
                "Kolathapangi ",
                "Kotar ",
                "Pithakhia ",
                "Sauria "
            ),
            "Gobara" to listOf (
                "Gobara ",
                "Kanthapur ",
                "Kadua Nuagaon ",
                "Gobara Sasan ",
                "Rajaballavpur "

            ),
            "Chhagaon" to listOf (
                "Chhagaon ",
                "Manitiri ",
                "Parbatia ",
                "Charabhaunri ",
                "Sasanga ",
                "Baghua ",
                "Sarakuan "
            ),

            "Achalakot" to listOf(
                "Badasahi ",
                "Mundiasahi ",
                "Sabarsahi ",
                "Maalisahi ",
                "Nuasahi ",
                "Sanapatana ",
                "Badapatna ",
                "Haridapasi "
            ),
            "Badanauput" to listOf(
                "Badanauput ",
                "Salijanga ",
                "Baulanga ",
                "Khandahata "
            ),
            "Viruda"  to listOf(
                "Uppargada ",
                "Talagada ",
                "Godijharia ",
                "Balipatana ",
                "Panasapatana ",
                "Manimagapatna ",
                "Viruda "

            ),
            "Baliput" to listOf(
                "Baliput ",
                "Biriput ",
                "Baneswarpada ",
                "Godorabandha "
            ),
            "Hatamal" to listOf(
                "Hatamal ",
                "Nandapur ",
                "Sunthipal ",
                "Saanpur ",
                "Bishnupur "
            ),
            "Gadadharpur" to listOf(
                "Gadadharpur ",
                "Harisaranapur ",
                "Koilikanya ",
                "Manapur ",
                "Kalibiri ",
                "Raghurampur ",
                "Kanthipur ",
                "Pakhapada ",
                "Sananauput "
            ),
            "Somapada" to listOf(
                "Somapada ",
                "Tiarasahi ",
                "Baharabila ",
                "Mahuladhipi ",
                "Kumbhiput "
            ),
            "Bhogoda" to listOf(
                "Bhogoda ",
                "Belanta ",
                "Gokhanakhala ",
                "Marichia ",
                "khajuripada ",
                "Grid sahi ",
                "Paikasahi ",
                "Vejia ",

                ),
            "Panchagaon" to listOf(
                "Panchagaon ",
                "Kadamasahi ",
                "Seshagaon ",
                "Tentuliragadi ",
                "Popara ",
                "Sudarsanpur "

            ),
            "Nuapatana" to listOf(
                "Gahamarasahi ",
                " Majhi sahi ",
                " Bada sahi ",
                "Tala sahi ",
                "Nalikanti sahi ",
                "Chasa sahi ",
                "Sadaksahi ",
                "Kalapatasahi ",
                "Surendrapatana ",
                "Telisahi ",
                "Golakhpatna sahi ",
                "Hariballav sahi ",
                "Sabar sahi ",
                "Bidyanagari ",
                "Kansari sahi ",
                "Bali sahi ",
                "Phalikia sahi ",
                "Harijana sahi ",
                "Mundiasahi "

            ),
            "Puruna Tigiria" to listOf(
                "Puruna Tigiria ",
                "Paikianra ",
                "Amuniasahi ",
                "Jatiani sabarsahi ",
                "Chasanhara "
            ),
            "Jamadeipur" to listOf(
                "Jemdeipur ",
                "Pankala ",
                "Basudevpur ",
                "Banamalipur ",
                "Goutampur ",
                "Godisahi ",
                "Chinapatana "

            ),
            "Bindhanima" to listOf(
                "Bindhanima ",
                "Sethasahi ",
                "Hatasahi ",
                "Damasahi "


            ),
            "Nizigarh" to listOf(
                "Nizigarh ",
                "Karadapali ",
                " Kadalibadi sabar sahi ",
                "Gopinathpur Sasana ",
            ),
            "Kakhadi" to listOf  (
                "Bidyadharpur" ,
                "Kakhadi" ,
                "Mahalapada" ,
                "Kaptabarei" ,
                "Gopinathpada"
            ),

            "Shankarpur" to listOf  (
                "Mathasahi" ,
                "Majhisahi" ,
                "Benguniasahi" ,
                "Amarabatipur"
            ),

            "Mahisalanda" to listOf  (
                "Mahisalanda" ,
                "Machapangi" ,
                "Ambilajhari" ,
                "Banto" ,
                "Gahanda" ,
                "Dudhianali" ,

                ),

            "Mangarajpur" to listOf  (
                "Mangarajpur" ,
                "Bagdhara" ,
                "Ramchandrapur" ,
                "Patapolasahi" ,
                "Kochilapada" ,
                "Kochilanugaon" ,
                "Berena"
            ),

            "Badasamntrapur" to listOf  (
                "Badasamntarapur" ,
                "Sardar kharida" ,
                "Badapadagaon" ,
                "Dianipathena"
            ),

            "Brahmapur" to listOf  (
                "Brahmapur" ,
                "Belda" ,
                "Kamanga" ,
                "Kanjia" ,
                "Jamadeipur"

            )) .mapValues { (_, value) ->
            value
                .map { it.trim() }   // remove trailing spaces
                .sortedBy { it.lowercase() }
        }


        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, grievanceTypes)
        binding.spinnerBlock.setAdapter(adapter)

        val adapterWard = ArrayAdapter(this, R.layout.simple_list_item_1, wordNoTypes)
        binding.spinnerWN.setAdapter(adapterWard)

        binding.spinnerBlock.setOnClickListener {
            binding.spinnerBlock.showDropDown()
        }
        binding.spinnerWN.setOnClickListener {
            binding.spinnerWN.showDropDown()
        }
        binding.spinnerWN.setOnItemClickListener { parent, view, position, id ->
            wardData = parent.getItemAtPosition(position).toString()
            println("spinnerWN $wardData")
        }
        binding.spinnerBlock.setOnItemClickListener { parent, view, position, id ->
            blockData = parent.getItemAtPosition(position).toString()
            println("spinnerBlock $blockData")
            binding.tilGrievanceType.error = null
            binding.spinnerGP.setText("", false)
            if (blockData == "Athagarh NAC") {
                // Disable subtype completely
                //binding.titleGP.visibility = View.GONE
                binding.llNac.visibility = View.VISIBLE
                binding.llCommon.visibility = View.GONE
                binding.spinnerGP.isEnabled = false
                val adapterWard = ArrayAdapter(this, R.layout.simple_list_item_1, wordNoTypesNac)
                binding.spinnerWNNac.setAdapter(adapterWard)
                /*val adapterNac = ArrayAdapter(this, R.layout.simple_list_item_1, villagesNac)
                binding.spinnerVS.setAdapter(adapterNac)*/
            }else{
                // Load related values
                binding.llNac.visibility = View.GONE
                binding.llCommon.visibility = View.VISIBLE
                val subGps = gpTypeMap[blockData].orEmpty()
                if (subGps.isNotEmpty()) {
                    //binding.titleGP.visibility = View.VISIBLE
                    binding.spinnerGP.isEnabled = true
                    val subAdapter = ArrayAdapter(
                        this,
                        android.R.layout.simple_list_item_1,
                        subGps
                    )

                    binding.spinnerGP.setAdapter(subAdapter)
                } else {
                    //binding.titleGP.visibility = View.GONE
                    binding.spinnerGP.isEnabled = false
                }
            }
        }

        binding.spinnerWNNac.setOnClickListener {
            binding.spinnerWNNac.showDropDown()
        }
        binding.spinnerWNNac.setOnItemClickListener { parent, view, position, id ->
            wardData = parent.getItemAtPosition(position).toString()
            println("spinnerWN $wardData")
            binding.titleWNNac.error = null
            binding.spinnerVS.setText("", false)
            val subVSs = villagesNacMap[wardData].orEmpty()
            if (subVSs.isNotEmpty()) {
                //binding.titleGP.visibility = View.VISIBLE
                val subAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    subVSs
                )
                binding.spinnerVSNAc.setAdapter(subAdapter)
            } else {
                //binding.titleGP.visibility = View.GONE
                binding.spinnerVS.isEnabled = false
            }
        }

        binding.spinnerGP.setOnClickListener {
            binding.spinnerGP.showDropDown()
        }

        binding.spinnerVS.setOnClickListener {
            binding.spinnerVS.showDropDown()
        }
        binding.spinnerVSNAc.setOnClickListener {
            binding.spinnerVSNAc.showDropDown()
        }

        binding.spinnerGP.setOnItemClickListener { parent, view, position, id ->
            gpData = parent.getItemAtPosition(position).toString()
            println("spinnerGP $gpData")
            binding.titleGP.error = null
            binding.spinnerVS.setText("", false)
            val subVSs = villageTypeMap[gpData].orEmpty()
            if (subVSs.isNotEmpty()) {
                //binding.titleGP.visibility = View.VISIBLE
                binding.spinnerVS.isEnabled = true
                val subAdapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    subVSs
                )

                binding.spinnerVS.setAdapter(subAdapter)
            } else {
                //binding.titleGP.visibility = View.GONE
                binding.spinnerVS.isEnabled = false
            }
        }

        binding.spinnerVS.setOnItemClickListener { parent, view, position, id ->
            villageData = parent.getItemAtPosition(position).toString()
            println("spinnerVS $villageData")
            binding.titleVS.error = null
        }

        binding.spinnerVSNAc.setOnItemClickListener { parent, view, position, id ->
            villageData = parent.getItemAtPosition(position).toString()
            println("spinnerVSNac $villageData")
            binding.titleVSNAc.error = null
        }

        binding.addTopic.setOnClickListener {
            if (binding.topicContainer.childCount >= maxTopics) {
                Toast.makeText(this, "Maximum 5 topics allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            topicCount++
            val itemBinding = ItemTopicBinding.inflate(
                layoutInflater,
                binding.topicContainer,
                false
            )

            itemBinding.etTopic.hint = "Enter Topic $topicCount"

            itemBinding.ivDelete.setOnClickListener {
                binding.topicContainer.removeView(itemBinding.root)
                topicCount--
                reArrangeHints(itemBinding.etTopic)
            }

            binding.topicContainer.addView(itemBinding.root)
        }
        /*binding.tvDate.setOnClickListener {

            val calendar = Calendar.getInstance()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->

                    val formattedDate =
                        "%02d-%02d-%04d".format(
                            selectedDay,
                            selectedMonth + 1,
                            selectedYear
                        )

                    binding.tvDate.setText(formattedDate)
                },
                year,
                month,
                day
            )

            // Optional: Disable future dates
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            datePickerDialog.show()
        }*/
        setupRecyclerView()
        setupAddPhotoClick()

        /*binding.btnSubmit.setOnClickListener{
            val nameD = binding.etName.text.toString().trim()
            val fatherNameD = binding.etFather.text.toString().trim()
            val contactD = binding.etContact.text.toString().trim()
            val greivanceM = binding.etGrievanceMatter.text.toString().trim()
            if(blockData.isBlank()){
                //showAlert("Alert!", "Please select block")
                binding.tilGrievanceType.error = "Block is required"
                binding.tilGrievanceType.requestFocus()
            }else if(blockData != "Athagarh NAC" && gpData.isBlank()){
                binding.titleGP.error = "GP is required"
                binding.titleGP.requestFocus()
                //showAlert("Alert!", "Please select gp")
            }else if(villageData.isBlank()){
                if(blockData == "Athagarh NAC"){
                    binding.titleVSNAc.error = "Village/Sahi is required"
                    binding.titleVSNAc.requestFocus()
                }else{
                    binding.titleVS.error = "Village/Sahi is required"
                    binding.titleVS.requestFocus()
                }
                //showAlert("Alert!", "Please select village/sahi")
            }else if(wardData.isBlank()){
                if(blockData == "Athagarh NAC"){
                    binding.titleWNNac.error = "Ward No is required"
                    binding.titleWNNac.requestFocus()
                }else{
                    binding.titleWN.error = "Village/Sahi is required"
                    binding.titleWN.requestFocus()
                }
                //showAlert("Alert!", "Please select ward number")
            }else if(nameD.isBlank()){
                binding.titleName.error = "Citizen Name is required"
                binding.titleName.requestFocus()
                //showAlert("Alert!", "Please enter name")
            }else if(fatherNameD.isBlank()){
                binding.titleFS.error = "Father/Spouse Name is required"
                binding.titleFS.requestFocus()
                //showAlert("Alert!", "Please enter father/spouse name")
            }else if(contactD.isBlank()){
                binding.titleContact.error = "Contact is required"
                binding.titleContact.requestFocus()
                //showAlert("Alert!", "Please enter contact")
            }else if(contactD.length<10){
                binding.titleContact.error = "Contact is not valid"
                binding.titleContact.requestFocus()
                //showAlert("Alert!", "Please enter valid contact")
            }else if(greivanceM.isBlank()){
                binding.titleGrievance.error = "Grievance Matter is required"
                binding.titleGrievance.requestFocus()
                //showAlert("Alert!", "Please enter grievance matter")
            }else{
                if(Util.isNetworkAvailable(this@CreateGrievanceActivity)){
                    saveGrievanceData()
                }else{
                    saveOfflineData("offline")
                }
            }
        }*/
        binding.etName.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                binding.titleName.error = null
            }
        }
        binding.etFather.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                binding.titleFS.error = null
            }
        }
        binding.etContact.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                binding.titleContact.error = null
            }
        }
        binding.etGrievanceMatter.addTextChangedListener {
            if (!it.isNullOrBlank()) {
                binding.titleGrievance.error = null
            }
        }
        binding.btnSubmit.setOnClickListener {
            val nameD = binding.etName.text.toString().trim()
            val fatherNameD = binding.etFather.text.toString().trim()
            val contactD = binding.etContact.text.toString().trim()
            val greivanceM = binding.etGrievanceMatter.text.toString().trim()

            var isValid = true

            // Clear previous errors
            binding.tilGrievanceType.error = null
            binding.titleGP.error = null
            binding.titleVS.error = null
            binding.titleVSNAc.error = null
            binding.titleWN.error = null
            binding.titleWNNac.error = null
            binding.titleName.error = null
            binding.titleFS.error = null
            binding.titleContact.error = null
            binding.titleGrievance.error = null

            // Block validation
            if (blockData.isBlank()) {
                binding.tilGrievanceType.error = "Block is required"
                isValid = false
            }

            // GP validation
            if (blockData != "Athagarh NAC" && gpData.isBlank()) {
                binding.titleGP.error = "GP is required"
                isValid = false
            }

            // Village validation
            if (villageData.isBlank()) {
                if (blockData == "Athagarh NAC") {
                    binding.titleVSNAc.error = "Village/Sahi is required"
                } else {
                    binding.titleVS.error = "Village/Sahi is required"
                }
                isValid = false
            }

            // Ward validation
            /*if (wardData.isBlank()) {
                if (blockData == "Athagarh NAC") {
                    binding.titleWNNac.error = "Ward No is required"
                } else {
                    binding.titleWN.error = "Ward No is required"
                }
                isValid = false
            }*/

            // Name validation
            if (nameD.isBlank()) {
                binding.titleName.error = "Citizen Name is required"
                isValid = false
            }

            // Father/Spouse validation
            if (fatherNameD.isBlank()) {
                binding.titleFS.error = "Father/Spouse Name is required"
                isValid = false
            }

            // Contact validation
            if (contactD.isBlank()) {
                binding.titleContact.error = "Contact is required"
                isValid = false
            } else if (contactD.length < 10) {
                binding.titleContact.error = "Contact is not valid"
                isValid = false
            }

            // Grievance validation
            if (greivanceM.isBlank()) {
                binding.titleGrievance.error = "Grievance Matter is required"
                isValid = false
            }

            // Final check
            if (isValid) {
                if (Util.isNetworkAvailable(this@CreateGrievanceActivity)) {
                    saveGrievanceData()
                } else {
                    saveOfflineData("offline")
                }
            }
        }
    }

    private fun reArrangeHints(etTopic: TextInputEditText) {
        for (i in 0 until binding.topicContainer.childCount) {
            val child = binding.topicContainer.getChildAt(i)
            etTopic.hint = "Enter Topic ${i + 1}"
        }
    }

    private fun setupRecyclerView() {
        imageAdapter = ImageAdapter(this,fileList) { position ->
            fileList.removeAt(position)
            imageAdapter.notifyItemRemoved(position)
        }

        binding.recycleViewPhotos.apply {
            layoutManager = LinearLayoutManager(
                this@CreateGrievanceActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            //isNestedScrollingEnabled = false
            adapter = imageAdapter
        }
    }

    private fun setupAddPhotoClick() {
        binding.addPhoto.setOnClickListener {
            if (fileList.size >= 10) {
                Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val options = arrayOf("Camera", "Gallery", "Document")
            AlertDialog.Builder(this)
                .setTitle("Add Photo")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            cameraImageUri = createImageUri()
                            cameraLauncher.launch(cameraImageUri)
                        }
                        1 -> galleryLauncher.launch("image/*")
                        2 -> documentLauncher.launch(
                            arrayOf(
                                "application/pdf",
                                "application/msword", // .doc
                                "application/vnd.openxmlformats-officedocument.wordprocessingml.document" // .docx
                            )
                        )
                    }
                }
                .show()
        }
    }
    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->

            if (uris.isEmpty()) return@registerForActivityResult

            val remainingSlots = 10 - fileList.size

            if (remainingSlots <= 0) {
                Toast.makeText(this, "Maximum 10 files allowed", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            uris.take(remainingSlots).forEach { uri ->
                addImage(uri)
            }
        }


    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                addImage(cameraImageUri)
            }
        }

    private val documentLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->

            if (uris.isEmpty()) return@registerForActivityResult

            val remainingSlots = 10 - fileList.size

            if (remainingSlots <= 0) {
                Toast.makeText(this, "Maximum 10 files allowed", Toast.LENGTH_SHORT).show()
                return@registerForActivityResult
            }

            uris.take(remainingSlots).forEach { uri ->
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                addFile(uri)
            }
        }


    private fun addImage(uri: Uri) {
        if (fileList.size >= 10) {
            Toast.makeText(this, "Maximum 10 images allowed", Toast.LENGTH_SHORT).show()
            return
        }
        fileList.add(uri)
        imageAdapter.notifyItemInserted(fileList.size - 1)
    }

    private fun addFile(uri: Uri) {
        if (fileList.size >= 10) {
            Toast.makeText(this, "Maximum 10 files allowed", Toast.LENGTH_SHORT).show()
            return
        }
        fileList.add(uri)
        println("fileList $fileList")
        imageAdapter.notifyItemInserted(fileList.size - 1)
    }

    fun getFileName(uri: Uri): String {
        var name = "file"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1 && cursor.moveToFirst()) {
                name = cursor.getString(index)
            }
        }
        return name
    }

    private fun createImageUri(): Uri {
        val imageFile = File.createTempFile(
            "camera_",
            ".jpg",
            cacheDir
        )
        return FileProvider.getUriForFile(
            this,
            "${packageName}.provider",
            imageFile
        )
    }

    class ImageAdapter(private val context: Context, private val images: MutableList<Uri>,
                       private val onRemove: (Int) -> Unit) :
        RecyclerView.Adapter<ImageAdapter.ImageVH>() {
        inner class ImageVH(val binding: RowPhotosBinding) :
            RecyclerView.ViewHolder(binding.root) {

            init {
                binding.imageViewDelete.setOnClickListener {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onRemove(position)
                    }
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
            if (isDocumentUri(images[position])) {
                // show document icon
                holder.binding.imageView.setImageResource(com.supragyan.grievancems.R.drawable.documentation)
            } else {
                // show actual image
                holder.binding.imageView.setImageURI(images[position])
            }
        }

        override fun getItemCount() = images.size

        private fun isDocumentUri(uri: Uri): Boolean {
            val mimeType = context.contentResolver.getType(uri)
            return mimeType != null && !mimeType.startsWith("image/")
        }
    }

    private fun saveGrievanceData() {
        if (progressDialog != null) {
            progressDialog!!.show()
        }
        val tag = "user_save"
        val jObj = JSONObject()
        try {
            jObj.put("block", blockData)
            jObj.put("gp", gpData)
            jObj.put("villageSahi", villageData)
            jObj.put("address", binding.etAddress.text.toString().trim())
            jObj.put("wardNo", wardData)
            jObj.put("name", binding.etName.text.toString().trim())
            jObj.put("fatherSpouseName", binding.etFather.text.toString().trim())
            jObj.put("contact", binding.etContact.text.toString().trim())
            val topicD = binding.etTopic1.text.toString().trim()
            if(topicD.isNotEmpty()){
                topics.add(topicD)
            }
            if (binding.topicContainer.isNotEmpty()) {
                for (i in 0 until binding.topicContainer.childCount) {
                    val child = binding.topicContainer.getChildAt(i)
                    val itemBinding = ItemTopicBinding.bind(child)

                    val text = itemBinding.etTopic.text?.toString()?.trim()
                    if (!text.isNullOrEmpty()) {
                        topics.add(text)
                    }
                }
            }
            topics.forEachIndexed { index, value ->
                jObj.put("topic${index + 1}", value)
            }
            jObj.put("grievanceDetails", binding.etGrievanceMatter.text.toString().trim())
            jObj.put("agentName", sharedPreferenceClass?.getValue_string("AGENT_NAME"))
            jObj.put("agentRemarks", binding.etRemark.text.toString().trim())
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
                    }else{
                        showAlert("Success", "New grievance created successfully")
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

                if ( response != null) {
                    val statusCode = error.networkResponse.statusCode

                    if (statusCode == 401 || statusCode == 402 || statusCode == 403 || statusCode == 404) {
                        val responseBody = String(error.networkResponse.data, StandardCharsets.UTF_8)
                        val jsonObject = JSONObject(responseBody)
                        val message = jsonObject.optString("message", "Something went wrong! please try after some time")
                        val header = jsonObject.optString("error", "Authentication failed")
                        showAlert(header,message)
                    }else{
                        showAlert("Server Error","Server Error!!! The grievance has been saved locally.")
                        saveOfflineData("")
                    }
                } else {
                    /*Toast.makeText(
                        this@LoginActivity,
                        "Server Error!!! Please Try After Some Time.",
                        Toast.LENGTH_LONG
                    ).show()*/
                    showAlert("Server Error","Server Error!!! The grievance has been saved locally.")
                    saveOfflineData("")
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
        val alert = AlertDialog.Builder(this@CreateGrievanceActivity)
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

    private fun saveOfflineData(from: String){
        val topicsOffline = mutableListOf<String>()
        val topic1 =  binding.etTopic1.text.toString().trim()
        topicsOffline.add(topic1)
        if (binding.topicContainer.isNotEmpty()) {
            for (i in 0 until binding.topicContainer.childCount) {
                val child = binding.topicContainer.getChildAt(i)
                val itemBinding = ItemTopicBinding.bind(child)

                val text = itemBinding.etTopic.text?.toString()?.trim()
                if (!text.isNullOrEmpty()) {
                    topicsOffline.add(text)
                }
            }
        }
        val offlinePathList = mutableListOf<String>()
        if(fileList.size>0){
            fileList.forEach { uri ->
                try {
                    val path = copyUriToInternalStorage(this, uri)
                    offlinePathList.add(path)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        println("offlinePathList $offlinePathList")
        val uniqueId = UUID.randomUUID().toString()
        val model = GrievanceModel()
        model.offlineID = uniqueId
        model.userID = sharedPreferenceClass?.getValue_string("USERID")
        model.block = blockData
        model.gp = gpData
        model.village = villageData
        model.address = binding.etAddress.text.toString().trim()
        model.wardNo = wardData
        model.name = binding.etName.text.toString().trim()
        model.fatherName = binding.etFather.text.toString().trim()
        model.contact = binding.etContact.text.toString().trim()
        model.topic = topicsOffline.joinToString(",")
        model.grievanceMatter = binding.etGrievanceMatter.text.toString().trim()
        model.remark = binding.etRemark.text.toString().trim()
        model.photos = offlinePathList.joinToString (",")
        model.grievanceID = grievanceID
        model.uploadID = uploadID
        db.addGrievanceData(model)
        if(from == "offline"){
            showAlert("No Network","No network connection detected. The data has been saved locally.")
        }
    }

    /*private fun copyUriToInternalStorage(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI")

        val file = File(
            context.filesDir,
            "img_${System.currentTimeMillis()}.jpg"
        )

        FileOutputStream(file).use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
    }*/
    private fun copyUriToInternalStorage(context: Context, uri: Uri): String {

        val contentResolver = context.contentResolver

        // Get original file name
        var fileName = "file_${System.currentTimeMillis()}"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex)
            }
        }

        val inputStream = contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Cannot open URI")

        val file = File(context.filesDir, fileName)

        FileOutputStream(file).use { output ->
            inputStream.use { input ->
                input.copyTo(output)
            }
        }

        return file.absolutePath
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
                val contentType =
                    contentResolver.getType(uri) ?: "application/octet-stream"

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

            val contentType =
                contentResolver.getType(fileUri) ?: "application/octet-stream"
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