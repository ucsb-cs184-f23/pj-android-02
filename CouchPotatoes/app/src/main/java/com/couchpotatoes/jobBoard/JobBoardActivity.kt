package com.couchpotatoes.jobBoard

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
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
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class JobBoardActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_board)

        var database: DatabaseReference = Firebase.database.reference
        var auth: FirebaseAuth = FirebaseAuth.getInstance()

        createNavMenu(R.id.my_toolbar, this, auth)

        val jobsRef = database.child("jobs")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // get number of jobs
                var jobList = ArrayList<Job>()
                for (ds in dataSnapshot.children) {
                    val job = Job()
                    if (ds.child("status").getValue<String?>().toString() == "pending") {
                        // Pass job details to Job Board Adapter to create the Job Board List
                        job.deliveryAddress = ds.child("deliveryAddress").getValue<String?>().toString()
                        job.status = ds.child("status").getValue<String?>().toString()
                        job.requesterName = ds.child("requesterName").getValue<String?>().toString()
                        job.requesterEmail = ds.child("requesterEmail").getValue<String?>().toString()
                        job.item = ds.child("item").getValue<String?>().toString()
                        job.price = ds.child("price").getValue<String?>().toString()
                        job.store = ds.child("store").getValue<String?>().toString()
                        job.uid = ds.child("uid").getValue<String?>().toString()
                        jobList.add(job)
                    }
                }

                val recycleView = findViewById<RecyclerView>(R.id.job_recycler_view)
                recycleView.layoutManager = LinearLayoutManager(this@JobBoardActivity)
                recycleView.adapter = JobBoardAdapter(jobList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        jobsRef.addValueEventListener(postListener)
    }
}