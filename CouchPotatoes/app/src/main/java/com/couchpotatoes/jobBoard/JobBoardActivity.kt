package com.couchpotatoes.jobBoard

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.couchpotatoes.CurrentJobActivity
import com.couchpotatoes.MainActivity
import com.couchpotatoes.R
import com.couchpotatoes.RequestFormActivity
import com.couchpotatoes.UserProfileActivity
import com.couchpotatoes.UserSelectionActivity
import com.couchpotatoes.classes.Job
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class JobBoardActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_job_board)

        auth = FirebaseAuth.getInstance()

        val toolBar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        toolBar.setBackgroundColor(Color.parseColor("#00000000"));

        drawer {
            primaryItem("Home") {
                onClick { _ ->
                    val intent = Intent(this@JobBoardActivity, UserSelectionActivity::class.java)
                    startActivity(intent)
                    false
                }
            }
            primaryItem("User Profile") {
                onClick { _ ->
                    val currentUser = auth.currentUser
                    val intent = Intent(this@JobBoardActivity, UserProfileActivity::class.java).apply {
                        putExtra("userName", currentUser?.displayName)
                        putExtra("userEmail", currentUser?.email)
                    }
                    startActivity(intent)
                    false
                }
            }
            primaryItem("Current Job") {
                onClick { _ ->
                    val intent = Intent(this@JobBoardActivity, CurrentJobActivity::class.java)
                    startActivity(intent)
                    false
                }
            }
            primaryItem("Job Board") {
                onClick { _ ->
                    val intent = Intent(this@JobBoardActivity, JobBoardActivity::class.java)
                    startActivity(intent)
                    false
                }
            }

            // put everything for now, will separate based on role later
            primaryItem("Request Form") {
                onClick { _ ->
                    val intent = Intent(this@JobBoardActivity, RequestFormActivity::class.java)
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
                        val googleSignInClient = GoogleSignIn.getClient(this@JobBoardActivity, gso)
                        googleSignInClient.signOut().addOnCompleteListener {
                            // After signing out, redirect the user back to MainActivity
                            val intent = Intent(this@JobBoardActivity, MainActivity::class.java)
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

        var database: DatabaseReference = Firebase.database.reference
        val jobsRef = database.child("jobs")
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // get number of jobs
                var jobList = ArrayList<Job>()
                for (ds in dataSnapshot.children) {
                    val job = Job()
                    if (ds.child("status").getValue<String?>().toString() == "pending") {
                        // Pass job details to Job Board Adapter to create the Job Board List
                        job.deliveryAddress = ds.child("deliveryAddress").getValue<String?>().toString()
                        job.status = ds.child("status").getValue<String?>().toString()
                        job.requesterName = ds.child("requesterName").getValue<String?>().toString()
                        job.requesterEmail = ds.child("requesterEmail").getValue<String?>().toString()
                        job.item = ds.child("item").getValue<String?>().toString()
                        job.price = ds.child("price").getValue<String?>().toString()
                        job.store = ds.child("store").getValue<String?>().toString()
                        job.uid = ds.child("uid").getValue<String?>().toString()
                        jobList.add(job)
                    }
                }

                val recycleView = findViewById<RecyclerView>(R.id.job_recycler_view)
                recycleView.layoutManager = LinearLayoutManager(this@JobBoardActivity)
                recycleView.adapter = JobBoardAdapter(jobList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        jobsRef.addValueEventListener(postListener)
    }
}