package com.couchpotatoes.jobBoard

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.couchpotatoes.R
import com.couchpotatoes.classes.Job
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.ArrayList


class JobItemActivity : AppCompatActivity() {
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
        database = Firebase.database.reference
//        Log.i("TAG", "uid: ${job.uid}")
        database.child("jobs").child(job.uid.toString()).child("status").setValue("accepted")
    }
}