package com.example.trilock.data.register_login.activities.notifications

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.trilock.R
import com.example.trilock.data.model.ui.people.PeopleViewModel
import com.example.trilock.data.register_login.classes.Encryption
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.auth.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.w3c.dom.Document
import kotlin.collections.mutableListOf as mutableListOf

class PeopleFragment : Fragment() {

    private lateinit var peopleViewModel: PeopleViewModel
    private lateinit var peopleRecyclerView: RecyclerView
    val db = Firebase.firestore
    private lateinit var adapter: PeopleAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        peopleViewModel =
            ViewModelProvider(this).get(PeopleViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_people, container, false)
        val rcl = inflater.inflate(R.layout.list_people, container,false)
        val peopleSwitch: Switch = rcl.findViewById(R.id.switch_people)

        peopleRecyclerView = root.findViewById(R.id.recyclerview_people)
        linearLayoutManager = LinearLayoutManager(context)
        peopleRecyclerView.layoutManager = linearLayoutManager

        dataFirestore()

        return root
    }

    private fun dataFirestore() {

        //Trying to collect first names from Database.
        val userList: ArrayList<String> = mutableListOf<String>() as ArrayList<String>
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                    for (document in result) {

                        val map = document.get("firstName") as Map <String, String>

                        val password = "Poop1234"

                        val encryptedString: String = map.get("encrypted") as String
                        val ivString: String = map.get("iv") as String
                        val saltString: String = map.get("salt") as String

                        val encrypted = Base64.decode(encryptedString, Base64.NO_WRAP)
                        val iv = Base64.decode(ivString, Base64.NO_WRAP)
                        val salt = Base64.decode(saltString, Base64.NO_WRAP)

                        val decrypted = Encryption().decrypt(
                            hashMapOf("iv" to iv, "salt" to salt, "encrypted" to encrypted), password)
                        val name: String = Base64.encodeToString(decrypted, Base64.NO_WRAP)

//                        val name = document.data["firstName"].toString()
                        userList.add(name)
                    }
                adapter = PeopleAdapter(userList, 1)
                peopleRecyclerView.adapter = adapter
                    Toast.makeText(context, "toast", Toast.LENGTH_SHORT).show()
                }

            .addOnFailureListener { exception ->
                Log.d("TAG", "get failed with ", exception)
                Toast.makeText(context, "Firestore not working", Toast.LENGTH_SHORT).show()
            }
    }
}





