package com.example.trilock.data.model.ui.lock

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.trilock.R

class LockFragment : Fragment() {

    private lateinit var lockViewModel: LockViewModel

    private val TAG = "LockFragment"
    private var lockImages = arrayOf(R.drawable.baseline_lock_24, R.drawable.baseline_lock_open_24)

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {
        lockViewModel =
                ViewModelProvider(this).get(LockViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_lock, container, false)
        var textViewLockStatus: TextView = root.findViewById(R.id.text_lock_status)
        val imageViewLock: ImageView = root.findViewById(R.id.image_view_lock)
        lockViewModel.text.observe(viewLifecycleOwner, Observer {
            textViewLockStatus.text = getString(R.string.locked)
        })

        // set on-click listener for ImageView
        imageViewLock.setOnClickListener {
            // your code here
            Log.i(TAG," imageView clicked")
            if (textViewLockStatus.text.toString() == "Locked") {
                Log.i(TAG," textView equal to Locked")
                imageViewLock.setImageResource(lockImages[1])
                lockViewModel.text.observe(viewLifecycleOwner, Observer { textViewLockStatus.text = getString(R.string.unlocked)})
            } else {
                Log.i(TAG," textView not equal to Locked")
                imageViewLock.setImageResource(lockImages[0])
                lockViewModel.text.observe(viewLifecycleOwner, Observer { textViewLockStatus.text = getString(R.string.locked)})
            }
        }

        return root
    }
}