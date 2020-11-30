package com.example.trilock.data.register_login.activities.home
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.trilock.R
import com.example.trilock.data.model.ui.lock.LockViewModel
import com.example.trilock.data.register_login.activities.AddLockActivity
import com.google.firebase.Timestamp.now
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LockFragment : Fragment() {

    private lateinit var lockViewModel: LockViewModel
    private lateinit var database: DatabaseReference

    var auth: FirebaseAuth = Firebase.auth

    private val PREFS_FILENAME = "SHARED_PREF"
    private lateinit var sharedPreferences: SharedPreferences

    lateinit var currentUser: FirebaseUser

    private val TAG = "LockFragment"
    private var lockImages = arrayOf(R.drawable.baseline_lock_24, R.drawable.baseline_lock_open_24)
    private var isLocked: Boolean = false
    private val db = Firebase.firestore
    private lateinit var currentLock: String
    private lateinit var imageViewLock: ImageView
    private lateinit var textViewLockStatus: TextView
    private lateinit var textViewLockTitle: TextView
    private var arrayListOfLocks: ArrayList<String> = ArrayList()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {
        lockViewModel =
                ViewModelProvider(this).get(LockViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_lock, container, false)
        textViewLockStatus = root.findViewById(R.id.text_lock_status)
        textViewLockTitle= root.findViewById(R.id.text_view_lock_title)
        imageViewLock = root.findViewById(R.id.image_view_lock)
        imageViewLock.isInvisible = true

        sharedPreferences = context?.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)!!
        currentLock = sharedPreferences.getString("LOCK", "You have no lock")!!
        updateLockTitle()
        currentUser = auth.currentUser!!
        database = Firebase.database.reference

        getLocks()

        currentLockStatus()

        // set on-click listener for ImageView
        imageViewLock.setOnClickListener {
            Log.i(TAG," imageView clicked")
            lockStatusChange()
            getUser()
            currentLockStatus()
            actuallyUnlockOrLockTheLock()
        }

        return root
    }

    private fun updateLockTitle() {
        db.collection("locks").document(currentLock).get().addOnSuccessListener {document ->
            if (document != null) {
                Log.i(TAG, "hello " + document.data)
                textViewLockTitle.text = document.data!!["title"].toString()
            }
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
        val id = item!!.itemId
        //handle item clicks
        if (id == R.id.action_add){
            val intent = Intent(context, AddLockActivity::class.java)
            startActivity(intent)
        }
        if (id == R.id.action_next){
            nextLock()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun actuallyUnlockOrLockTheLock(){
        val database = Firebase.database
        val myRef = database.getReference("locks/HUfT5rj0QTjE7FgyGhfu/isLocked")

        if(isLocked){
            myRef.setValue(1)
        } else {
            myRef.setValue(0)
        }

    }

    private fun getUser() {
        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            Log.i(TAG, "Got user from database")
            if(document!=null){
                addEvent()
            }
        }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
            }
    }

    private fun addEvent() {
        val todayAsTimestamp = now().toDate()
        val event = hashMapOf(
            "firstName" to "Bob",
            "locked" to isLocked.toString(),
            "timeStamp" to todayAsTimestamp.toString()
        )

        db.collection("locks")
            .document("HUfT5rj0QTjE7FgyGhfu")
            .collection("events")
            .add(event)
            .addOnSuccessListener { document ->
        }
    }

    private fun currentLockStatus() {
        val database = db.collection("locks").document("HUfT5rj0QTjE7FgyGhfu")
        database.get().addOnSuccessListener {document ->
            if (document.data!!["locked"] as Boolean) {
                isLocked = true
                Log.i(TAG," Lock is locked")
                imageViewLock.setImageResource(lockImages[0])
                lockViewModel.text.observe(viewLifecycleOwner, Observer { textViewLockStatus.text = getString(R.string.locked)})
            } else {
                isLocked = false
                Log.i(TAG," Lock is unlocked")
                imageViewLock.setImageResource(lockImages[1])
                lockViewModel.text.observe(viewLifecycleOwner, Observer { textViewLockStatus.text = getString(R.string.unlocked)})
            }
            imageViewLock.isInvisible = false
        }

    }

    private fun lockStatusChange() {

        val database = db.collection("locks").document("HUfT5rj0QTjE7FgyGhfu")

        if (isLocked) {
            isLocked = false
            val status = hashMapOf("locked" to false)
            database.set(status)
                .addOnSuccessListener { Log.d(TAG, "Lock is now unlocked") }
                .addOnFailureListener { e -> Log.w(TAG, "Error in locks document", e) }
        } else {
            isLocked = true
            val status = hashMapOf("locked" to true)
            database.set(status)
                .addOnSuccessListener { Log.d(TAG, "Lock is now locked") }
                .addOnFailureListener { e -> Log.w(TAG, "Error in locks document", e) }
        }
    }

    private fun saveLockSelection(currentLock: String) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("LOCK", currentLock)
        editor.apply()
        Log.i(TAG, currentLock)
    }

    private fun nextLock() {
        Log.i(TAG,""+arrayListOfLocks.indexOf(currentLock))
        Log.i(TAG, arrayListOfLocks[arrayListOfLocks.indexOf(currentLock)])

        if (arrayListOfLocks.indexOf(currentLock)+1 == arrayListOfLocks.size) {
            currentLock = arrayListOfLocks[0]
        } else {
            currentLock = arrayListOfLocks[arrayListOfLocks.indexOf(currentLock)+1]
        }

        saveLockSelection(currentLock)
        updateLockTitle()
    }

    private fun getLocks() {
        db.collection("locks")
            .whereArrayContains("owners", currentUser.uid)
            .get().addOnSuccessListener {result ->
                for (document in result){
                    arrayListOfLocks.add(document.id)
                }
            }
        db.collection("locks")
            .whereArrayContains("guests", currentUser.uid)
            .get().addOnSuccessListener { result ->
                for (document in result) {
                    arrayListOfLocks.add(document.id)
                }
            }
    }
}