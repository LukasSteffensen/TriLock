package com.example.trilock.data.register_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.trilock.R
import com.example.trilock.data.model.LoginActivity
import com.example.trilock.data.register_login.classes.Encryption
import com.google.firebase.firestore.Blob
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.HashMap
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        // Access a Cloud Firestore instance from your Activity
        val db = Firebase.firestore

        val textViewSignIn = findViewById<TextView>(R.id.textViewLogin)

        val editTextFirstName = findViewById<EditText>(R.id.editTextTextFirstName)
        val editTextLastName = findViewById<EditText>(R.id.editTextTextLastName)
        val editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        val editTextEmail = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextTextPassword)

        textViewSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        val buttonRegister = findViewById<Button>(R.id.buttonRegister)

        buttonRegister.setOnClickListener {
            val firstName = editTextFirstName.text.toString().trim()
            val lastName = editTextLastName.text.toString().trim()
            val phoneNumber = editTextPhone.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val password = editTextPassword.text.toString().trim()

            //Checks if all the edittexts are empty and if some requirements are not met
            if (firstName.isEmpty()) {
                editTextFirstName.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextFirstName, InputMethodManager.SHOW_IMPLICIT)
                toast("Please put in your first name")
            } else if (lastName.isEmpty()) {
                editTextLastName.requestFocus()
                val imm: InputMethodManager =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(editTextLastName, InputMethodManager.SHOW_IMPLICIT)
                toast("Please put in your last name")
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
                toast("Password must be at least 8 characters and contain at least a number, uppercase letter and lowercase letter")
            } else {

                Log.i("RegisterActivity: ", "we hit the else!")

                //Send verification email, maybe before encryption?

                //Encryption of user info here

                val encryptedFirstName = Encryption().encrypt(firstName.toByteArray(Charsets.UTF_8), password)
                val encryptedLastName = Encryption().encrypt(lastName.toByteArray(Charsets.UTF_8), password)
                val encryptedPhoneNumber = Encryption().encrypt(phoneNumber.toByteArray(Charsets.UTF_8), password)
                val encryptedEmail = Encryption().encrypt(email.toByteArray(Charsets.UTF_8), password)

                val decryptedFirstName = Encryption().decrypt(encryptedFirstName, password)
                val decryptedFirstNameString = Base64.encodeToString(decryptedFirstName, Base64.NO_WRAP)

                Log.i("Register first decrypt:", "" + decryptedFirstNameString)

                val encryptedFirstNameList = HashMap<String, String>()
                val encryptedLastNameList = HashMap<String, String>()
                val encryptedPhoneNumberList = HashMap<String, String>()
                val encryptedEmailList = HashMap<String, String>()

                encryptedFirstNameList["salt"] = Base64.encodeToString(encryptedFirstName["salt"],Base64.NO_WRAP)
                encryptedFirstNameList["iv"] = Base64.encodeToString(encryptedFirstName["iv"],Base64.NO_WRAP)
                encryptedFirstNameList["encrypted"] = Base64.encodeToString(encryptedFirstName["encrypted"],Base64.NO_WRAP)

                val encrypted = Base64.decode(encryptedFirstNameList["encrypted"], Base64.NO_WRAP)
                val iv = Base64.decode(encryptedFirstNameList["iv"], Base64.NO_WRAP)
                val salt = Base64.decode(encryptedFirstNameList["salt"], Base64.NO_WRAP)

                val decrypted = Encryption().decrypt(
                    hashMapOf("iv" to iv, "salt" to salt, "encrypted" to encrypted), password)

                val name: String = Base64.encodeToString(decrypted, Base64.NO_WRAP)

                Log.i("Register decryption: ", ""+name)


                encryptedLastNameList["salt"] = Base64.encodeToString(encryptedLastName["salt"],Base64.NO_WRAP)
                encryptedLastNameList["iv"] = Base64.encodeToString(encryptedLastName["iv"],Base64.NO_WRAP)
                encryptedLastNameList["encrypted"] = Base64.encodeToString(encryptedLastName["encrypted"],Base64.NO_WRAP)

                encryptedPhoneNumberList["salt"] = Base64.encodeToString(encryptedPhoneNumber["salt"],Base64.NO_WRAP)
                encryptedPhoneNumberList["iv"] = Base64.encodeToString(encryptedPhoneNumber["iv"],Base64.NO_WRAP)
                encryptedPhoneNumberList["encrypted"] = Base64.encodeToString(encryptedPhoneNumber["encrypted"],Base64.NO_WRAP)

                encryptedEmailList["salt"] = Base64.encodeToString(encryptedEmail["salt"],Base64.NO_WRAP)
                encryptedEmailList["iv"] = Base64.encodeToString(encryptedEmail["iv"],Base64.NO_WRAP)
                encryptedEmailList["encrypted"] = Base64.encodeToString(encryptedEmail["encrypted"],Base64.NO_WRAP)

                //DATABASE STUFF HERE
                val user = hashMapOf(
                    "firstName" to encryptedFirstNameList,
                    "lastName" to encryptedLastNameList,
                    "phone" to encryptedPhoneNumberList,
                    "email" to encryptedEmailList
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