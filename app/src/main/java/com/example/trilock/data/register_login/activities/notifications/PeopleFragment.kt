package com.example.trilock.data.register_login.activities.notifications

import android.content.Context
import android.content.SharedPreferences
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trilock.R
import com.example.trilock.data.model.ui.people.PeopleViewModel
import com.example.trilock.data.register_login.classes.Encryption
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.collections.mutableListOf as mutableListOf

class PeopleFragment : Fragment() {

    private val TAG = "PeopleFragment"

    private lateinit var peopleViewModel: PeopleViewModel
    private lateinit var peopleRecyclerView: RecyclerView
    val db = Firebase.firestore
    private lateinit var adapter: PeopleAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var buttonInvite: Button
    private lateinit var editTextEmailInvite: EditText
    private lateinit var textViewLockTitle: TextView
    private lateinit var currentLock: String
    private lateinit var inviteEmail: String
    private lateinit var guestId: String
    private lateinit var guestName: String
    private lateinit var lockTitle: String


    private val PREFS_FILENAME = "SHARED_PREF"
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        peopleViewModel =
            ViewModelProvider(this).get(PeopleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_people, container, false)
        val rcl = inflater.inflate(R.layout.list_people, container, false)
        val peopleSwitch: Switch = rcl.findViewById(R.id.switch_people)

        buttonInvite = root.findViewById(R.id.button_invite)
        editTextEmailInvite = root.findViewById(R.id.edit_text_email_invite)
        textViewLockTitle = root.findViewById(R.id.text_view_people_lock_title)

        peopleRecyclerView = root.findViewById(R.id.recyclerview_people)
        linearLayoutManager = LinearLayoutManager(context)
        peopleRecyclerView.layoutManager = linearLayoutManager

        sharedPreferences = context?.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)!!
        currentLock = sharedPreferences.getString("LOCK", "You have no lock")!!

        buttonInvite.setOnClickListener {
            inviteUser()
        }

        db.collection("locks").document(currentLock).get().addOnSuccessListener {
            documentSnapshot ->
            lockTitle = documentSnapshot["title"].toString()
            textViewLockTitle.text = lockTitle
        }

        dataFirestore()

        peopleSwitch.setOnClickListener {
            makeDialog()
        }

        return root
    }

    private fun inviteUser() {
        if (editTextEmailInvite.text.isEmpty()) {
            toast("Please put in the email of the user you would like to invite")
        } else if (!editTextEmailInvite.text.toString().isEmailValid()) {
            toast("Please enter a valid email address")
        } else {
            inviteEmail = editTextEmailInvite.text.toString()
            db.collection("users")
                .whereEqualTo("email", inviteEmail)
                .get().addOnSuccessListener {result ->
                    if (result.isEmpty) {
                        toast("Unable to invite user, check that you have written their email correctly")
                    } else {
                    for (document in result) {
                        if (inviteEmail == document["email"]) {
                            guestId = document.id
                            guestName = document["firstName"].toString() + " " + document["lastName"].toString()
                            makeDialog()
                            }
                        }
                    }
                }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun dataFirestore() {

        //Trying to collect firstnames from Database.
        val userList: ArrayList<String> = mutableListOf<String>() as ArrayList<String>
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val name = document.data["firstName"].toString()
                    userList.add(name)
                }
                adapter = PeopleAdapter(userList)
                peopleRecyclerView.adapter = adapter
            }

            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
                Toast.makeText(context, "Firestore not working", Toast.LENGTH_SHORT).show()
            }
    }

    private fun makeDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Are you sure?")
        builder.setMessage("This action will make $guestName able to unlock your lock '$lockTitle'")
        builder.setNeutralButton(R.string.cancel)
        {
            dialogInterface, which ->
            toast("Action cancelled")
        }
        builder.setPositiveButton(R.string.accept)
        {
                _, _ ->
            db.collection("locks")
                .document(currentLock)
                .update("guests",FieldValue.arrayUnion(guestId))
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }
}





