package com.example.trilock.data.register_login.activities.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.trilock.R
import com.example.trilock.data.register_login.classes.Event
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HistoryAdapter(val history: ArrayList<Event>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>(){

    class HistoryViewHolder(historyView: View) : RecyclerView.ViewHolder(historyView) {

        val historyTextView: TextView
        val timeStampTextView: TextView
        val statusImageView: ImageView

        init {
            historyTextView = historyView.findViewById(R.id.text_view_history)
            timeStampTextView = historyView.findViewById(R.id.text_view_time_stamp)
            statusImageView = historyView.findViewById(R.id.image_view_lock)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.list_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.timeStampTextView.text = history[position].timeStamp
        if(history[position].isLocked) {
            holder.statusImageView.setImageResource(R.drawable.baseline_lock_24)
            holder.historyTextView.text = history[position].firstName + " has locked the TriLock"

        } else {
            holder.statusImageView.setImageResource(R.drawable.baseline_lock_open_24)
            holder.historyTextView.text = history[position].firstName + " has unlocked the TriLock"
        }
    }

    override fun getItemCount(): Int {
        return history.size
    }
}

