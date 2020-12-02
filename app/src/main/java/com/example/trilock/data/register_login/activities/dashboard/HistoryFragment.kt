package com.example.trilock.data.register_login.activities.dashboard

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trilock.R
import com.example.trilock.data.model.LoginActivity
import com.example.trilock.data.model.ui.history.HistoryViewModel
import com.example.trilock.data.register_login.activities.AddLockActivity
import com.example.trilock.data.register_login.classes.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryFragment : Fragment() {

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyRecyclerView: RecyclerView
    val db = Firebase.firestore
    private val TAG = "HistoryFragment"
    private var arrayListOfLocks: ArrayList<String> = ArrayList()
    private lateinit var adapter: HistoryAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    var auth: FirebaseAuth = Firebase.auth

    private val PREFS_FILENAME = "SHARED_PREF"
    lateinit var currentUser: FirebaseUser
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currentLockID : String
    private lateinit var lockTitleTextView : TextView
    private lateinit var alertDialogBuilder: AlertDialog.Builder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        historyViewModel =
            ViewModelProvider(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        historyRecyclerView = root.findViewById(R.id.recyclerview_history)
        alertDialogBuilder = AlertDialog.Builder(context)
        linearLayoutManager = LinearLayoutManager(context)
        historyRecyclerView.layoutManager = linearLayoutManager

        lockTitleTextView = root.findViewById(R.id.text_view_history_lock_title)

        sharedPreferences = context?.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)!!
        currentLockID = sharedPreferences.getString("LOCK", "You have no lock")!!

        dataFirestore(currentLockID)
        setLockTitle(currentLockID)
        currentUser = auth.currentUser!!

        getLocks()

        return root
    }

    private fun nextLock() {
        if (currentLockID == "You have no lock") {
            toast("You have no locks")
        } else {
            Log.i(TAG,""+arrayListOfLocks.indexOf(currentLockID))
            currentLockID = if (arrayListOfLocks.indexOf(currentLockID)+1 == arrayListOfLocks.size) {
                toast("You only have one lock")
                arrayListOfLocks[0]
            } else {
                arrayListOfLocks[arrayListOfLocks.indexOf(currentLockID)+1]
            }

            saveLockSelection(currentLockID)

            updateLockTitle()
        }
    }

    private fun dataFirestore(lockID : String) {
        val eventList: ArrayList<Event> = ArrayList()
        db.collection("locks").document(lockID).collection("events").orderBy("timeStamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val Event = Event(
                        document.data["firstName"].toString(),
                        document.data["timeStamp"].toString(),
                        document.data["locked"].toString() == "true")
                    eventList.add(Event)
                }
                adapter = HistoryAdapter(eventList)
                historyRecyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
                Toast.makeText(context, "Firestore not working", Toast.LENGTH_SHORT).show()
            }
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

    private fun setLockTitle(lockID: String) {
        db.collection("locks").document(lockID).get().addOnSuccessListener { document ->
            if(document != null && document.exists()){
                lockTitleTextView.text = document["title"].toString()
            }
        }
    }

    private fun saveLockSelection(currentLockID: String) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("LOCK", currentLockID)
        editor.apply()
        Log.i(TAG, currentLockID)
        Log.i(TAG, sharedPreferences.getString("LOCK", "something not good").toString())
    }

    private fun getLocks() {
        db.collection("locks")
            .whereArrayContains("owners", currentUser.uid)
            .get().addOnSuccessListener {result ->
                for (document in result){
                    arrayListOfLocks.add(document.id)
                    if (currentLockID == "You have no lock"){
                        currentLockID = document.id
                    }
                }
                db.collection("locks")
                    .whereArrayContains("guests", currentUser.uid)
                    .get().addOnSuccessListener { result ->
                        for (document in result) {
                            arrayListOfLocks.add(document.id)
                            if (currentLockID == "You have no lock"){
                                currentLockID = document.id
                            }
                        }
                        if (arrayListOfLocks.size == 1) {
                            saveLockSelection(arrayListOfLocks[0])
                            currentLockID = arrayListOfLocks[0]
                            updateLockTitle()
                        }
                    }
            }
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun updateLockTitle() {
        db.collection("locks").document(currentLockID).get().addOnSuccessListener {document ->
            if (document != null && document.exists()) {
                Log.i(TAG, "hello " + document.data)
                lockTitleTextView.text = document.data!!["title"].toString()
            } else {
                lockTitleTextView.text = "You have no lock"
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

    private fun resetSharedPreferences() {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.clear().apply()
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
}