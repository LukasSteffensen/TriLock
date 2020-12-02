package com.example.trilock.data.register_login.activities.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.trilock.R
import com.example.trilock.data.model.LoginActivity
import com.example.trilock.data.model.ui.settings.SettingsViewModel
import com.example.trilock.data.register_login.activities.AddLockActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SettingsFragment : Fragment() {

    private val TAG = "SettingsFragment"
    private lateinit var settingsViewModel: SettingsViewModel
    private val PREFS_FILENAME = "SHARED_PREF"
    private lateinit var sharedPreferences: SharedPreferences
    private var isSwitched = false
    lateinit var mTextView : TextView
    private val db = Firebase.firestore
    var auth: FirebaseAuth = Firebase.auth
    private lateinit var userUid: String
    private lateinit var alertDialogBuilder: AlertDialog.Builder


    @SuppressLint("UseSwitchCompatOrMaterialCode", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        settingsViewModel =
                ViewModelProvider(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)
        val textView: TextView = root.findViewById(R.id.text_settings)
        val switch: Switch = root.findViewById(R.id.switch_biometrics)
        mTextView = root.findViewById(R.id.text_view_name_setting)
        userUid = auth.uid.toString()

        setUserName()
        sharedPreferences = context?.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)!!
        isSwitched = sharedPreferences.getBoolean("SWITCH", false)
        Log.i("SettingsFragment: ", isSwitched.toString())
        if (isSwitched) {
            switch.isChecked = true
            switch.text = "Biometric authentication"
        }

        switch.setOnCheckedChangeListener { _, isSwitched ->
            saveBioAuth(isSwitched)
        }

        settingsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
    private fun saveBioAuth(isSwitched: Boolean) {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean("SWITCH", isSwitched)
        editor.apply()
        Log.i("SettingsFragment: ", "inside saveBioAuth fun$isSwitched")
    }
    private fun toast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun setUserName() {
        db.collection("users")
            .document(userUid)
            .get().addOnSuccessListener { document ->
                mTextView.text = document.data!!["firstName"].toString() + " " + document.data!!["lastName"].toString()
            }
    }

    private fun logOut() {
        auth.signOut()
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

    //enable options menu in this fragment
    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }
    //inflate the menu
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater!!.inflate(R.menu.options_menu_settings, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    //handle item clicks of menu
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //get item id to handle item clicks
        val id = item.itemId
        //handle item clicks
        if (id == R.id.action_add) {
            val intent = Intent(context, AddLockActivity::class.java)
            startActivity(intent)
        } else if (id == R.id.action_log_out) {
            alertLogOut()
        }

        return super.onOptionsItemSelected(item)
    }

}