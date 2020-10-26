package com.example.trilock.data.register_login

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.trilock.R

class MainActivity : AppCompatActivity() {

    private val button_register: Button = findViewById<Button>(R.id.testButton)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        button_register.setOnClickListener {

        }

    }
}