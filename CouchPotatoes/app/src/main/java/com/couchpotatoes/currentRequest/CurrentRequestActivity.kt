package com.couchpotatoes.currentRequest

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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

    private lateinit var emptyView : TextView

    private var requestsList = mutableListOf<Job>()
    private var currentRequestsIds = mutableListOf<String>()

    private var CHANNEL_ID = "couch_potato_channel_id"
    private var NOTIFICATION_ID = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_request)

        createNotificationChannel()

        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference

        createNavMenu(R.id.my_toolbar, this, auth)

        val user = FirebaseAuth.getInstance().currentUser

        currentRequestsRecyclerView = findViewById(R.id.currentRequestsRecyclerView)
        currentRequestsRecyclerView.layoutManager = LinearLayoutManager(this)

        emptyView = findViewById<TextView>(R.id.empty_view)

        database.child("users").child(user!!.uid).child("currentRequests")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentRequestsIds = snapshot.value as? MutableList<String> ?: mutableListOf()

                    if (currentRequestsIds.isNullOrEmpty()) {
                        Log.e("TESTING", "no current requests")
                        currentRequestsRecyclerView.visibility = View.GONE;
                        emptyView.visibility = View.VISIBLE;
                    }
                    else {
                        currentRequestsRecyclerView.visibility = View.VISIBLE;
                        emptyView.visibility = View.GONE;
                    }

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
        val uniqueRequestsIds = requestIds.toSet()
        requestsList.clear()
        uniqueRequestsIds.forEach { requestId ->
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

    private fun onDataChange(snapshot: DataSnapshot) {
        // Keep it as a list but remove duplicates by converting to set and back
        currentRequestsIds = snapshot.children.mapNotNull { it.key }.toSet().toMutableList()
        fetchRequestDetails(currentRequestsIds)
        // Other logic remains the same
    }

    private fun fetchRequests(userEmail: String?) {
        Log.d("fetchJobs", currentRequestsIds.toString())
        currentRequestsAdapter = CurrentRequestsAdapter(requestsList, currentRequestsIds, userEmail, auth, database, this::showNotification)
        currentRequestsRecyclerView.adapter = currentRequestsAdapter
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
            if (ActivityCompat.checkSelfPermission(
                    this@CurrentRequestActivity,
                    POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(POST_NOTIFICATIONS), 100)
                Log.d("TAG", "no permission")
                if (ActivityCompat.checkSelfPermission(
                        this@CurrentRequestActivity,
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
