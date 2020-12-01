package com.example.trilock.data.register_login.activities.notifications

import android.content.Context
import android.content.SharedPreferences
import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trilock.R
import com.example.trilock.data.model.ui.people.PeopleViewModel
import com.example.trilock.data.register_login.classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.log
import kotlin.collections.mutableListOf as mutableListOf

class PeopleFragment : Fragment() {

    private val TAG = "PeopleFragment"

    private lateinit var peopleViewModel: PeopleViewModel
    private lateinit var peopleRecyclerView: RecyclerView
    val db = Firebase.firestore
    var auth: FirebaseAuth = Firebase.auth

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
    private var isOwner: Boolean = false
    private lateinit var userUid: String
    private var ownerArrayList: ArrayList<String> = ArrayList()
    private var guestArrayList: ArrayList<String> = ArrayList()
    private var userList: ArrayList<User> = ArrayList()
    private lateinit var user: User




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
        sharedPreferences = context?.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)!!
        currentLock = sharedPreferences.getString("LOCK", "You have no lock")!!

        userUid = auth.uid.toString()

        peopleRecyclerView = root.findViewById(R.id.recyclerview_people)
        linearLayoutManager = LinearLayoutManager(context)
        peopleRecyclerView.layoutManager = linearLayoutManager
        adapter = PeopleAdapter(userList, isOwner)
        peopleRecyclerView.adapter = adapter
        isOwner()


        buttonInvite.setOnClickListener {
            inviteUser()
        }

        db.collection("locks").document(currentLock).get().addOnSuccessListener {
            documentSnapshot ->
            lockTitle = documentSnapshot["title"].toString()
            textViewLockTitle.text = lockTitle
        }

        return root
    }

    private fun inviteUser() {
        if (editTextEmailInvite.text.isEmpty()) {
            inputAgain(editTextEmailInvite, "Please put in the email of the user you would like to invite")
        } else if (!editTextEmailInvite.text.toString().isEmailValid()) {
            inputAgain(editTextEmailInvite, "Please enter a valid email address")
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
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun makeDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Are you sure?")
        builder.setMessage("This action will make $guestName able to unlock your lock '$lockTitle'")
        builder.setNeutralButton(R.string.cancel)
        {
            dialogInterface, which ->
            closeKeyboardAndRemoveText()
            toast("Action cancelled")
        }
        builder.setPositiveButton(R.string.accept)
        {
                _, _ ->
            db.collection("locks")
                .document(currentLock)
                .update("guests",FieldValue.arrayUnion(guestId))
            toast("$guestName has now been added to your lock")
            closeKeyboardAndRemoveText()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun isOwner() {
        db.collection("locks")
            .document(currentLock)
            .get().addOnSuccessListener { document  ->
                Log.i(TAG, document.toString())
                ownerArrayList = document.get("owners") as ArrayList<String>
                guestArrayList = document.get("guests") as ArrayList<String>
                Log.i(TAG, ownerArrayList.toString())
                Log.i(TAG, userUid)
                if (!ownerArrayList.contains(userUid)) {
                    isOwner = false
                    buttonInvite.isInvisible = true
                    editTextEmailInvite.isInvisible = true
                } else {
                    isOwner = true
                }
                for (userId in ownerArrayList) {
                    Log.i("HELLOOOOO", userId)
                    db.collection("users")
                        .document(userId).get()
                        .addOnSuccessListener {
                            if(document != null && document.exists()) {
                                Log.i("GET?? INSTEAD",document.data?.get("firstName").toString())
                                Log.i("GET INSTEAD",document.get("firstName").toString())
                                Log.i("second for loop", document.data!!["firstName"].toString())
                                user = User(
                                    document.data!!["firstName"].toString(),
                                    true
                                )
                                userList.add(user)
                                adapter.notifyDataSetChanged()
                            }
                        }
                }
                for (userId in guestArrayList) {
                    Log.i("HELLOOOOO", userId)
                    db.collection("users")
                        .document(userId).get()
                        .addOnSuccessListener {
                            Log.i(TAG,document.data!!["firstName"].toString())
                            user = User(
                            document["firstName"].toString(),
                            false
                        )
                            userList.add(user)
                            adapter.update(userList)
                        }
                }
                Log.i(TAG, userList.toString())
            }
    }

    private fun closeKeyboardAndRemoveText() {
        editTextEmailInvite.setText("")
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }

    private fun inputAgain(editText: EditText, toast: String) {
        editText.requestFocus()
        val imm: InputMethodManager =
            activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
        toast(toast)
    }
}





