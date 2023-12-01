package com.couchpotatoes

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Color.rgb
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.couchpotatoes.classes.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CurrentJobActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_job)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        createNavMenu(R.id.my_toolbar, this, auth)

        val user = FirebaseAuth.getInstance().currentUser

        val detailsCard = findViewById<CardView>(R.id.detailsCard)
        detailsCard.visibility = View.GONE
        val emptyView = findViewById<TextView>(R.id.emptyView)
        emptyView.visibility = View.VISIBLE

        database.child("users").child(user!!.uid).child("currentJob").get()
            .addOnSuccessListener { snapshot ->
                val currentJobId = snapshot.value as? String
                // Attach a listener to read the data at our posts reference
                database.child("jobs").child(currentJobId.toString())
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            Log.d("TAG", dataSnapshot.toString())
                            val job = dataSnapshot.getValue<Job>()
                            Log.d("TAG", job.toString())
                                if (job == null || job.status == "complete") {
                                    detailsCard.visibility = View.GONE
                                    emptyView.text = "You have nothing to do"
                                } else {
                                    emptyView.visibility = View.GONE
                                    detailsCard.visibility = View.VISIBLE
                                    findViewById<TextView>(R.id.requesterName).text =
                                        job?.requesterName
                                    findViewById<TextView>(R.id.requesterEmail).text =
                                        job?.requesterEmail
                                    findViewById<TextView>(R.id.item).text = job?.item
                                    findViewById<TextView>(R.id.price).text = job?.price
                                    findViewById<TextView>(R.id.store).text = job?.store
                                    findViewById<TextView>(R.id.deliveryAddress).text =
                                        job?.deliveryAddress
                                    findViewById<TextView>(R.id.status).text = job?.status
                                    val cancelButton = findViewById<Button>(R.id.cancel)
                                    val completeButton = findViewById<Button>(R.id.complete)


                                    when (job?.status) {
                                        "accepted" -> {
                                            completeButton.text = "Gather"
                                        }

                                        "gathering" -> {
                                            completeButton.text = "Deliver"
                                        }

                                        "delivering" -> {
                                            completeButton.text = "Complete"
                                        }
                                    }

                                    val dialogBuilder = AlertDialog.Builder(this@CurrentJobActivity)

                                    if (user.email == job.requesterEmail) { // You are the requester

                                        cancelButton.setOnClickListener {
                                            dialogBuilder.setMessage("Are you sure you want to cancel this job?")
                                                .setPositiveButton("Yes") { _, _ ->
                                                    database.child("jobs")
                                                        .child(currentJobId.toString())
                                                        .removeValue()
                                                    database.child("users").child(user!!.uid)
                                                        .child("currentJob").setValue(null)
                                                }
                                                .setNegativeButton("No", null)
                                                .show()
                                        }

                                        completeButton.setOnClickListener {
                                            dialogBuilder.setMessage("Are you sure you want to mark this job as complete?")
                                                .setPositiveButton("Yes") { _, _ ->
                                                    database.child("jobs")
                                                        .child(currentJobId.toString())
                                                        .child("status")
                                                        .setValue("complete")
                                                    database.child("users").child(user!!.uid)
                                                        .child("currentJob").setValue(null)
                                                }
                                                .setNegativeButton("No", null)
                                                .show()
                                        }




                                    } else { // You are the dasher
                                        // Cancel Button
                                        cancelButton.setOnClickListener {
                                            dialogBuilder.setMessage("Are you sure you want to cancel this job?")
                                                .setPositiveButton("Yes") { _, _ ->
                                                    database.child("users").child(user!!.uid)
                                                        .child("currentJob").setValue(null)
                                                    database.child("jobs")
                                                        .child(currentJobId.toString())
                                                        .child("status")
                                                        .setValue("pending")
                                                    recreate()
                                                }
                                                .setNegativeButton("No", null)
                                                .show()
                                        }

                                        if (job?.status == "accepted") {
                                            // Gathering Button
                                            completeButton.setBackgroundColor(rgb(0,0x66,0x66))
                                            completeButton.setOnClickListener {
                                                dialogBuilder.setMessage("Are you sure you want to mark this job as gathering?")
                                                    .setPositiveButton("Yes") { _, _ ->
                                                        database.child("jobs")
                                                            .child(currentJobId.toString())
                                                            .child("status")
                                                            .setValue("gathering")
                                                    }
                                                    .setNegativeButton("No", null)
                                                    .show()
                                            }
                                        }

                                        if (job?.status == "gathering") {
                                            // Delivering Button
                                            completeButton.setBackgroundColor(rgb(0xC4,0x50,0x25))
                                            completeButton.setOnClickListener {
                                                dialogBuilder.setMessage("Are you sure you want to mark this job as delivering?")
                                                    .setPositiveButton("Yes") { _, _ ->
                                                        database.child("jobs")
                                                            .child(currentJobId.toString())
                                                            .child("status")
                                                            .setValue("delivering")
                                                    }
                                                    .setNegativeButton("No", null)
                                                    .show()
                                            }
                                        }

                                        if (job?.status == "delivering") {
                                            // Complete Button
                                            completeButton.setBackgroundColor(rgb(0xEE,0x99,0x39))
                                            completeButton.setOnClickListener {
                                                dialogBuilder.setMessage("Are you sure you want to mark this job as complete?")
                                                    .setPositiveButton("Yes") { _, _ ->
                                                        database.child("jobs")
                                                            .child(currentJobId.toString())
                                                            .child("status")
                                                            .setValue("complete")
                                                        database.child("users").child(user!!.uid)
                                                            .child("currentJob").setValue(null)
                                                        recreate()
                                                    }
                                                    .setNegativeButton("No", null)
                                                    .show()
                                            }
                                        }
                                    }
                                }
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                            println("The read failed: " + databaseError.code)
                        }
                    })
            }
    }
}
