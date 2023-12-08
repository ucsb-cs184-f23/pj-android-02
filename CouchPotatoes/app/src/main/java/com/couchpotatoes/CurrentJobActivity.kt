package com.couchpotatoes

import android.Manifest.permission.POST_NOTIFICATIONS
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color.rgb
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.couchpotatoes.classes.Job
import com.google.android.material.bottomsheet.BottomSheetDialog
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
                                    Log.d("TAG", job?.status.toString())
                                    if (job != null && user.email == job.requesterEmail) {
                                        showNotification("Request has been completed")
                                    }
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

                                        // Request Notification Permissions
                                        if (ActivityCompat.checkSelfPermission(
                                                this@CurrentJobActivity,
                                                POST_NOTIFICATIONS
                                            ) != PackageManager.PERMISSION_GRANTED
                                        ) {
                                            requestPermissions(arrayOf(POST_NOTIFICATIONS), 100)
                                        }

                                        // Send notification based on changed status
                                        when (job?.status) {
                                            "accepted" -> {
                                                showNotification("Your request has been accepted")
                                            }

                                            "gathering" -> {
                                                showNotification("Your hustler is now gathering")
                                            }

                                            "delivering" -> {
                                                showNotification("Your hustler is now delivering")
                                            }
                                        }
                                    } else { // You are the dasher
                                        // Cancel Button

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
                                                        showBottomSheetDialog()
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

    private fun showBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.activity_rating_system, null)
        bottomSheetDialog.setContentView(view)

        // Set the height of the bottom sheet
        val bottomSheet = bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        val layoutParams = bottomSheet?.layoutParams
        layoutParams?.height = 800
        bottomSheet?.layoutParams = layoutParams

        val closeButton = view.findViewById<Button>(R.id.ratingCloseButton)
        closeButton.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        val ratingBar = view.findViewById<RatingBar>(R.id.ratingBar)
        var deliveryRating = 0.0
        ratingBar.onRatingBarChangeListener = RatingBar.OnRatingBarChangeListener { _, rating, _ ->
            // Use the rating value. It will be a value between 1 and 5.
            deliveryRating = rating.toDouble()
        }

        val submitButton = view.findViewById<Button>(R.id.ratingSubmitButton)
        submitButton.setOnClickListener{
            val userId = FirebaseAuth.getInstance().currentUser!!.uid

            database.child("users").child(userId).child("currentJob").get().addOnSuccessListener { snapshot ->
                val currentJobId = (snapshot.value).toString()

                database.child("jobs").child(currentJobId).child("userId").get().addOnSuccessListener { snapshot ->
                    val revieweeUserId = (snapshot.value).toString()

                    database.child("users").child(revieweeUserId).child("totalJobs").get().addOnSuccessListener { snapshot ->
                        val total = snapshot.value as? Int
                        var totalJobs = 0
                        if (total != null) {
                            totalJobs = total
                        }

                        database.child("users").child(revieweeUserId).child("totalRating").get().addOnSuccessListener { snapshot ->
                            val total = snapshot.value as? Int
                            var totalRating = 0
                            if (total != null) {
                                totalRating = total
                            }

                            database.child("users").child(revieweeUserId).child("Rating").get().addOnSuccessListener { snapshot ->
                                val currentRating = snapshot.value as? Double
                                if (currentRating != null) {
                                    database.child("users").child(revieweeUserId).child("Rating").setValue((deliveryRating + totalRating) / totalJobs)
                                }
                                else {
                                    database.child("users").child(revieweeUserId).child("Rating").setValue(deliveryRating)
                                }

                                database.child("users").child(revieweeUserId).child("usersToReview").get().addOnSuccessListener { snapshot ->
                                    val reviewList = snapshot.value as? MutableList<String>
                                    if (reviewList != null) {
                                        reviewList.add(userId)
                                        database.child("users").child(revieweeUserId).child("usersToReview").setValue(reviewList)
                                    }
                                    else {
                                        database.child("users").child(revieweeUserId).child("usersToReview").setValue(
                                            mutableListOf(userId)
                                        )
                                    }

                                    database.child("users").child(revieweeUserId).child("totalJobs").get().addOnSuccessListener { snapshot ->
                                        val total = snapshot.value as? Int
                                        if (total != null) {
                                            database.child("users").child(revieweeUserId).child("totalJobs").setValue(total + 1)
                                        }
                                        else {
                                            database.child("users").child(revieweeUserId).child("totalJobs").setValue(1)
                                        }

                                        database.child("users").child(revieweeUserId).child("totalRatings").get().addOnSuccessListener { snapshot ->
                                            val total = snapshot.value as? Int
                                            if (total != null) {
                                                database.child("users").child(revieweeUserId).child("totalRatings").setValue(total + deliveryRating)
                                            }
                                            else {
                                                database.child("users").child(revieweeUserId).child("totalRatings").setValue(deliveryRating)
                                            }

                                            database.child("users").child(userId)
                                                .child("currentJob").setValue(null)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }
}
