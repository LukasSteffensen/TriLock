package com.example.trilock.data.model

import android.graphics.Color.red
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.trilock.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications))
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        val textViewLockStatus = findViewById<TextView>(R.id.text_lock_status)
        // get reference to ImageView

        val imageView = findViewById<ImageView>(R.id.imageView)
        // set on-click listener for ImageView
        imageView.setOnClickListener {
            // your code here
            if (textViewLockStatus.text.equals("Locked")) {
                textViewLockStatus.setText("Unlocked")
                textViewLockStatus.setTextColor(3)
            } else {
                textViewLockStatus.setText("Locked")
                textViewLockStatus.setTextColor(2)
            }
        }
    }
}