package com.couchpotatoes.currentRequest

import android.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.couchpotatoes.R
import com.couchpotatoes.classes.Job
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue

class CurrentRequestsAdapter(
    private val requestsList: MutableList<Job>,
    private val currentRequestsIds: MutableList<String>,
    private val userEmail: String?,
    private val auth: FirebaseAuth,
    private val database: DatabaseReference,
) : RecyclerView.Adapter<CurrentRequestsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val requesterName: TextView = view.findViewById<TextView>(R.id.requesterName)
        val requesterEmail: TextView = view.findViewById<TextView>(R.id.requesterEmail)
        val item: TextView = view.findViewById<TextView>(R.id.item)
        val price: TextView = view.findViewById<TextView>(R.id.price)
        val store: TextView = view.findViewById<TextView>(R.id.store)
        val deliveryAddress: TextView = view.findViewById<TextView>(R.id.deliveryAddress)
        val status: TextView = view.findViewById<TextView>(R.id.status)
        val cancelButton: TextView = view.findViewById<Button>(R.id.cancel)
        val completeButton: TextView = view.findViewById<Button>(R.id.complete)
        // Initialize other views
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.current_request_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val requestListJob = requestsList[position]

        requestListJob?.uid?.let { it ->
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


                        holder.cancelButton.setOnClickListener {
                            AlertDialog.Builder(it.context)
                                .setTitle("Cancel Request")
                                .setMessage("Are you sure you want to cancel this request?")
                                .setPositiveButton("Yes") { _, _ ->
                                    job?.uid.let { uid ->
                                        // Create a new list excluding the cancelled job's UID
                                        currentRequestsIds.remove(uid)

                                        Log.d("current", currentRequestsIds.toString())

                                        // Update the new job list in the user's 'currentJobs' in Firebase
                                        auth.currentUser?.uid?.let { userId ->
                                            database.child("users").child(userId).child("currentRequests").setValue(currentRequestsIds)
                                        }

                                        if (userEmail == job?.requesterEmail) {
                                            if (uid != null) {
                                                database.child("jobs").child(uid).removeValue()
                                            }
                                        } else {
                                            if (uid != null) {
                                                database.child("jobs").child(uid).child("status").setValue("pending")
                                            }
                                        }
                                    }
                                }
                                .setNegativeButton("No", null)
                                .show()
                        }
                        holder.completeButton.setOnClickListener {
                            AlertDialog.Builder(it.context)
                                .setTitle("Complete Request")
                                .setMessage("Are you sure this request has been completed?")
                                .setPositiveButton("Yes") { _ , _ ->
                                    job?.uid.let { uid ->
                                        if (uid != null) {
                                            database.child("jobs").child(uid).removeValue()
                                        }
                                        currentRequestsIds.remove(uid)
                                        Log.d("current", currentRequestsIds.toString())
                                        // Update the new job list in the user's 'currentJobs' in Firebase
                                        auth.currentUser?.uid?.let { userId ->
                                            database.child("users").child(userId)
                                                .child("currentRequests")
                                                .setValue(currentRequestsIds)
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

    override fun getItemCount() = requestsList.size
}