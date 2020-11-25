package com.example.trilock.data.model.ui.history

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trilock.R
import com.example.trilock.data.register_login.activities.dashboard.HistoryAdapter
import com.example.trilock.data.register_login.classes.Event
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryFragment : Fragment() {

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var historyRecyclerView: RecyclerView
    val db = Firebase.firestore
    private lateinit var adapter: HistoryAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        historyViewModel =
                ViewModelProvider(this).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        historyRecyclerView = root.findViewById(R.id.recyclerview_history)
        linearLayoutManager = LinearLayoutManager(context)
        historyRecyclerView.layoutManager = linearLayoutManager

        dataFirestore()


        return root
    }

    private fun dataFirestore() {

        //Trying to collect firstnames from Database.
        val eventList: ArrayList<Event> = ArrayList()
        db.collection("locks").document("HUfT5rj0QTjE7FgyGhfu").collection("events").get().addOnSuccessListener { result ->
            for (document in result) {
                val Event = Event(document.data["firstName"].toString(), document.data["timeStamp"].toString(),
                    document.data["isLocked"].toString() == "true")
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
}