package com.example.trilock.data.model.ui.lock

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColor
import androidx.core.view.isInvisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.trilock.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LockFragment : Fragment() {

    private lateinit var lockViewModel: LockViewModel

    private val TAG = "LockFragment"
    private var lockImages = arrayOf(R.drawable.baseline_lock_24, R.drawable.baseline_lock_open_24)
    private var isLocked: Boolean = false
    private val db = Firebase.firestore
    private lateinit var imageViewLock: ImageView
    private lateinit var textViewLockStatus: TextView

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {
        lockViewModel =
                ViewModelProvider(this).get(LockViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_lock, container, false)
        textViewLockStatus = root.findViewById(R.id.text_lock_status)
        imageViewLock = root.findViewById(R.id.image_view_lock)
        imageViewLock.isInvisible = true

        currentLockStatus()

        // set on-click listener for ImageView
        imageViewLock.setOnClickListener {
            Log.i(TAG," imageView clicked")
            lockStatusChange()
            currentLockStatus()
        }

        return root
    }

    private fun currentLockStatus() {
        val database = db.collection("locks").document("HUfT5rj0QTjE7FgyGhfu")
        database.get().addOnSuccessListener {document ->
            if (document.data!!["locked"].toString() == "true") {
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

}