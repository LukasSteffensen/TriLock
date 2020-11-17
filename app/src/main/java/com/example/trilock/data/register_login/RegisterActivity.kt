package com.example.trilock.data.register_login

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
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
import java.nio.charset.Charset
import java.security.KeyStore
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


class RegisterActivity : AppCompatActivity() {

    private val IV_LENGTH = 16
    private val charset = Charsets.UTF_8

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //KeyGenerator
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore")
        val keyGenParameterSpec = KeyGenParameterSpec.Builder("MyKeyAlias",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()

        //TESTING ENCRYPTION AND DECRYPTION
        val pair = encryptData("Hello, this is test")

        val decryptedData: String = decryptData(pair.first, pair.second)

        Log.i("Encryption test: ", pair.second.toString())
        Log.i("Decryption test: ", decryptedData)

        //END TEST

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
                toast("Password must be at least 8 characters and contain at least one number, uppercase letter and lowercase letter")
            } else {



                Log.i("RegisterActivity: ", "we hit the else!")

                //Send verification email, maybe before encryption?

                //Encryption of user info here

                //DATABASE STUFF HERE
                val user = hashMapOf(
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "phone" to phoneNumber,
                    "email" to email,
                    "password" to password
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

    fun getKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val secretKeyEntry = keyStore.getEntry("MyKeyAlias", null) as KeyStore.SecretKeyEntry
        return secretKeyEntry.secretKey
    }

    fun encryptData(data: String): Pair<ByteArray, ByteArray> {
        val cipher :Cipher = Cipher.getInstance("AES/CBC/NoPadding")

        var temp :String = data
        while(temp.toByteArray().size%16 != 0) {
            temp+= "\u0020"
        }
        cipher.init(Cipher.ENCRYPT_MODE, getKey())

        val ivBytes = cipher.iv

        val encryptedBytes: ByteArray = cipher.doFinal(temp.toByteArray(Charsets.UTF_8))

        return Pair(ivBytes, encryptedBytes)
    }

    fun decryptData(ivBytes: ByteArray, data: ByteArray): String {
        val cipher = Cipher.getInstance("AES/CBC/NoPadding")
        val spec = IvParameterSpec(ivBytes)

        cipher.init(Cipher.DECRYPT_MODE, getKey(),spec)
        return cipher.doFinal(data).toString(Charsets.UTF_8).trim()
    }
}