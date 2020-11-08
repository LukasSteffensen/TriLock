package com.example.trilock.data.model.ui.people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trilock.R
//import com.example.trilock.data.register_login.ui.notifications.PeopleRecyclerAdapter

class PeopleFragment : Fragment() {

    private lateinit var peopleViewModel: PeopleViewModel
    private lateinit var peopleViewAdapter: RecyclerView.Adapter<*>
    private lateinit var peopleViewManager: RecyclerView.LayoutManager
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        peopleViewModel=
                ViewModelProvider(this).get(PeopleViewModel::class.java)
//        linearLayoutManager = LinearLayoutManager(this)
//        peopleRecyclerView.layoutManager = linearLayoutManager
        val root = inflater.inflate(R.layout.fragment_people, container, false)
        val textView: TextView = root.findViewById(R.id.text_people)
        peopleViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root

 //       peopleViewManager = LinearLayoutManager(this)
//        peopleViewAdapter = PeopleRecyclerAdapter(peopleDataset)


    }
}