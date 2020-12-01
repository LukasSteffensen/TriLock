package com.example.trilock.data.register_login.activities.notifications

import android.provider.Contacts
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView;
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView;
import com.example.trilock.R
import com.example.trilock.data.register_login.classes.User

class PeopleAdapter(var people: ArrayList<User>,
                    private val itemClickListener: OnItemClickListener, var isOwner: Boolean) : RecyclerView.Adapter<PeopleAdapter.PeopleViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        //return PeopleViewHolder(LayoutInflater.from(context).inflate(R.layout.list_people, parent, false))
            val view = LayoutInflater.from(parent.context).inflate(R.layout.list_people, parent, false)
        return PeopleViewHolder(view)
    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        holder.personTextView.text = people[position].firstName.toString()
        holder.imageViewGear.isInvisible = !isOwner
        if (people[position].isOwner as Boolean) {
            holder.textViewGuestOrOwner.text = "Owner"
            holder.imageViewGear.isInvisible = true
        } else {
            holder.textViewGuestOrOwner.text = "Guest"
        }
        holder.bind(people[position], itemClickListener)
    }

    override fun getItemCount(): Int {
        return people.size
    }

    fun update(userList: ArrayList<User>, updateOwner: Boolean) {
        people = userList
        isOwner = updateOwner
        people.sortByDescending { isOwner.toString() }
        notifyDataSetChanged()
    }

    interface OnItemClickListener{
        fun onItemClicked(user: User)
    }

    class PeopleViewHolder(peopleView: View) : RecyclerView.ViewHolder(peopleView) {

        val personTextView: TextView = peopleView.findViewById(R.id.text_view_person)
        val imageViewGear: ImageView = peopleView.findViewById(R.id.image_view_people_settings)
        val textViewGuestOrOwner: TextView = peopleView.findViewById(R.id.text_view_owner_or_guest)

        fun bind(user: User,clickListener: PeopleAdapter.OnItemClickListener) {
            itemView.setOnClickListener {
                clickListener.onItemClicked(user)
            }
        }
    }

}




