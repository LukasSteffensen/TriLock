package com.example.trilock.data.model.ui.people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trilock.R
import com.example.trilock.data.register_login.activities.notifications.People
import com.google.firebase.database.*

//import com.example.trilock.data.register_login.ui.notifications.PeopleRecyclerAdapter

class PeopleFragment : Fragment() {

    private lateinit var peopleViewModel: PeopleViewModel
    private var peopleRecyclerView: RecyclerView? = null
    lateinit var nameTextView: TextView
    private lateinit var peopleViewAdapter: RecyclerView.Adapter<*>
    private lateinit var peopleViewManager: RecyclerView.LayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var mDatabase: DatabaseReference
    private var fragmentView: View? = null
    var firebaseDatabase: FirebaseDatabase? = null
    private var PeopleList: ArrayList<People>? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        peopleViewModel =
                ViewModelProvider(this).get(PeopleViewModel::class.java)
//        linearLayoutManager = LinearLayoutManager(activity)
//        peopleRecyclerView.layoutManager = linearLayoutManager
//        val root = inflater.inflate(R.layout.fragment_people, container, false)
        val textView: TextView? = fragmentView?.findViewById(R.id.text_people)

        fragmentView = LayoutInflater.from(activity).inflate(R.layout.fragment_people, container, false)

        firebaseDatabase = FirebaseDatabase.getInstance()

        mDatabase = FirebaseDatabase.getInstance().getReference("users")
//        logRecyclerView()

        peopleRecyclerView = fragmentView?.findViewById(R.id.recyclerview_people)
        peopleRecyclerView?.setHasFixedSize(true)
        peopleRecyclerView?.layoutManager = LinearLayoutManager(context)
        peopleRecyclerView?.itemAnimator = DefaultItemAnimator()




        //peopleRecyclerView?.adapter = PeopleAdapter(PeopleList!!, R.layout.list_people)



        peopleViewModel.text.observe(viewLifecycleOwner, Observer {
            textView?.text = it
        }        )
        return fragmentView

    }

    inner class PeopleAdapter(val people: List<People>, val peopleLayout: Int) : RecyclerView.Adapter<PeopleFragment.PeopleViewHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(peopleLayout, parent, false)
            return PeopleViewHolder(view)
        }

        override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
            val people = people.get(position)
            holder.updatePeople(people)
        }

        override fun getItemCount(): Int {
            return people.size
        }

    }

    inner class PeopleViewHolder(peopleView: View?) : RecyclerView.ViewHolder(peopleView!!) {

        private var peopleTextView: TextView? = peopleView?.findViewById(R.id.text_view_person)

        fun updatePeople (event: People) {
            peopleTextView!!.text = event.toString()
        }
    }

    override fun onStart() {
        super.onStart()
    }
}

/*    private fun showPeople(people: List<People>){
        peopleRecyclerView.layoutManager = LinearLayoutManager(activity)
        peopleRecyclerView.adapter = (peopleViewAdapter)
*/

/*    private fun logRecyclerView(){
        var FirebaseRecyclerAdapter = object: FirebaseRecyclerAdapter<People, PeopleViewModel>() {

            People::class.java,
            R.layout.list_people,
            PeopleViewHolder::class.java,
            mDatabase

        }{
            override fun populateViewHolder(viewHolder: PeopleViewHolder, model: People, position: Int) {
                viewHolder.peopleView.firstName.setText(model.firstname)
                viewHolder.peopleView.lastName.setText(model.lastname)
            }


        }

*/



