package com.supragyan.grievancems.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.supragyan.grievancems.R
import com.supragyan.grievancems.databinding.ActivityMainBinding
import com.supragyan.grievancems.databinding.AgentNameDialogBinding
import com.supragyan.grievancems.utility.SharedPreferenceClass

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var sharedPreferenceClass: SharedPreferenceClass? = null
    var tvContact : TextView? = null

        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferenceClass= SharedPreferenceClass(this@MainActivity)
        setSupportActionBar(binding.appBarMain.toolbar)

        window.navigationBarColor = Color.BLACK
        val headerView = binding.navView.getHeaderView(0)
        val tvUserName = headerView.findViewById<TextView>(R.id.tvAgentName)
        tvContact = headerView.findViewById<TextView>(R.id.textViewContact)
        tvUserName.text = "Logged in as: " + sharedPreferenceClass!!.getValue_string("NAME")
        tvContact?.text = "Agent Name: " +  sharedPreferenceClass?.getValue_string("AGENT_NAME")

        val nameA = sharedPreferenceClass?.getValue_string("AGENT_NAME")
        if(nameA!!.isBlank() || nameA == "false" ){
            showAgentNameDialog()
        }
        binding.appBarMain.fab.setOnClickListener { view ->
            /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null)
                .setAnchorView(R.id.fab).show()*/
            val intent = Intent(this, CreateGrievanceActivity::class.java)
            startActivity(intent)
        }
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(setOf(
            R.id.nav_home,
            R.id.nav_gallery,
            R.id.nav_slideshow
        ), drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        navView.setNavigationItemSelectedListener(NavigationView.OnNavigationItemSelectedListener { menuItem ->

            // This method will trigger on item Click of navigation menu
            when (menuItem.itemId) {

                R.id.nav_gallery -> {

                    if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        drawerLayout.closeDrawer(Gravity.LEFT)
                    }
                    showLogout()
                }
                R.id.nav_home -> {

                    if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                        drawerLayout.closeDrawer(Gravity.LEFT)
                    }
                }
            }
            true
        })

    }

    private fun showLogout(){
        AlertDialog.Builder(this@MainActivity)
            .setTitle("Alert")
            .setCancelable(false)
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { dialog, which ->
                //db?.emptyBadgeTable()
                sharedPreferenceClass!!.clearData()
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                finishAffinity()

            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun showAgentNameDialog() {
        val dialog = Dialog(this)
        val binding = AgentNameDialogBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(false)

        binding.btnCancel.setOnClickListener {
            dialog.dismiss()
            hideKeyboard()
        }

        binding.btnYes.setOnClickListener {
            val remarkD = binding.etName.text.toString().trim()

            if (remarkD.isEmpty()) {
                Toast.makeText(this@MainActivity, "Enter name", Toast.LENGTH_SHORT).show()
            } else {
                // your logic here
                sharedPreferenceClass?.setValue_string("AGENT_NAME",remarkD)
                tvContact?.text = "Agent Name: " +  remarkD
                dialog.dismiss()
                hideKeyboard()
            }
        }

        dialog.show()

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager?
        imm?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }
}