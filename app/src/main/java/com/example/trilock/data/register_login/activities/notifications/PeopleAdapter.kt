package com.example.trilock.data.register_login.activities.notifications

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.trilock.R

class PeopleAdapter(val people: ArrayList<String>) : RecyclerView.Adapter<PeopleAdapter.PeopleViewHolder>(){

    class PeopleViewHolder(peopleView: View) : RecyclerView.ViewHolder(peopleView) {

        val personTextView: TextView

        init {
            personTextView = peopleView.findViewById(R.id.text_view_person)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_people, parent, false)
        return PeopleViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        holder.personTextView.text = people[position]
    }

    override fun getItemCount(): Int {
        return people.size
    }

}


