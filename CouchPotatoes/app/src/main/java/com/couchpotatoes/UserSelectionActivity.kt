package com.couchpotatoes

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import co.zsmb.materialdrawerkt.builders.drawer
import co.zsmb.materialdrawerkt.builders.footer
import co.zsmb.materialdrawerkt.draweritems.badgeable.primaryItem
import com.couchpotatoes.jobBoard.JobBoardActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class UserSelectionActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_selection)

        auth = FirebaseAuth.getInstance()

        val toolBar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(toolBar)
        supportActionBar?.setDisplayShowTitleEnabled(false);
        toolBar.setBackgroundColor(Color.parseColor("#00000000"));

        drawer {
            primaryItem("Home") {
                onClick { _ ->
                    val intent = Intent(this@UserSelectionActivity, UserSelectionActivity::class.java)
                    startActivity(intent)
                    false
                }
            }
            primaryItem("User Profile") {
                onClick { _ ->
                    val currentUser = auth.currentUser
                    val intent = Intent(this@UserSelectionActivity, UserProfileActivity::class.java).apply {
                        putExtra("userName", currentUser?.displayName)
                        putExtra("userEmail", currentUser?.email)
                    }
                    startActivity(intent)
                    false
                }
            }
            primaryItem("Current Job") {
                onClick { _ ->
                    val intent = Intent(this@UserSelectionActivity, CurrentJobActivity::class.java)
                    startActivity(intent)
                    false
                }
            }
            primaryItem("Job Board") {
                onClick { _ ->
                    val intent = Intent(this@UserSelectionActivity, JobBoardActivity::class.java)
                    startActivity(intent)
                    false
                }
            }

            // put everything for now, will separate based on role later
            primaryItem("Request Form") {
                onClick { _ ->
                    val intent = Intent(this@UserSelectionActivity, RequestFormActivity::class.java)
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
                        val googleSignInClient = GoogleSignIn.getClient(this@UserSelectionActivity, gso)
                        googleSignInClient.signOut().addOnCompleteListener {
                            // After signing out, redirect the user back to MainActivity
                            val intent = Intent(this@UserSelectionActivity, MainActivity::class.java)
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

        database = Firebase.database.reference

        val requesterButton = findViewById<Button>(R.id.requesterButton)
        requesterButton.setOnClickListener {
            // set user role to requester
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            database.child("users").child(userId).child("role").setValue("requester")

            val intent = Intent(this, RequestFormActivity::class.java)
            startActivity(intent)
        }

        val dasherButton = findViewById<Button>(R.id.dasherButton)
        dasherButton.setOnClickListener {
            // set user role to dasher
            val userId = FirebaseAuth.getInstance().currentUser!!.uid
            database.child("users").child(userId).child("role").setValue("dasher")

            val intent = Intent(this, JobBoardActivity::class.java)
            startActivity(intent)
        }

    }
}