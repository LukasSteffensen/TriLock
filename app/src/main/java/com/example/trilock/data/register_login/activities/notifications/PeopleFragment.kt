package com.example.trilock.data.register_login.activities.notifications

import android.content.Context
import android.content.SharedPreferences
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trilock.R
import com.example.trilock.data.model.LoginActivity
import com.example.trilock.data.model.ui.people.PeopleViewModel
import com.example.trilock.data.register_login.activities.AddLockActivity
import com.example.trilock.data.register_login.classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class PeopleFragment : Fragment(), PeopleAdapter.OnItemClickListener {

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
    lateinit var currentUser: FirebaseUser
    private lateinit var inviteEmail: String
    private lateinit var guestId: String
    private lateinit var guestLastName: String
    private lateinit var guestFirstName: String
    private lateinit var lockTitle: String
    private var isOwner: Boolean = false
    private lateinit var userUid: String
    private var ownerArrayList: ArrayList<String> = ArrayList()
    private var guestArrayList: ArrayList<String> = ArrayList()
    private var arrayListOfLocks: ArrayList<String> = ArrayList()
    private var userList: ArrayList<User> = ArrayList()
    private lateinit var user: User
    private lateinit var alertDialogBuilder: AlertDialog.Builder

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

        buttonInvite = root.findViewById(R.id.button_invite)
        editTextEmailInvite = root.findViewById(R.id.edit_text_email_invite)
        textViewLockTitle = root.findViewById(R.id.text_view_people_lock_title)
        sharedPreferences = context?.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)!!
        currentLock = sharedPreferences.getString("LOCK", "You have no lock")!!

        userUid = auth.uid.toString()

        peopleRecyclerView = root.findViewById(R.id.recyclerview_people)
        linearLayoutManager = LinearLayoutManager(context)
        peopleRecyclerView.layoutManager = linearLayoutManager
        adapter = PeopleAdapter(userList, this, isOwner)
        peopleRecyclerView.adapter = adapter
        ownerCheckAndAdapterUpdate()

        alertDialogBuilder = AlertDialog.Builder(context)
        currentUser = auth.currentUser!!

        getLocks()

        updateLockTitle()

        buttonInvite.setOnClickListener {
            checkInputAndGetUserFromDatabase()
        }

        return root
    }

    override fun onItemClicked(user: User) {
        alertRemoveGuest(user)
    }

    private fun removeGuest(user: User) {
        db.collection("locks")
            .document(currentLock)
            .update("guests", FieldValue.arrayRemove(user.userId)).addOnSuccessListener {
                userList.remove(user)
                adapter.update(userList, true)
            }
    }

    private fun checkInputAndGetUserFromDatabase() {
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
                            if (guestId == userUid) {
                                toast("You cannot add yourself")
                            } else {
                                guestFirstName = document["firstName"].toString()
                                guestLastName = document["lastName"].toString()
                                alertAddGuest()
                                }
                            }
                        }
                    }
                }
        }
    }

    private fun String.isEmailValid(): Boolean {
        return !TextUtils.isEmpty(this) && android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()
    }

    private fun alertAddGuest() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Are you sure?")
        builder.setMessage("This action will make $guestFirstName $guestLastName able to unlock your lock '$lockTitle'")
        builder.setNeutralButton(R.string.cancel)
        {
            dialogInterface, which ->
            closeKeyboardAndRemoveText()
            toast("Action cancelled")
        }
        builder.setPositiveButton(R.string.accept)
        {
                _, _ ->
            addGuest()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    private fun addGuest() {
        db.collection("locks")
            .document(currentLock)
            .update("guests",FieldValue.arrayUnion(guestId)).addOnSuccessListener {
                toast("$guestFirstName $guestLastName has now been added to your lock")
                val guest: User = User(guestFirstName, guestLastName, false, guestId)
                userList.add(guest)
                adapter.update(userList, true)
                closeKeyboardAndRemoveText()
            }
    }

    private fun ownerCheckAndAdapterUpdate() {
        db.collection("locks")
            .document(currentLock)
            .get().addOnSuccessListener { document  ->
                Log.i(TAG, document.toString())
                if (document.get("owners") != null) {
                    ownerArrayList = document.get("owners") as ArrayList<String>
                }
                if (document.get("guests") != null) {
                    guestArrayList = document.get("guests") as ArrayList<String>
                }
                Log.i(TAG, ownerArrayList.toString())
                Log.i(TAG, userUid)
                if (!ownerArrayList.contains(userUid)) {
                    isOwner = false
                    buttonInvite.isInvisible = true
                    editTextEmailInvite.isInvisible = true
                } else {
                    buttonInvite.isInvisible = false
                    editTextEmailInvite.isInvisible = false
                    isOwner = true
                }
                for (userId in ownerArrayList) {
                    Log.i("HELLOOOOO", userId)
                    db.collection("users")
                        .document(userId).get()
                        .addOnSuccessListener { userDocument ->
                            if(userDocument != null && userDocument.exists()) {
                                Log.i("second for loop", userDocument.data!!["firstName"].toString())
                                user = User(
                                    userDocument.data!!["firstName"].toString(),
                                    userDocument.data!!["lastName"].toString(),
                                    true,
                                    userId
                                )
                                userList.add(user)
                                adapter.update(userList, isOwner)
                            }
                        }
                }
                for (userId in guestArrayList) {
                    Log.i("HELLOOOOO", userId)
                    db.collection("users")
                        .document(userId).get()
                        .addOnSuccessListener {guestDocument ->
                            Log.i(TAG, guestDocument.data!!["firstName"].toString())
                            user = User(
                            guestDocument["firstName"].toString(),
                            guestDocument["lastName"].toString(),
                            false,
                                userId
                        )
                            userList.add(user)
                            adapter.update(userList, isOwner)
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

    //enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    //inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.options_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    //handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id to handle item clicks
        val id = item.itemId
        //handle item clicks
        if (id == R.id.action_add){
            val intent = Intent(context, AddLockActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.action_next){
            nextLock()
        } else if (id == R.id.action_log_out){
            alertLogOut()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun nextLock() {
        if (currentLock == "You have no lock") {
            toast("You have no locks")
            updateLockTitle()
        } else if (arrayListOfLocks.isNotEmpty()) {
            currentLock = when (arrayListOfLocks.size) {
                arrayListOfLocks.indexOf(currentLock)+1 -> {
                    arrayListOfLocks[0]
                }
                1 -> {
                    toast("You only have one lock")
                    arrayListOfLocks[0]
                }
                else -> {
                    arrayListOfLocks[arrayListOfLocks.indexOf(currentLock)+1]
                }
            }
            saveLockSelection(currentLock)
            updateLockTitle()
            userList.clear()
            ownerCheckAndAdapterUpdate()
        }
    }

    private fun saveLockSelection(currentLock: String) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("LOCK", currentLock)
        editor.apply()
        Log.i(TAG, currentLock)
        Log.i(TAG, sharedPreferences.getString("LOCK", "something not good").toString())
    }

    private fun resetSharedPreferences() {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.clear().apply()
    }

    private fun getLocks() {
        db.collection("locks")
            .whereArrayContains("owners", currentUser.uid)
            .get().addOnSuccessListener {result ->
                for (document in result){
                    arrayListOfLocks.add(document.id)
                    if (currentLock == "You have no lock"){
                        currentLock = document.id
                    }
                }
                db.collection("locks")
                    .whereArrayContains("guests", currentUser.uid)
                    .get().addOnSuccessListener { result ->
                        for (document in result) {
                            arrayListOfLocks.add(document.id)
                            if (currentLock == "You have no lock"){
                                currentLock = document.id
                            }
                        }
                        if (arrayListOfLocks.size == 1) {
                            saveLockSelection(arrayListOfLocks[0])
                            currentLock = arrayListOfLocks[0]
                            updateLockTitle()
                        }
                    }
            }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun updateLockTitle() {
        db.collection("locks").document(currentLock).get().addOnSuccessListener {document ->
            if (document != null && document.exists()) {
                Log.i(TAG, "hello " + document.data)
                lockTitle = document.data!!["title"].toString()
                textViewLockTitle.text = lockTitle
            } else {
                textViewLockTitle.text = "You have no lock"
            }
        }
    }

    private fun logOut() {
        auth.signOut()
        resetSharedPreferences()
        val intent = Intent(activity, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    private fun alertLogOut() {
        alertDialogBuilder.setTitle("Are you sure you want to log out?")
        alertDialogBuilder.setMessage("This will require you to use email and password next time you want to log in")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            logOut()
        }

        alertDialogBuilder.setNegativeButton("No") { _, _ ->
        }
        alertDialogBuilder.show()
    }

    private fun alertRemoveGuest(user: User) {
        alertDialogBuilder.setTitle("Are you sure you want to remove ${user.firstName} ${user.lastName}?")
        alertDialogBuilder.setMessage("This will make them unable to interact with the lock in any way but you can always add them again")
        alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
            removeGuest(user)
        }

        alertDialogBuilder.setNegativeButton("No") { _, _ ->
        }
        alertDialogBuilder.show()
    }
}






