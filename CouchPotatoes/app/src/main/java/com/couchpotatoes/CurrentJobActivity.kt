package com.couchpotatoes

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.couchpotatoes.classes.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CurrentJobActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_job)

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        val user = FirebaseAuth.getInstance().currentUser

        findViewById<LinearLayout>(R.id.detailsContainer).visibility = View.GONE
        findViewById<TextView>(R.id.emptyView).visibility = View.VISIBLE

        // First, get the currentJob for the user
        database.child("users").child(user!!.uid).child("currentJob").get().addOnSuccessListener { snapshot ->
            val currentJob = snapshot.value as? String
            if (currentJob != null) {
                // Now, get the job details using the currentJob
                database.child("jobs").child(currentJob).get().addOnSuccessListener { jobSnapshot ->
                    val job = jobSnapshot.getValue(Job::class.java)
                    findViewById<TextView>(R.id.requesterName).text = "Requester Name: " + job?.requesterName
                    findViewById<TextView>(R.id.requesterEmail).text = "Requester Email: " + job?.requesterEmail
                    findViewById<TextView>(R.id.item).text = "Item: " + job?.item
                    findViewById<TextView>(R.id.price).text = "Price: " + job?.price
                    findViewById<TextView>(R.id.store).text = "Store: " + job?.store
                    findViewById<TextView>(R.id.deliveryAddress).text = "Delivery Address: " + job?.deliveryAddress
                    findViewById<TextView>(R.id.status).text = "Status: " + job?.status
                    findViewById<LinearLayout>(R.id.detailsContainer).visibility = View.VISIBLE
                    findViewById<TextView>(R.id.emptyView).visibility = View.GONE
                }.addOnFailureListener {
                    findViewById<TextView>(R.id.emptyView).visibility = View.VISIBLE
                }
            } else {
                findViewById<TextView>(R.id.emptyView).visibility = View.VISIBLE
            }
        }.addOnFailureListener {
            findViewById<TextView>(R.id.emptyView).visibility = View.VISIBLE
        }
    }
}
