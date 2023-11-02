package com.couchpotatoes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.couchpotatoes.classes.Job
import com.couchpotatoes.classes.User
import com.couchpotatoes.jobBoard.JobBoardActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import java.util.UUID


class RequestFormActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_form)

        auth = FirebaseAuth.getInstance()

        val toolBar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        toolBar.setBackgroundColor(Color.parseColor("#00000000"));

        drawer {
            primaryItem("Home") {
                onClick { _ ->
                    val intent = Intent(this@RequestFormActivity, UserSelectionActivity::class.java)
                    startActivity(intent)
                    false
                }
            }
            primaryItem("User Profile") {
                onClick { _ ->
                    val currentUser = auth.currentUser
                    val intent = Intent(this@RequestFormActivity, UserProfileActivity::class.java).apply {
                        putExtra("userName", currentUser?.displayName)
                        putExtra("userEmail", currentUser?.email)
                    }
                    startActivity(intent)
                    false
                }
            }
            primaryItem("Current Job") {
                onClick { _ ->
                    val intent = Intent(this@RequestFormActivity, CurrentJobActivity::class.java)
                    startActivity(intent)
                    false
                }
            }
            primaryItem("Job Board") {
                onClick { _ ->
                    val intent = Intent(this@RequestFormActivity, JobBoardActivity::class.java)
                    startActivity(intent)
                    false
                }
            }

            // put everything for now, will separate based on role later
            primaryItem("Request Form") {
                onClick { _ ->
                    val intent = Intent(this@RequestFormActivity, RequestFormActivity::class.java)
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
                        val googleSignInClient = GoogleSignIn.getClient(this@RequestFormActivity, gso)
                        googleSignInClient.signOut().addOnCompleteListener {
                            // After signing out, redirect the user back to MainActivity
                            val intent = Intent(this@RequestFormActivity, MainActivity::class.java)
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
    }

    fun submitbuttonHandler(view: View) {
        //Decide what happens when the user clicks the submit button
        database = Firebase.database.reference

        val whatEditText = findViewById<View>(R.id.what) as EditText
        val what = whatEditText.text.toString()
        val whereEditText = findViewById<View>(R.id.where) as EditText
        val where = whereEditText.text.toString()
        val costEditText = findViewById<View>(R.id.cost) as EditText
        val cost = costEditText.text.toString()
        val addressEditText = findViewById<View>(R.id.address) as EditText
        val address = addressEditText.text.toString()

        // switch to better system later
        val jobId = UUID.randomUUID().toString()

        // create job
        val job = Job(
            jobId,
            FirebaseAuth.getInstance().currentUser!!.displayName,
            FirebaseAuth.getInstance().currentUser!!.email,
            what,
            cost,
            where,
            address,
            "pending")

        database.child("jobs").child(jobId).setValue(job)
    }
}