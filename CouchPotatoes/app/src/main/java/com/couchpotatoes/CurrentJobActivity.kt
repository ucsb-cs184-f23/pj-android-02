package com.couchpotatoes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.couchpotatoes.classes.Job
import com.couchpotatoes.jobBoard.JobBoardActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
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

        val toolBar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        toolBar.setBackgroundColor(Color.parseColor("#00000000"));

        drawer {
            primaryItem("Home") {
                onClick { _ ->
                    val intent = Intent(this@CurrentJobActivity, UserSelectionActivity::class.java)
                    startActivity(intent)
                    false
                }
            }
            primaryItem("User Profile") {
                onClick { _ ->
                    val currentUser = auth.currentUser
                    val intent = Intent(this@CurrentJobActivity, UserProfileActivity::class.java).apply {
                        putExtra("userName", currentUser?.displayName)
                        putExtra("userEmail", currentUser?.email)
                    }
                    startActivity(intent)
                    false
                }
            }
            primaryItem("Current Job") {
                onClick { _ ->
                    val intent = Intent(this@CurrentJobActivity, CurrentJobActivity::class.java)
                    startActivity(intent)
                    false
                }
            }
            primaryItem("Job Board") {
                onClick { _ ->
                    val intent = Intent(this@CurrentJobActivity, JobBoardActivity::class.java)
                    startActivity(intent)
                    false
                }
            }

            // put everything for now, will separate based on role later
            primaryItem("Request Form") {
                onClick { _ ->
                    val intent = Intent(this@CurrentJobActivity, RequestFormActivity::class.java)
                    startActivity(intent)
                    false
                }
            }
            footer {
                primaryItem("Logout") {
                    textColor = 0xFFFF0000
                    onClick { _ ->
                        // Sign out from FirebaseAuth
                        auth.signOut()

                        // Sign out from GoogleSignInClient
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(this@CurrentJobActivity, gso)
                        googleSignInClient.signOut().addOnCompleteListener {
                            // After signing out, redirect the user back to MainActivity
                            val intent = Intent(this@CurrentJobActivity, MainActivity::class.java)
                            // Clear the activity stack to prevent the user from navigating back to the profile activity
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        false
                    }
                }
            }

            // use created toolbar for drawer
            toolbar = toolBar
        }

        val user = FirebaseAuth.getInstance().currentUser

        val detailsCard = findViewById<CardView>(R.id.detailsCard)
        detailsCard.visibility = View.GONE
        val emptyView = findViewById<TextView>(R.id.emptyView)
        emptyView.visibility = View.VISIBLE

        // First, get the currentJob for the user
        database.child("users").child(user!!.uid).child("currentJob").get().addOnSuccessListener { snapshot ->
            val currentJob = snapshot.value as? String
            if (currentJob != null) {
                // Now, get the job details using the currentJob
                database.child("jobs").child(currentJob).get().addOnSuccessListener { jobSnapshot ->
                    val job = jobSnapshot.getValue(Job::class.java)

                    if (job == null) {
                        detailsCard.visibility = View.GONE
                        emptyView.text = "Your current job was cancelled"
                        database.child("users").child(user!!.uid).child("currentJob").removeValue()
                    } else {
                        emptyView.visibility = View.GONE
                        detailsCard.visibility = View.VISIBLE
                        findViewById<TextView>(R.id.requesterName).text = job?.requesterName
                        findViewById<TextView>(R.id.requesterEmail).text = job?.requesterEmail
                        findViewById<TextView>(R.id.item).text = job?.item
                        findViewById<TextView>(R.id.price).text = job?.price
                        findViewById<TextView>(R.id.store).text = job?.store
                        findViewById<TextView>(R.id.deliveryAddress).text = job?.deliveryAddress
                        findViewById<TextView>(R.id.status).text = job?.status
                        val cancelButton = findViewById<Button>(R.id.cancel)
                        val completeButton = findViewById<Button>(R.id.complete)

                        val dialogBuilder = AlertDialog.Builder(this)

                        if (user.email == job?.requesterEmail) { // You are the requester

                            cancelButton.setOnClickListener {
                                dialogBuilder.setMessage("Are you sure you want to cancel this job?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        database.child("jobs").child(currentJob).removeValue()
                                        database.child("users").child(user!!.uid).child("currentJob").setValue(null)
                                        recreate()
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            }

                            completeButton.setOnClickListener {
                                dialogBuilder.setMessage("Are you sure you want to mark this job as complete?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        database.child("jobs").child(currentJob).child("status").setValue("complete")
                                        database.child("users").child(user!!.uid).child("currentJob").setValue(null)
                                        recreate()
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            }

                        } else { // You are the dasher

                            cancelButton.setOnClickListener {
                                dialogBuilder.setMessage("Are you sure you want to cancel this job?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        database.child("users").child(user!!.uid).child("currentJob").setValue(null)
                                        recreate()
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            }

                            completeButton.setOnClickListener {
                                dialogBuilder.setMessage("Are you sure you want to mark this job as complete?")
                                    .setPositiveButton("Yes") { _, _ ->
                                        database.child("jobs").child(currentJob).child("status").setValue("complete")
                                        database.child("users").child(user!!.uid).child("currentJob").setValue(null)
                                        recreate()
                                    }
                                    .setNegativeButton("No", null)
                                    .show()
                            }
                        }
                    }
                }.addOnFailureListener {
                    emptyView.visibility = View.VISIBLE
                }
            } else {
                emptyView.visibility = View.VISIBLE
            }
        }.addOnFailureListener {
            emptyView.visibility = View.VISIBLE
        }
    }
}
