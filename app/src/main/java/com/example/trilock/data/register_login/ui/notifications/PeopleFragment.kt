package com.example.trilock.data.model.ui.people

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.trilock.R

class PeopleFragment : Fragment() {

    private lateinit var peopleViewModel: PeopleViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        peopleViewModel=
                ViewModelProvider(this).get(PeopleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_people, container, false)
        val textView: TextView = root.findViewById(R.id.text_people)
        peopleViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}