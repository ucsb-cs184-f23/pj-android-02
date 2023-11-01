package com.couchpotatoes.jobBoard

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.couchpotatoes.CurrentJobActivity
import com.couchpotatoes.R
import com.couchpotatoes.classes.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class JobItemActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var job: Job
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_item)
        job = intent.getSerializableExtra("job") as Job

        val arrayAdapter: ArrayAdapter<*>
        val users = arrayOf(
            "Requester: ${job.requesterName}", "Requester Email: ${job.requesterEmail}", "Item: ${job.item}",
            "Price: ${job.price}", "Store: ${job.store}", "Delivery Address: ${job.deliveryAddress}", "Status: ${job.status}"
        )

        // access the listView from xml file
        var mListView = findViewById<ListView>(R.id.job_list)
        arrayAdapter = ArrayAdapter(this,
            android.R.layout.simple_list_item_1, users)
        mListView.adapter = arrayAdapter
    }

    fun acceptButtonHandler(view: View) {
        // Change the job's status from "pending" to "accepted"
        database = Firebase.database.reference
        database.child("jobs").child(job.uid.toString()).child("status").setValue("accepted")

        // TODO: For now, accept button navigate to Job Board to accept new jobs (plus it shows that accepted jobs are no on the job board)
        //      We probably want it to navigate to current job or something
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        val user = FirebaseAuth.getInstance().currentUser

        database.child("users").child(user!!.uid).child("currentJob").setValue(job.uid.toString())

        val intent = Intent(this, CurrentJobActivity::class.java)
        startActivity(intent)
    }
}