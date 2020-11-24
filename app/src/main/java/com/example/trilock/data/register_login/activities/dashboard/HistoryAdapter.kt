package com.example.trilock.data.register_login.activities.dashboard

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.trilock.R
import com.example.trilock.data.register_login.classes.Event

class HistoryAdapter(val history: ArrayList<Event>) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>(){

    class HistoryViewHolder(historyView: View) : RecyclerView.ViewHolder(historyView) {

        val historyTextView: TextView
        val timeStampTextView: TextView

        init {
            historyTextView = historyView.findViewById(R.id.text_view_history)
            timeStampTextView = historyView.findViewById(R.id.text_view_time_stamp)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.list_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.historyTextView.text = history[position].firstName
        holder.timeStampTextView.text = history[position].timeStamp
    }

    override fun getItemCount(): Int {
        return history.size
    }

}


