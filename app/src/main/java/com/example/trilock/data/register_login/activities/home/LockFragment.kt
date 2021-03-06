package com.example.trilock.data.register_login.activities.home
import android.app.AlertDialog
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
import androidx.lifecycle.ViewModelProvider
import com.example.trilock.R
import com.example.trilock.data.model.LoginActivity
import com.example.trilock.data.model.ui.lock.LockViewModel
import com.example.trilock.data.register_login.DiffieHellman
import com.example.trilock.data.register_login.activities.AddLockActivity
import com.google.firebase.Timestamp.now
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.collections.ArrayList

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
    private lateinit var userFirstName: String
    private lateinit var imageViewLock: ImageView
    private lateinit var textViewLockStatus: TextView
    private lateinit var textViewLockTitle: TextView
    private var arrayListOfLocks: ArrayList<String> = ArrayList()
    private lateinit var alertDialogBuilder: AlertDialog.Builder

    val g: BigInteger = 2.toBigInteger()
    private val hexStr: String = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C662" +
            "8B80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF" +
            "9519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7E" +
            "C6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C" +
            "4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163F" +
            "A8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670" +
            "C354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E8603" +
            "9B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956" +
            "AE515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF"
    //    public val p: BigInteger = 2^2048−2^1984−1+264*((21918*pi)+124476)
    var p: BigInteger = hexStr.toBigInteger(radix = 16)

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

        alertDialogBuilder = AlertDialog.Builder(context)

        currentUser = auth.currentUser!!
        database = Firebase.database.reference

        Log.i("HASH!", hashString("Hello SHA 256 from ESP32learning", "SHA-256"))

        getLocks()

        // set on-click listener for ImageView
        imageViewLock.setOnClickListener {
            Log.i(TAG," imageView clicked")
            changeLockStatus()
            addEventWithUser(currentLock)
            currentLockStatus(currentLock)
            lockOrUnlock(currentLock)
            imageViewLock.isClickable = false
        }

        diffieTest()

        return root
    }

    private fun updateLockTitle() {
        db.collection("locks").document(currentLock).get().addOnSuccessListener {document ->
            if (document != null && document.exists()) {
                Log.i(TAG, "hello " + document.data)
                textViewLockTitle.text = document.data!!["title"].toString()
            } else {
                textViewLockTitle.text = "You have no lock"
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

    private fun lockOrUnlock(lockID: String){
        val database = Firebase.database
        val lockCommandRef = database.getReference("/locks/$lockID/shouldLock")
        if(isLocked){
            lockCommandRef.setValue(1).addOnFailureListener {
                toast("Not able to connect to the lock, please check you internet connection")
                Log.i(TAG, it.stackTrace.toString())
            }
        } else {
            lockCommandRef.setValue(0).addOnFailureListener {
                toast("Not able to connect to the lock, please check you internet connection")
                Log.i(TAG, it.stackTrace.toString())
            }
        }
    }

    private fun addEventWithUser(lockID: String) {
        db.collection("users").document(currentUser.uid).get().addOnSuccessListener { document ->
            Log.i(TAG, "Got user from database")
            if(document!=null){
                userFirstName = document["firstName"] as String
                val todayAsTimestamp = now().toDate()
                val event = hashMapOf(
                    "firstName" to userFirstName,
                    "locked" to isLocked.toString(),
                    "timeStamp" to todayAsTimestamp.toString()
                )
                db.collection("locks")
                    .document(lockID)
                    .collection("events")
                    .add(event)
                    .addOnSuccessListener { document ->
                    }
            }
        }
            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
            }
    }

    private fun currentLockStatus(lockID : String) {
        val database = Firebase.database
        val myRef = database.getReference("locks/$lockID/status")
        val statusListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.value.toString() == "0") {
                    imageViewLock.setImageResource(lockImages[1])
                    textViewLockStatus.text = "Unlocked"
                    imageViewLock.isClickable = true
                } else {
                    imageViewLock.setImageResource(lockImages[0])
                    textViewLockStatus.text = "Locked"
                    imageViewLock.isClickable = true
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.i(TAG, databaseError.message)
            }
        }
        myRef.addValueEventListener(statusListener)
        imageViewLock.isInvisible = false
    }

    private fun changeLockStatus() {
        val database = db.collection("locks").document("HUfT5rj0QTjE7FgyGhfu")
        if (isLocked) {
            isLocked = false
            val status = hashMapOf("locked" to false)
            database.update(status as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d(TAG, "Lock is now unlocked")
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error in locks document", e) }
        } else {
            isLocked = true
            val status = hashMapOf("locked" to true)
            database.update(status as Map<String, Any>)
                .addOnSuccessListener {
                    Log.d(TAG, "Lock is now locked")
                }
                .addOnFailureListener { e -> Log.w(TAG, "Error in locks document", e) }
        }
    }

    private fun saveLockSelection(currentLock: String) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putString("LOCK", currentLock)
        editor.apply()
        Log.i(TAG, currentLock)
        Log.i(TAG, sharedPreferences.getString("LOCK", "something not good").toString())
    }

    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private fun nextLock() {
        if (currentLock == "You have no lock") {
            toast("You have no locks")
        } else if (arrayListOfLocks.isNotEmpty()){
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
            currentLockStatus(currentLock)
        }
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
                updateLockTitle()
                currentLockStatus(currentLock)
                saveLockSelection(currentLock)
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

    private fun diffieTest() {
        val alicePrivateKey = DiffieHellman.privateKey(p)
        val alicePublicKey = DiffieHellman.publicKey(p,g,alicePrivateKey)

        Log.i("alicePrivateKey", alicePrivateKey.toString())
        Log.i("alicePublicKey", alicePublicKey.toString())

        val bobPrivateKey = DiffieHellman.privateKey(p)
        val bobPublicKey = DiffieHellman.publicKey(p,g,bobPrivateKey)

        Log.i("bobPrivateKey", bobPrivateKey.toString())
        Log.i("bobPublicKey", bobPublicKey.toString())

        val aliceSecretKey = DiffieHellman.secret(p,bobPublicKey,alicePrivateKey)
        val bobSecretKey = DiffieHellman.secret(p,alicePublicKey,bobPrivateKey)

        Log.i("aliceSecretKey", aliceSecretKey.toString())
        Log.i("bobSecretKey", bobSecretKey.toString())

        if (aliceSecretKey == bobSecretKey) {
            Log.i("Yay", "The shared secret key is the same for Alice and Bob")
//            toast("DIFFIE HELLMAN WORKS")
//            Encryption.main(hashString(aliceSecretKey.toString(), "MD5"))
//            Log.i("MD5", hashString(aliceSecretKey.toString(), "MD5"))
        }
    }

    private fun hashString(input: String, algorithm: String): String    {
        return MessageDigest.getInstance(algorithm)
            .digest(input.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }
}