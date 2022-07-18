package com.saboon.defter.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.MenuItemCompat
import androidx.core.view.get
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.saboon.defter.R
import com.saboon.defter.databinding.ActivityMainBinding
import com.saboon.defter.fragments.LogInFragment
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var navController: NavController

    private val childFragmentList = arrayOf(
        R.id.homeFragment,
        R.id.addNewMomentFragment,
        R.id.datesFragment
    )
    private val childFragmentMenuList = arrayOf(0, 1, 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = FirebaseAuth.getInstance()

        val navHostFragment  = supportFragmentManager.findFragmentById(binding.fragmentContainerViewMain.id)  as NavHostFragment
        binding.bottomNavigation.setupWithNavController(navHostFragment .navController)
        navController = navHostFragment.navController
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navController.navigateUp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        navController.previousBackStackEntry?.let {
            val destinationFragment = it.destination.id
            if (childFragmentList.contains(destinationFragment)){
                binding.bottomNavigation.menu[childFragmentMenuList[childFragmentList.indexOf(destinationFragment)]].isChecked = true
            }
        }

        super.onBackPressed()
    }




}