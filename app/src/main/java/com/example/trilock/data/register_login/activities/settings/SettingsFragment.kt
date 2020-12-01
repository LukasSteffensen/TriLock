package com.example.trilock.data.register_login.activities.settings

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private val PREFS_FILENAME = "SHARED_PREF"
    private lateinit var sharedPreferences: SharedPreferences
    private var isSwitched = false
    lateinit var mTextView : TextView
    private val db = Firebase.firestore
    var auth: FirebaseAuth = Firebase.auth

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
}