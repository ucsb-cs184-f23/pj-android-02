package com.couchpotatoes.currentJob

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color.rgb
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchpotatoes.BaseActivity
import com.couchpotatoes.R
import com.couchpotatoes.classes.Job
import com.couchpotatoes.classes.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CurrentJobActivity () : BaseActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private lateinit var currentJobsRecyclerView: RecyclerView
    private lateinit var currentJobsAdapter: CurrentJobsAdapter
    private var jobsList = mutableListOf<Job>()
    private var currentJobIds = mutableListOf<String>()

    private var CHANNEL_ID = "couch_potato_channel_id"
    private var NOTIFICATION_ID = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_job)

        createNotificationChannel()

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        createNavMenu(R.id.my_toolbar, this, auth)

        val user = FirebaseAuth.getInstance().currentUser

        currentJobsRecyclerView = findViewById(R.id.currentJobsRecyclerView)
        currentJobsRecyclerView.layoutManager = LinearLayoutManager(this)

        database.child("users").child(user!!.uid).child("currentJobs")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentJobIds = snapshot.value as? MutableList<String> ?: mutableListOf()
                    Log.d("currentJobIds", currentJobIds.toString())
                    fetchJobDetails(currentJobIds)
                    fetchJobs(user?.email)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("FirebaseError", "Listener was cancelled, error: ${error.toException()}")
                }
            })
        // Fetch jobs from Firebase and update jobsList
    }

    private fun fetchJobDetails(jobIds: List<String>) {
        jobsList.clear()
        for (jobId in jobIds) {
            database.child("jobs").child(jobId)
                .get()
                .addOnSuccessListener { dataSnapshot ->
                    val job = dataSnapshot.getValue(Job::class.java)
                    if (job != null && job.status != "complete") {
                        jobsList.add(job)
                    }
                    currentJobsAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { exception ->
                    Log.e("FirebaseError", "Error fetching data", exception)
                }
        }
    }

    private fun fetchJobs(userEmail: String?) {
        Log.d("fetchJobs", currentJobIds.toString())
        currentJobsAdapter = CurrentJobsAdapter(jobsList, currentJobIds, userEmail, auth, database, this::showNotification)
        currentJobsRecyclerView.adapter = currentJobsAdapter
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Couch Potato"
            val descriptionText = "Status changed"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(status: String) {
        var builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.potato_logo)
            .setContentTitle("Status Change")
            .setContentText(status)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(status))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with (NotificationManagerCompat.from(this)) {
            // notificationId is a unique int for each notification that you must define.
            if (ActivityCompat.checkSelfPermission(
                    this@CurrentJobActivity,
                    POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                requestPermissions(arrayOf(POST_NOTIFICATIONS), 100)
                Log.d("TAG", "no permission")
                if (ActivityCompat.checkSelfPermission(
                        this@CurrentJobActivity,
                        POST_NOTIFICATIONS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
