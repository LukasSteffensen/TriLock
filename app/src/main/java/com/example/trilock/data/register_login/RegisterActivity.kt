package com.example.trilock.data.register_login

import android.R.attr.password
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trilock.R
import com.example.trilock.data.model.LoginActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.regex.Matcher
import java.util.regex.Pattern


class RegisterActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Access a Cloud Firestore instance from your Activity
        val db = Firebase.firestore

        val textViewSignIn = findViewById<TextView>(R.id.textViewLogin)

        val editTextName = findViewById<EditText>(R.id.editTextTextPersonName)
        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        val editTextEmail = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextTextPassword)

        textViewSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            val name = editTextName.text.toString().trim()
            val phoneNumber = editTextPhone.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            //Checks if all the edittexts are empty and if all requirements are met
            if (name.isEmpty()) {
                editTextName.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextName, InputMethodManager.SHOW_IMPLICIT)
                toast("Please put in your name")
            } else if (phoneNumber.isEmpty()) {
                editTextPhone.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextPhone, InputMethodManager.SHOW_IMPLICIT)
                toast("Please put in your phone number")
            } else if (phoneNumber.length != 8) {
                editTextPhone.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextPhone, InputMethodManager.SHOW_IMPLICIT)
                toast("Please enter a valid phone number")
            } else if (email.isEmpty()) {
                editTextEmail.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextEmail, InputMethodManager.SHOW_IMPLICIT)
                toast("Please put in your email address")
            } else if (!email.isEmailValid()) {
                editTextEmail.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextEmail, InputMethodManager.SHOW_IMPLICIT)
                toast("Please enter a valid email address")
            } else if (password.isEmpty()) {
                editTextPassword.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextPassword, InputMethodManager.SHOW_IMPLICIT)
                toast("Please put in your password")
            } else if (!password.isPasswordValid()) {
                editTextPassword.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextPassword, InputMethodManager.SHOW_IMPLICIT)
                toast("Password must be at least 8 characters and contain at least one number, uppercase letter and lowercase letter")
            } else {

                Log.i("RegisterActivity: ", "we hit the else!")

                //Send verification email, maybe before encryption?

                //Encryption of user info here

                //DATABASE STUFF HERE
                val user = hashMapOf(
                    "Name" to name,
                    "Phone" to phoneNumber,
                    "Email" to email,
                    "Password" to password
                )

                db.collection("users")
                    .add(user)
                    .addOnSuccessListener { documentReference ->
                        Log.d("RegisterActivity: ", "DocumentSnapshot added with ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("RegisterActivity: ", "Error adding document", e)
                    }

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //Checks if password meets the requirements
    private fun String.isPasswordValid(): Boolean {
        val pattern: Pattern
        val matcher: Matcher

        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}$"

        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(this)

        return matcher.matches()
    }

    //Checks if email is valid
    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }
}