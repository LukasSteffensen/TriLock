package com.example.trilock.data.model.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.trilock.R

class SettingsFragment : Fragment() {

    private lateinit var settingsViewModel: SettingsViewModel

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
        switch.setOnCheckedChangeListener { compoundButton, isSwitched ->
            if (isSwitched){
                switch.setText("Turn off biometric authentication")
            } else {
                switch.setText("Turn on biometric authentication")
            }
        }
        settingsViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}