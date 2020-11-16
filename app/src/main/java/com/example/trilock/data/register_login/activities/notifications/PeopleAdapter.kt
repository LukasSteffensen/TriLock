package com.example.trilock.data.register_login.activities.notifications

import android.content.Context
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.trilock.R

class PeopleAdapter(val context: Context, val PeopleList: ArrayList<People>) : RecyclerView.Adapter<PeopleAdapter.Holder>() {

    override fun onBindViewHolder(peopleHolder: Holder, position: Int) {
        peopleHolder?.bind(PeopleList[position], context)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_people, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return PeopleList.size
    }

    inner class Holder(view: View) : RecyclerView.ViewHolder(view!!) {
        val firstname = view?.findViewById<TextView>(R.id.text_view_person)

        fun bind(person: People, context: Context) {
            firstname?.text = person.firstname
        }
    }

//    class peopleViewHolder {
//        RecyclerView.ViewHolder
//    }

}
