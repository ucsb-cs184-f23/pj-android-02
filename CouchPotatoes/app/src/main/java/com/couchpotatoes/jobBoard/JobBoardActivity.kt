package com.couchpotatoes.jobBoard

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchpotatoes.R
import com.couchpotatoes.classes.Job
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.Arrays

class JobBoardActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_board)

        var database: DatabaseReference = Firebase.database.reference
        val jobsRef = database.child("jobs")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // get number of jobs
                var jobList = ArrayList<Job>()
                for (ds in dataSnapshot.children) {
                    val job = Job()
                    for (data in ds.children) {
                        // unoptimized solution for now
                        if (data.key.toString() == "deliveryAddress") {
                            job.deliveryAddress = data.value.toString()
                        }

                        if (data.key.toString() == "status") {
                            job.status = data.value.toString()
                        }

                        if (data.key.toString() == "requesterName") {
                            job.requesterName = data.value.toString()
                        }

                        if (data.key.toString() == "requesterEmail") {
                            job.requesterEmail = data.value.toString()
                        }

                        if (data.key.toString() == "item") {
                            job.item = data.value.toString()
                        }

                        if (data.key.toString() == "price") {
                            job.price = data.value.toString()
                        }
                        if (data.key.toString() == "store") {
                            job.store = data.value.toString()
                        }

                        if (data.key.toString() == "uid") {
                            job.uid = data.value.toString()
                        }
                    }
                    jobList.add(job)
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