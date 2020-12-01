package com.example.trilock.data.register_login.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.trilock.R
import com.example.trilock.data.register_login.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest

class AddLockActivity : AppCompatActivity() {

    private val TAG = "AddLockActivity"

    lateinit var sharedPreferences: SharedPreferences
    val PREFS_FILENAME = "SHARED_PREF"

    private lateinit var editTextLock: EditText
    private lateinit var editTextTitle: EditText
    private lateinit var buttonAddLock: Button

    private lateinit var title: String
    private lateinit var lockCode: String
    private lateinit var lock: HashMap <*,*>
    private lateinit var lockHash: String
    private lateinit var userUid: String

    private val db = Firebase.firestore
    var auth: FirebaseAuth = Firebase.auth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_lock)

        editTextLock = findViewById(R.id.edit_text_lock)
        editTextTitle = findViewById(R.id.edit_text_title)
        buttonAddLock = findViewById(R.id.button_add_lock)

        sharedPreferences = getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

        buttonAddLock.setOnClickListener {
            when {
                editTextTitle.text.isEmpty() -> {
                    inputAgain(editTextTitle, "Please put in the title for your lock")
                }
                editTextLock.text.isEmpty() -> {
                    inputAgain(editTextLock, "Please put in the code for your lock")
                }
                else -> {
                    lockCode = editTextLock.text.toString()
                    Log.i(TAG, lockCode)
                    lockHash = hashString(lockCode, "SHA-256")
                    Log.i(TAG, lockHash)
                    val docRef = db.collection("unregisteredLocks").document(lockHash)
                    docRef.get()
                        .addOnSuccessListener { document ->
                            if (document != null) {
                                Log.d(TAG, "DocumentSnapshot data: $document")
//                                add new lock to database along with user
                                title = editTextTitle.text.toString()
                                userUid = auth.uid.toString()
                                Log.i(TAG, title)
                                Log.i(TAG, userUid)

                                lock = hashMapOf(
                                    "title" to title,
                                    "owners" to arrayListOf(userUid)
                                )
                                addLock()
                            } else {
                                Log.d(TAG, "No such document")
                                toast("Please enter valid lock code")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.d(TAG, "get failed with ", exception)
                        }
                }
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun hashString(input: String, algorithm: String): String    {
        return MessageDigest.getInstance(algorithm)
            .digest(input.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }

    private fun inputAgain(editText: EditText, toast: String) {
        editText.requestFocus()
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        toast(toast)
    }

    private fun addLock() {
        db.collection("locks")
            .add(lock)
            .addOnSuccessListener { document ->
                        toast("Lock successfully added!")
                        deleteUnregisteredLock()
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                        saveLockSelection(document.id)
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        e -> Log.w(TAG, "Error writing document", e)
                    }
    }

    private fun deleteUnregisteredLock() {
        db.collection("unregisteredLocks").document(lockHash)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    private fun saveLockSelection(currentLock: String) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("LOCK", currentLock)
        editor.apply()
        Log.i(TAG, currentLock)
    }
}