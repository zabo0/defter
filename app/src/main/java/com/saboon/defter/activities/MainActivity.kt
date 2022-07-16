package com.saboon.defter.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.core.view.MenuItemCompat
import com.bumptech.glide.Glide
import com.saboon.defter.R
import com.saboon.defter.databinding.ActivityMainBinding
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)



        Glide
            .with(this)
            .load("https://images.unsplash.com/photo-1603415526960-f7e0328c63b1?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80")
            .into(binding.appBarAccountImage)

    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//
//        val menuItem = menu.findItem(R.id.goToAccount)
//        val view = menuItem.actionView
//
//        val accountImage: CircleImageView = view.findViewById(R.id.toolbarAccountIcon)
//
//        Glide
//            .with(this)
//            .load("https://images.unsplash.com/photo-1603415526960-f7e0328c63b1?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1170&q=80")
//            .into(accountImage)
//
//        accountImage.setOnClickListener {
//            //go to account
//        }
//        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
//        return true
//    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }




}