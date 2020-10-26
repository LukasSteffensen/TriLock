package com.example.trilock.data.model.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.trilock.R

class LockFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    private val TAG = "LockFragment"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_lock, container, false)
        var textViewLockStatus: TextView = root.findViewById(R.id.text_lock_status)
        val imageViewLock: ImageView = root.findViewById(R.id.image_view_lock)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textViewLockStatus.text = "Locked"
        })

        // set on-click listener for ImageView
        imageViewLock.setOnClickListener {
            // your code here
            Log.i(TAG," imageView clicked")
            if (textViewLockStatus.text.toString() == "Locked") {
                Log.i(TAG," textView equal to Locked")

                homeViewModel.text.observe(viewLifecycleOwner, Observer { textViewLockStatus.text = "Unlocked"})
            } else {
                Log.i(TAG," textView not equal to Locked")
                homeViewModel.text.observe(viewLifecycleOwner, Observer { textViewLockStatus.text = "Locked"})
            }
        }

        return root
    }
}