package com.couchpotatoes.currentJob

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.couchpotatoes.classes.Job
import com.couchpotatoes.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue


// Gotta show dialog
class CurrentJobsAdapter(private val jobsList: MutableList<Job>,
                         private val currentJobIds: MutableList<String>,
                         private val userEmail: String?,
                         private val auth: FirebaseAuth,
                         private val database: DatabaseReference,
                         private val notificationCallback: (String) -> Unit) : RecyclerView.Adapter<CurrentJobsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val requesterName = view.findViewById<TextView>(R.id.requesterName)
        val requesterEmail = view.findViewById<TextView>(R.id.requesterEmail)
        val item = view.findViewById<TextView>(R.id.item)
        val price = view.findViewById<TextView>(R.id.price)
        val store = view.findViewById<TextView>(R.id.store)
        val deliveryAddress = view.findViewById<TextView>(R.id.deliveryAddress)
        val status = view.findViewById<TextView>(R.id.status)
        val cancelButton = view.findViewById<Button>(R.id.cancel)
        val completeButton = view.findViewById<Button>(R.id.complete)
        // Initialize other views
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.current_job_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jobListJob = jobsList[position]

        jobListJob?.uid?.let {
            database.child("jobs").child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val job = dataSnapshot.getValue<Job>()

                        holder.requesterName.text = job?.requesterName
                        holder.requesterEmail.text = job?.requesterEmail
                        holder.item.text = job?.item
                        holder.price.text = job?.price.toString()
                        holder.store.text = job?.store
                        holder.deliveryAddress.text = job?.deliveryAddress
                        holder.status.text = job?.status

                        if (job?.requesterEmail == userEmail) {
                            holder.completeButton.isEnabled = false
                            holder.completeButton.text = "Your Request"
                        }
                        if (job?.status == "accepted") {
                            holder.completeButton.text = "Gather"
                            holder.completeButton.setBackgroundColor(Color.rgb(0,0x66,0x66))
                        }
                        if (job?.status == "gathering") {
                            holder.completeButton.text = "Deliver"
                            holder.completeButton.setBackgroundColor(Color.rgb(0xC4, 0x50, 0x25))
                        }
                        if (job?.status == "delivering") {
                            holder.completeButton.text = "Complete"
                            holder.completeButton.setBackgroundColor(Color.rgb(0xEE, 0x99, 0x39))
                        }

                        holder.cancelButton.setOnClickListener {
                            AlertDialog.Builder(it.context)
                                .setTitle("Cancel Job")
                                .setMessage("Are you sure you want to cancel this job?")
                                .setPositiveButton("Yes") { dialog, which ->
                                    job?.uid.let { uid ->
                                        // Create a new list excluding the cancelled job's UID
                                        currentJobIds.remove(uid)

                                        Log.d("current", currentJobIds.toString())

                                        // Update the new job list in the user's 'currentJobs' in Firebase
                                        auth.currentUser?.uid?.let { userId ->
                                            database.child("users").child(userId).child("currentJobs").setValue(currentJobIds)
                                        }

                                        if (userEmail == job?.requesterEmail) {
                                            if (uid != null) {
                                                database.child("jobs").child(uid).removeValue()
                                            }
                                            notificationCallback("Job cancelled by requester")
                                        } else {
                                            if (uid != null) {
                                                database.child("jobs").child(uid).child("status").setValue("pending")
                                            }
                                            notificationCallback("Job cancelled by dasher")
                                        }
                                    }
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }
                        holder.completeButton.setOnClickListener {
                            AlertDialog.Builder(it.context)
                                .setTitle("Job Progress")
                                .setMessage("Are you sure you have completed this stage?")
                                .setPositiveButton("Yes") { dialog, which ->
                                    when (job?.status) {
                                        "accepted", "gathering", "delivering" -> {
                                            val nextStatus = when (job?.status) {
                                                "accepted" -> "gathering"
                                                "gathering" -> "delivering"
                                                else -> "complete"
                                            }
                                            job?.uid.let { uid -> if (uid != null) {
                                                database.child("jobs").child(uid).child("status").setValue(nextStatus)
                                            }
                                                if (nextStatus == "complete") {
                                                    currentJobIds.remove(uid)

                                                    Log.d("current", currentJobIds.toString())
                                                    // Update the new job list in the user's 'currentJobs' in Firebase
                                                    auth.currentUser?.uid?.let { userId ->
                                                        database.child("users").child(userId).child("currentJobs").setValue(currentJobIds)
                                                    }
                                                }
                                            }
                                            notificationCallback("Job status updated to $nextStatus")
                                        }
                                    }
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        println("The read failed: " + error.code)
                    }
                })
        }



    }

    // PUT THE STUFF INSIDE OF THE ONDATACHSNGE SO IT YODATES ANBD TIUY CSAN SET DIALOG TEXT


    override fun getItemCount() = jobsList.size
}
