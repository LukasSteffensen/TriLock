package com.example.trilock.data.model

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import com.example.trilock.R
import com.example.trilock.data.register_login.MainActivity
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences
    var isSwitched = false
    val PREFS_FILENAME = "SHARED_PREF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)
        isSwitched = sharedPreferences.getBoolean("SWITCH", false)
        Log.i("LoginActivity: ", isSwitched.toString())
        if (isSwitched) {
            Toast.makeText(this, "Biometric authentication is turned on", LENGTH_SHORT).show()
            // BIOMETRIC AUTHENTICATION STUFF GOES HERE
        } else {
            Toast.makeText(this, "Biometric authentication is turned off", LENGTH_SHORT).show()
            // ALTERNATIVE LOG IN METHOD HERE
        }

        val button = findViewById<Button>(R.id.button_login)
        button?.setOnClickListener()
        {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
