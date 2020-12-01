package com.example.trilock.data.register_login.activities.notifications

import android.provider.Contacts
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView;
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView;
import com.example.trilock.R
import com.example.trilock.data.register_login.classes.User

class PeopleAdapter(var people: ArrayList<User>, val isOwner: Boolean) : RecyclerView.Adapter<PeopleAdapter.PeopleViewHolder>(){

    class PeopleViewHolder(peopleView: View) : RecyclerView.ViewHolder(peopleView) {

        val personTextView: TextView
        val switch: Switch
        val imageViewGear: ImageView
        val textViewGuestOrOwner: TextView

        init {
            personTextView = peopleView.findViewById(R.id.text_view_person)
            switch = peopleView.findViewById(R.id.switch_people)
            imageViewGear = peopleView.findViewById(R.id.image_view_people_settings)
            textViewGuestOrOwner = peopleView.findViewById(R.id.text_view_owner_or_guest)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        //return PeopleViewHolder(LayoutInflater.from(context).inflate(R.layout.list_people, parent, false))
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_people, parent, false)
        return PeopleViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        holder.personTextView.text = people[position].firstName.toString()
        if (people[position].isOwner as Boolean) {
            holder.textViewGuestOrOwner.text = "Owner"
        } else {
            holder.textViewGuestOrOwner.text = "Guest"
        }
        if (!isOwner) {
            holder.imageViewGear.isInvisible = true
            holder.switch.isInvisible = true
        }
    }

    override fun getItemCount(): Int {
        return people.size
    }

    fun update(userList: ArrayList<User>) {
        people = userList
        notifyDataSetChanged()
    }
}


