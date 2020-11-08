package com.example.trilock.data.model.ui.people

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PeopleViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is people Fragment"
    }
    val text: LiveData<String> = _text
}