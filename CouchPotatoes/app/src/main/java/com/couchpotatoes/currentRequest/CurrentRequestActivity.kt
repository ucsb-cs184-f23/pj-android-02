package com.couchpotatoes.currentRequest

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchpotatoes.BaseActivity
import com.couchpotatoes.R
import com.couchpotatoes.classes.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CurrentRequestActivity () : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var currentRequestsRecyclerView: RecyclerView
    private lateinit var currentRequestsAdapter: CurrentRequestsAdapter
    private var requestsList = mutableListOf<Job>()
    private var currentRequestsIds = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_request)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        createNavMenu(R.id.my_toolbar, this, auth)

        val user = FirebaseAuth.getInstance().currentUser

        currentRequestsRecyclerView = findViewById(R.id.currentRequestsRecyclerView)
        currentRequestsRecyclerView.layoutManager = LinearLayoutManager(this)

        database.child("users").child(user!!.uid).child("currentRequests")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentRequestsIds = snapshot.value as? MutableList<String> ?: mutableListOf()
                    Log.d("currentRequestsIds", currentRequestsIds.toString())
                    fetchRequestDetails(currentRequestsIds)
                    fetchRequests(user?.email)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Listener was cancelled, error: ${error.toException()}")
                }
            })
    }

    private fun fetchRequestDetails(requestIds: List<String>) {
        requestsList.clear()
        for (requestId in requestIds) {
            database.child("jobs").child(requestId)
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    val request = dataSnapshot.getValue(Job::class.java)
                    if (request != null && request.status != "complete") {
                        requestsList.add(request)
                    }
                    currentRequestsAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseError", "Error fetching data", exception)
                }
        }
    }

    private fun fetchRequests(userEmail: String?) {
        Log.d("fetchJobs", currentRequestsIds.toString())
        currentRequestsAdapter = CurrentRequestsAdapter(requestsList, currentRequestsIds, userEmail, auth, database)
        currentRequestsRecyclerView.adapter = currentRequestsAdapter
    }



}